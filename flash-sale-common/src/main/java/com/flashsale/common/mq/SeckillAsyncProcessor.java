package com.flashsale.common.mq;

import com.flashsale.common.dto.SeckillDTO;
import com.flashsale.common.result.Result;
import com.flashsale.common.security.IdempotencyService;
import com.flashsale.common.security.AntiScalpingService;
import com.flashsale.common.lock.DistributedLockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 秒杀异步处理器 - 削峰填谷核心组件
 * 负责处理秒杀请求的排队、批处理、异步执行
 * @author 21311
 */
@Slf4j
@Component
public class SeckillAsyncProcessor {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private IdempotencyService idempotencyService;
    
    @Autowired
    private AntiScalpingService antiScalpingService;
    
    @Autowired
    private DistributedLockService distributedLockService;
    
    // 队列前缀
    private static final String SECKILL_QUEUE_PREFIX = "seckill:async:queue:";
    private static final String PROCESSING_QUEUE_PREFIX = "seckill:async:processing:";
    private static final String RESULT_PREFIX = "seckill:async:result:";
    private static final String FAILED_QUEUE_PREFIX = "seckill:async:failed:";
    private static final String METRICS_PREFIX = "seckill:async:metrics:";
    
    // 线程池配置
    private ThreadPoolExecutor asyncExecutor;
    private ScheduledExecutorService scheduledExecutor;
    
    // 处理统计
    private final AtomicLong processedCount = new AtomicLong(0);
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong failureCount = new AtomicLong(0);
    
    // 批处理配置
    private static final int BATCH_SIZE = 100;
    private static final int MAX_QUEUE_SIZE = 10000;
    private static final int PROCESSING_TIMEOUT_SECONDS = 30;
    
    @PostConstruct
    public void init() {
        // 初始化线程池
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        int maxPoolSize = corePoolSize * 4;
        
        asyncExecutor = new ThreadPoolExecutor(
            corePoolSize, maxPoolSize, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            r -> new Thread(r, "seckill-async-" + System.currentTimeMillis()),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        scheduledExecutor = Executors.newScheduledThreadPool(2);
        
        // 启动批处理任务
        startBatchProcessor();
        
        // 启动监控任务
        startMonitorTask();
        
        // 启动失败重试任务
        startRetryProcessor();
        
        log.info("秒杀异步处理器初始化完成: corePool={}, maxPool={}", corePoolSize, maxPoolSize);
    }
    
    @PreDestroy
    public void destroy() {
        if (asyncExecutor != null) {
            asyncExecutor.shutdown();
            try {
                if (!asyncExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    asyncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                asyncExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }
        
        log.info("秒杀异步处理器已关闭");
    }
    
    /**
     * 提交秒杀请求到异步队列
     * @param seckillDTO 秒杀请求
     * @return 异步任务ID
     */
    public String submitSeckillRequest(SeckillDTO seckillDTO) {
        // 生成任务ID
        String taskId = generateTaskId(seckillDTO.getUserId(), seckillDTO.getFlashSaleProductId());
        
        // 检查队列是否已满
        String queueKey = SECKILL_QUEUE_PREFIX + seckillDTO.getFlashSaleProductId();
        Long queueSize = redisTemplate.opsForList().size(queueKey);
        if (queueSize != null && queueSize >= MAX_QUEUE_SIZE) {
            log.warn("队列已满，拒绝请求: productId={}, queueSize={}", seckillDTO.getFlashSaleProductId(), queueSize);
            return null;
        }
        
        // 预处理：快速验证
        if (!preValidateRequest(seckillDTO)) {
            log.warn("预验证失败: userId={}, productId={}", seckillDTO.getUserId(), seckillDTO.getFlashSaleProductId());
            return null;
        }
        
        // 序列化请求并添加到队列
        try {
            SeckillTaskWrapper taskWrapper = new SeckillTaskWrapper(taskId, seckillDTO, System.currentTimeMillis());
            String serializedTask = serializeTask(taskWrapper);
            
            // 使用Lua脚本确保原子性
            String luaScript = 
                "local queueKey = KEYS[1] " +
                "local resultKey = KEYS[2] " +
                "local taskId = ARGV[1] " +
                "local serializedTask = ARGV[2] " +
                "local currentTime = ARGV[3] " +
                
                // 检查是否已存在相同任务
                "if redis.call('exists', resultKey) == 1 then " +
                "    return nil " +
                "end " +
                
                // 添加到队列并设置初始状态
                "redis.call('lpush', queueKey, serializedTask) " +
                "redis.call('setex', resultKey, 1800, 'QUEUED') " + // 30分钟过期
                "return taskId";
            
            DefaultRedisScript<String> script = new DefaultRedisScript<>(luaScript, String.class);
            String resultKey = RESULT_PREFIX + taskId;
            
            String result = redisTemplate.execute(script,
                Arrays.asList(queueKey, resultKey),
                taskId, serializedTask, String.valueOf(System.currentTimeMillis()));
            
            if (result != null) {
                log.info("秒杀请求已提交到异步队列: taskId={}, userId={}, productId={}", 
                        taskId, seckillDTO.getUserId(), seckillDTO.getFlashSaleProductId());
                return taskId;
            } else {
                log.warn("重复的秒杀请求: taskId={}", taskId);
                return null;
            }
            
        } catch (Exception e) {
            log.error("提交秒杀请求失败", e);
            return null;
        }
    }
    
    /**
     * 查询异步任务结果
     * @param taskId 任务ID
     * @return 任务结果
     */
    public AsyncTaskResult getTaskResult(String taskId) {
        String resultKey = RESULT_PREFIX + taskId;
        String status = (String) redisTemplate.opsForValue().get(resultKey);
        
        if (status == null) {
            return new AsyncTaskResult(taskId, "NOT_FOUND", null, null);
        }
        
        // 如果是完成状态，获取详细结果
        if ("SUCCESS".equals(status) || "FAILED".equals(status)) {
            String detailKey = resultKey + ":detail";
            String detail = (String) redisTemplate.opsForValue().get(detailKey);
            return new AsyncTaskResult(taskId, status, detail, null);
        }
        
        // 如果是处理中，获取进度信息
        if ("PROCESSING".equals(status)) {
            String progressKey = resultKey + ":progress";
            String progress = (String) redisTemplate.opsForValue().get(progressKey);
            return new AsyncTaskResult(taskId, status, progress, null);
        }
        
        return new AsyncTaskResult(taskId, status, null, null);
    }
    
    /**
     * 启动批处理任务
     */
    private void startBatchProcessor() {
        scheduledExecutor.scheduleWithFixedDelay(() -> {
            try {
                processBatch();
            } catch (Exception e) {
                log.error("批处理任务异常", e);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }
    
    /**
     * 处理批量任务
     */
    private void processBatch() {
        // 获取所有活跃的商品队列
        Set<String> productQueues = getActiveProductQueues();
        
        for (String queueKey : productQueues) {
            Long productId = extractProductIdFromQueueKey(queueKey);
            if (productId == null) continue;
            
            // 为每个商品并行处理
            asyncExecutor.submit(() -> processProductQueue(productId, queueKey));
        }
    }
    
    /**
     * 处理单个商品的队列
     */
    private void processProductQueue(Long productId, String queueKey) {
        try {
            // 批量获取任务
            List<String> tasks = new ArrayList<>();
            for (int i = 0; i < BATCH_SIZE; i++) {
                String task = (String) redisTemplate.opsForList().rightPop(queueKey);
                if (task == null) break;
                tasks.add(task);
            }
            
            if (tasks.isEmpty()) return;
            
            log.info("开始批处理: productId={}, batchSize={}", productId, tasks.size());
            
            // 处理每个任务
            for (String serializedTask : tasks) {
                try {
                    SeckillTaskWrapper taskWrapper = deserializeTask(serializedTask);
                    if (taskWrapper != null) {
                        processTask(taskWrapper);
                    }
                } catch (Exception e) {
                    log.error("处理单个任务失败: task={}", serializedTask, e);
                    // 将失败的任务添加到重试队列
                    addToRetryQueue(serializedTask, e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("批处理异常: productId={}", productId, e);
        }
    }
    
    /**
     * 处理单个秒杀任务
     */
    private void processTask(SeckillTaskWrapper taskWrapper) {
        String taskId = taskWrapper.getTaskId();
        SeckillDTO seckillDTO = taskWrapper.getSeckillDTO();
        
        try {
            // 更新状态为处理中
            updateTaskStatus(taskId, "PROCESSING", "开始处理秒杀请求");
            
            // 执行完整的秒杀逻辑
            Result<String> seckillResult = executeCompleteSeckill(seckillDTO);
            
            // 更新最终结果
            if (seckillResult.getCode().equals("200")) {
                updateTaskStatus(taskId, "SUCCESS", seckillResult.getData());
                successCount.incrementAndGet();
                log.info("秒杀任务处理成功: taskId={}, orderNo={}", taskId, seckillResult.getData());
            } else {
                updateTaskStatus(taskId, "FAILED", seckillResult.getMessage());
                failureCount.incrementAndGet();
                log.warn("秒杀任务处理失败: taskId={}, reason={}", taskId, seckillResult.getMessage());
            }
            
            processedCount.incrementAndGet();
            
        } catch (Exception e) {
            log.error("处理秒杀任务异常: taskId={}", taskId, e);
            updateTaskStatus(taskId, "FAILED", "系统异常: " + e.getMessage());
            failureCount.incrementAndGet();
            processedCount.incrementAndGet();
        }
    }
    
    /**
     * 执行完整的秒杀逻辑（需要注入真实的秒杀服务）
     */
    private Result<String> executeCompleteSeckill(SeckillDTO seckillDTO) {
        // 这里应该调用真实的秒杀服务
        // 为了示例，这里只返回模拟结果
        
        // 1. 幂等性检查
        String requestId = seckillDTO.getUserId() + ":" + seckillDTO.getFlashSaleProductId() + ":" + 
                          (seckillDTO.getRequestId() != null ? seckillDTO.getRequestId() : UUID.randomUUID().toString());
        
        if (!idempotencyService.checkSeckillIdempotency(seckillDTO.getUserId(), 
                seckillDTO.getFlashSaleProductId(), requestId)) {
            return Result.error("重复请求");
        }
        
        // 2. 防黄牛检查
        if (!antiScalpingService.checkUserLegitimacy(seckillDTO.getUserId(), 
                seckillDTO.getUserIp(), seckillDTO.getDeviceFingerprint(),
                seckillDTO.getUserLevel(), seckillDTO.getCreditScore(), seckillDTO.getIsVerified())) {
            return Result.error("用户验证失败");
        }
        
        // 3. 模拟库存检查和扣减（实际应该调用真实服务）
        String lockKey = "seckill:stock:" + seckillDTO.getFlashSaleProductId();
        return distributedLockService.executeWithLock(lockKey, 1, 10, TimeUnit.SECONDS, () -> {
            // 模拟业务逻辑
            String orderNo = "FS" + System.currentTimeMillis() + 
                           UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            
            // 模拟成功率（90%成功）
            if (Math.random() < 0.9) {
                return Result.success(orderNo);
            } else {
                throw new RuntimeException("库存不足");
            }
        });
    }
    
    /**
     * 预验证请求（快速检查）
     */
    private boolean preValidateRequest(SeckillDTO seckillDTO) {
        // 快速验证必要字段
        if (seckillDTO.getUserId() == null || seckillDTO.getFlashSaleProductId() == null) {
            return false;
        }
        
        // 快速黑名单检查
        if (seckillDTO.getUserIp() != null) {
            String blacklistKey = "blacklist:ip:" + seckillDTO.getUserIp();
            if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 更新任务状态
     */
    private void updateTaskStatus(String taskId, String status, String detail) {
        String resultKey = RESULT_PREFIX + taskId;
        String detailKey = resultKey + ":detail";
        
        redisTemplate.opsForValue().set(resultKey, status, 30, TimeUnit.MINUTES);
        if (detail != null) {
            redisTemplate.opsForValue().set(detailKey, detail, 30, TimeUnit.MINUTES);
        }
    }
    
    /**
     * 获取活跃的商品队列
     */
    private Set<String> getActiveProductQueues() {
        Set<String> keys = redisTemplate.keys(SECKILL_QUEUE_PREFIX + "*");
        return keys != null ? keys : new HashSet<>();
    }
    
    /**
     * 从队列键提取商品ID
     */
    private Long extractProductIdFromQueueKey(String queueKey) {
        try {
            String productIdStr = queueKey.substring(SECKILL_QUEUE_PREFIX.length());
            return Long.parseLong(productIdStr);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 生成任务ID
     */
    private String generateTaskId(Long userId, Long productId) {
        return userId + ":" + productId + ":" + System.currentTimeMillis() + ":" + 
               UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * 序列化任务
     */
    private String serializeTask(SeckillTaskWrapper taskWrapper) {
        try {
            return String.format("{\"taskId\":\"%s\",\"userId\":%d,\"productId\":%d,\"createTime\":%d,\"userIp\":\"%s\",\"quantity\":%d,\"userLevel\":%d,\"creditScore\":%d,\"isVerified\":%s,\"requestId\":\"%s\"}",
                taskWrapper.getTaskId(), 
                taskWrapper.getSeckillDTO().getUserId(),
                taskWrapper.getSeckillDTO().getFlashSaleProductId(),
                taskWrapper.getCreateTime(),
                taskWrapper.getSeckillDTO().getUserIp() != null ? taskWrapper.getSeckillDTO().getUserIp() : "",
                taskWrapper.getSeckillDTO().getQuantity(),
                taskWrapper.getSeckillDTO().getUserLevel() != null ? taskWrapper.getSeckillDTO().getUserLevel() : 1,
                taskWrapper.getSeckillDTO().getCreditScore() != null ? taskWrapper.getSeckillDTO().getCreditScore() : 60,
                taskWrapper.getSeckillDTO().getIsVerified() != null ? taskWrapper.getSeckillDTO().getIsVerified() : false,
                taskWrapper.getSeckillDTO().getRequestId() != null ? taskWrapper.getSeckillDTO().getRequestId() : "");
        } catch (Exception e) {
            log.error("序列化任务失败", e);
            return null;
        }
    }
    
    /**
     * 反序列化任务
     */
    private SeckillTaskWrapper deserializeTask(String serializedTask) {
        try {
            // 简单解析JSON（实际应该使用JSON库）
            String taskId = extractJsonValue(serializedTask, "taskId");
            Long userId = Long.parseLong(extractJsonValue(serializedTask, "userId"));
            Long productId = Long.parseLong(extractJsonValue(serializedTask, "productId"));
            Long createTime = Long.parseLong(extractJsonValue(serializedTask, "createTime"));
            String userIp = extractJsonValue(serializedTask, "userIp");
            Integer quantity = Integer.parseInt(extractJsonValue(serializedTask, "quantity"));
            Integer userLevel = Integer.parseInt(extractJsonValue(serializedTask, "userLevel"));
            Integer creditScore = Integer.parseInt(extractJsonValue(serializedTask, "creditScore"));
            Boolean isVerified = Boolean.parseBoolean(extractJsonValue(serializedTask, "isVerified"));
            String requestId = extractJsonValue(serializedTask, "requestId");
            
            SeckillDTO seckillDTO = new SeckillDTO();
            seckillDTO.setUserId(userId);
            seckillDTO.setFlashSaleProductId(productId);
            seckillDTO.setUserIp(userIp);
            seckillDTO.setQuantity(quantity);
            seckillDTO.setUserLevel(userLevel);
            seckillDTO.setCreditScore(creditScore);
            seckillDTO.setIsVerified(isVerified);
            seckillDTO.setRequestId(requestId);
            
            return new SeckillTaskWrapper(taskId, seckillDTO, createTime);
        } catch (Exception e) {
            log.error("反序列化任务失败: task={}", serializedTask, e);
            return null;
        }
    }
    
    /**
     * 简单的JSON值提取（实际应该使用JSON库）
     */
    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\":\"?([^,}\"]+)\"?";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : "";
    }
    
    /**
     * 添加到重试队列
     */
    private void addToRetryQueue(String serializedTask, String error) {
        String retryQueueKey = FAILED_QUEUE_PREFIX + "retry";
        String retryTask = serializedTask + "|ERROR:" + error + "|TIME:" + System.currentTimeMillis();
        redisTemplate.opsForList().leftPush(retryQueueKey, retryTask);
        redisTemplate.expire(retryQueueKey, 1, TimeUnit.HOURS);
    }
    
    /**
     * 启动重试处理器
     */
    private void startRetryProcessor() {
        scheduledExecutor.scheduleWithFixedDelay(() -> {
            try {
                processRetryQueue();
            } catch (Exception e) {
                log.error("重试处理异常", e);
            }
        }, 10, 30, TimeUnit.SECONDS);
    }
    
    /**
     * 处理重试队列
     */
    private void processRetryQueue() {
        String retryQueueKey = FAILED_QUEUE_PREFIX + "retry";
        String retryTask = (String) redisTemplate.opsForList().rightPop(retryQueueKey);
        
        if (retryTask != null) {
            // 解析重试任务并重新处理
            String[] parts = retryTask.split("\\|ERROR:");
            if (parts.length >= 1) {
                String originalTask = parts[0];
                log.info("重试处理失败任务: {}", originalTask);
                
                SeckillTaskWrapper taskWrapper = deserializeTask(originalTask);
                if (taskWrapper != null) {
                    asyncExecutor.submit(() -> processTask(taskWrapper));
                }
            }
        }
    }
    
    /**
     * 启动监控任务
     */
    private void startMonitorTask() {
        scheduledExecutor.scheduleWithFixedDelay(() -> {
            try {
                logMetrics();
            } catch (Exception e) {
                log.error("监控任务异常", e);
            }
        }, 30, 30, TimeUnit.SECONDS);
    }
    
    /**
     * 记录监控指标
     */
    private void logMetrics() {
        long processed = processedCount.get();
        long success = successCount.get();
        long failure = failureCount.get();
        
        double successRate = processed > 0 ? (double) success / processed * 100 : 0;
        
        log.info("异步处理统计 - 总处理:{}, 成功:{}, 失败:{}, 成功率:{:.2f}%, 活跃线程:{}, 队列任务:{}", 
                processed, success, failure, successRate, 
                asyncExecutor.getActiveCount(), asyncExecutor.getQueue().size());
        
        // 记录到Redis供监控系统读取
        String metricsKey = METRICS_PREFIX + "current";
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("processed", processed);
        metrics.put("success", success);
        metrics.put("failure", failure);
        metrics.put("successRate", successRate);
        metrics.put("activeThreads", asyncExecutor.getActiveCount());
        metrics.put("queueSize", asyncExecutor.getQueue().size());
        metrics.put("timestamp", System.currentTimeMillis());
        
        redisTemplate.opsForHash().putAll(metricsKey, metrics);
        redisTemplate.expire(metricsKey, 1, TimeUnit.HOURS);
    }
    
    /**
     * 任务包装器
     */
    private static class SeckillTaskWrapper {
        private String taskId;
        private SeckillDTO seckillDTO;
        private long createTime;
        
        public SeckillTaskWrapper(String taskId, SeckillDTO seckillDTO, long createTime) {
            this.taskId = taskId;
            this.seckillDTO = seckillDTO;
            this.createTime = createTime;
        }
        
        public String getTaskId() { return taskId; }
        public SeckillDTO getSeckillDTO() { return seckillDTO; }
        public long getCreateTime() { return createTime; }
    }
    
    /**
     * 异步任务结果
     */
    public static class AsyncTaskResult {
        private String taskId;
        private String status;
        private String result;
        private String error;
        
        public AsyncTaskResult(String taskId, String status, String result, String error) {
            this.taskId = taskId;
            this.status = status;
            this.result = result;
            this.error = error;
        }
        
        public String getTaskId() { return taskId; }
        public String getStatus() { return status; }
        public String getResult() { return result; }
        public String getError() { return error; }
        
        public boolean isCompleted() {
            return "SUCCESS".equals(status) || "FAILED".equals(status);
        }
        
        public boolean isSuccess() {
            return "SUCCESS".equals(status);
        }
    }
} 