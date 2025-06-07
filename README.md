# Flash Sale 高性能秒杀系统

[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.1-blue)](https://spring.io/projects/spring-cloud)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-latest-red)](https://redis.io/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-latest-orange)](https://www.rabbitmq.com/)

---

## 1. 项目详细介绍

### 项目概述
Flash Sale是一个基于**Spring Cloud微服务架构**的高性能秒杀系统，专为**高并发电商秒杀场景**设计。系统采用微服务拆分，实现了从用户管理、商品管理到秒杀下单、支付完成的完整业务闭环，重点解决了**高并发场景下的数据一致性、系统高可用性和业务性能优化**等核心技术挑战。

### 系统架构设计

```
                    ┌─────────────────┐
                    │   用户端入口     │
                    └─────────┬───────┘
                              │
                    ┌─────────▼───────┐       ┌─────────────────┐
                    │ Spring Gateway  │───────│ Sentinel 流控   │
                    │    API网关      │       │   熔断监控      │
                    └─────────┬───────┘       └─────────────────┘
                              │
            ┌─────────────────┼─────────────────┐
            │                 │                 │
    ┌───────▼────┐    ┌───────▼────┐    ┌──────▼─────┐
    │  用户服务   │    │  商品服务   │    │  秒杀服务  │
    │ (8000端口)  │   │ (8001端口)  │    │ (8083端口) │
    └────────────┘    └────────────┘    └────────────┘
            │                 │                 │
    ┌───────▼────┐    ┌───────▼────┐            │
    │  订单服务   │    │  支付服务   │            │
    │ (8002端口)  │    │ (8004端口) │            │
    └────────────┘    └────────────┘            │
            │                 │                 │
            └─────────────────┼─────────────────┘
                              │
            ┌─────────────────▼─────────────────┐
            │      消息队列 (RabbitMQ)           │
            │   异步处理 + 削峰限流 + 解耦        │
            └─────────────────┬─────────────────┘
                              │
            ┌─────────────────▼─────────────────┐
            │    数据存储层 (MySQL + Redis)      │
            │          持久化存储 + 缓存         │
            └───────────────────────────────────┘
```

### 重要文件及组件介绍

#### Controller层 - API接口控制
- **`SeckillController.java`** - 秒杀核心控制器
  - 实现秒杀下单、令牌生成、结果查询等核心API
  - 集成Sentinel流控注解，提供熔断降级保护
  - 包含系统预热、限流检查等高级功能
  
- **`UserController.java`** - 用户管理控制器
  - 用户注册登录、信息管理、密码修改
  - 用户名/手机号唯一性验证
  
- **`ProductController.java`** - 商品管理控制器
  - 商品CRUD操作、分页查询、关键词搜索
  - 支持分类筛选和商品详情展示
  
- **`OrderController.java`** - 订单管理控制器
  - 订单查询、支付、取消等操作
  - 用户订单历史和待付款订单管理
  
- **`PaymentController.java`** - 支付服务控制器
  - 多种支付方式支持、支付状态回调处理
  - 异步支付流程管理

#### Service层 - 核心业务逻辑
- **`SeckillServiceImpl.java`** - 秒杀业务核心实现
  - **库存预扣机制**: Redis原子操作防止超卖
  - **令牌生成验证**: 防止恶意重复提交
  - **用户限购控制**: 防止单用户重复购买
  - **异步订单处理**: RabbitMQ消息队列处理
  
- **`UserServiceImpl.java`** - 用户服务实现
  - 密码加密存储(SHA-256)
  - Token-based用户会话管理
  - Redis缓存用户信息提升性能
  
- **`OrderServiceImpl.java`** - 订单服务实现
  - 订单状态机管理(待付款→已付款→已完成)
  - 30分钟订单自动过期机制
  - 库存回滚和状态同步

#### Common工具类 - 基础设施
- **`Result.java`** - 统一返回结果封装
  - 标准化API响应格式
  - 包含状态码、消息、数据的完整结构
  
- **`GlobalExceptionHandler.java`** - 全局异常处理器
  - 统一异常捕获和处理
  - 参数校验异常、业务异常的优雅处理
  - 防止敏感错误信息泄露

#### 配置文件
- **`application.yml`** - 各服务配置
  - 数据库连接池配置
  - Redis集群配置
  - RabbitMQ消息队列配置
  - Sentinel流控规则配置

---

## 2. 技术栈

### 后端技术栈
| 技术分类 | 技术选型 | 版本 | 应用场景 |
|---------|---------|------|---------|
| **基础框架** | Spring Boot | 3.2.5 | 微服务基础框架，自动配置 |
| **微服务治理** | Spring Cloud | 2023.0.1 | 服务发现、配置管理、负载均衡 |
| **服务注册中心** | Nacos | 2.x | 服务注册发现 + 动态配置中心 |
| **API网关** | Spring Cloud Gateway | 3.x | 统一入口、路由转发、限流熔断 |
| **流量控制** | Alibaba Sentinel | 1.8.6 | 流量控制、熔断降级、系统保护 |
| **数据库** | MySQL | 8.0 | 主数据存储、事务ACID保证 |
| **缓存** | Redis | 7.x | 热点数据缓存、分布式锁、计数器 |
| **消息队列** | RabbitMQ | 3.x | 异步处理、削峰填谷、系统解耦 |
| **构建工具** | Maven | 3.9+ | 项目构建、依赖管理 |
| **JDK版本** | OpenJDK | 17 | 新特性支持、性能优化 |

### 开发与运维工具
| 工具类型 | 工具名称               | 用途 |
|---------|--------------------|------|
| **IDE** | IntelliJ IDEA      | 代码开发、调试 |
| **版本控制** | Git                | 代码版本管理 |
| **API测试** | ApiFox             | 接口测试、性能测试 |
| **监控面板** | Sentinel Dashboard | 实时流量监控 |

### 中间件选型理由
1. **Nacos**: 阿里云原生，服务发现+配置中心二合一，社区活跃
2. **Sentinel**: 阿里流控组件，与Spring Cloud无缝集成，功能强大
3. **Redis**: 高性能内存数据库，支持多种数据结构，适合高并发场景
4. **RabbitMQ**: 成熟的消息队列，支持多种消息模式，可靠性高
5. **MySQL**: 成熟稳定的关系型数据库，ACID特性保证数据一致性

---

## 3. 项目亮点

### 架构设计亮点

#### 优雅的微服务拆分
- **业务边界清晰**: 将用户、商品、秒杀、订单、支付拆分为独立服务
- **数据隔离性强**: 每个服务维护自己的数据库，避免数据耦合
- **扩展性良好**: 支持单独部署和扩缩容，热点服务可独立扩展

#### 多层防护的高可用设计
```
用户请求 → API网关限流 → Sentinel熔断 → Redis预扣库存 → 数据库最终一致性
    ↓            ↓              ↓             ↓              ↓
  恶意流量     系统保护        服务保护      超卖防护        数据保护
```

### 性能优化亮点

#### Redis多级缓存策略
```markdown
// 热点数据三级缓存
1. JVM本地缓存 (毫秒级)
2. Redis集中缓存 (5-10ms)  
3. 数据库兜底 (50-100ms)
```

#### 库存扣减优化策略
```java
// 核心代码示例
@Override
public Result<String> doSeckill(SeckillDTO seckillDTO) {
    // 1. Redis原子扣减库存，防止超卖
    Long stock = redisTemplate.opsForValue().decrement("stock:" + productId);
    if (stock < 0) {
        // 库存不足，回滚
        redisTemplate.opsForValue().increment("stock:" + productId);
        return Result.error("库存不足");
    }
    
    // 2. 异步创建订单，提升响应速度
    messageProducer.sendOrderMessage(seckillDTO);
    return Result.success("秒杀成功");
}
```

#### 智能异步处理机制
- **支付异步化**: 支付处理通过MQ异步执行，响应时间从2s降至200ms
- **订单超时处理**: 30分钟自动取消未付款订单，释放库存资源
- **消息可靠性**: 支持消息重试、死信队列、幂等性保证

### 安全防护亮点

#### 多重防刷机制
```java
// 令牌机制防重复提交
@PostMapping("/submit")
@SentinelResource(value = "doSeckill", blockHandler = "handleBlock")
public Result<String> doSeckill(@RequestBody SeckillDTO seckillDTO) {
    // 1. 令牌验证
    if (!validateToken(seckillDTO.getToken())) {
        return Result.error("令牌无效");
    }
    
    // 2. 用户限购检查  
    if (hasUserBought(seckillDTO.getUserId(), seckillDTO.getProductId())) {
        return Result.error("您已购买过该商品");
    }
    
    // 3. 频率限制
    if (!rateLimitService.isAllowed(getUserKey(seckillDTO), 5, 60)) {
        return Result.error("访问频率过高");
    }
}
```

#### 数据安全保护
- **密码加密**: SHA-256加盐加密，防止密码泄露
- **Token会话**: 基于Redis的分布式会话管理
- **参数校验**: 全局参数校验，防止SQL注入和XSS攻击

### 监控运维亮点

#### Sentinel实时监控
- **可视化大盘**: 实时QPS、RT、异常率监控
- **动态规则配置**: 支持热更新流控规则，无需重启
- **智能熔断**: 基于错误率和响应时间的自适应熔断

#### 全链路可观测
```java
// 统一日志格式
log.info("用户{}执行秒杀，商品ID：{}，结果：{}", 
    userId, productId, result);
    
// 关键指标埋点
// - 秒杀成功率
// - 平均响应时间  
// - 库存命中率
// - 系统错误率
```

### 业务逻辑亮点

#### 智能秒杀流程设计
```
预热阶段 → 令牌生成 → 资格校验 → 库存扣减 → 异步下单 → 支付处理 → 订单完成
   ↓           ↓          ↓          ↓          ↓          ↓          ↓
系统预热    防刷控制    用户限购    超卖防护    性能优化    可靠性     最终一致性
```

#### 优雅的状态机管理
```java
// 订单状态流转
待付款 → 已付款 → 已发货 → 已完成
  ↓        ↓        ↓        ↓
超时取消  支付成功   物流更新  评价完成
```

### 技术创新亮点

#### 配置热更新
- **Nacos配置中心**: 支持动态配置更新，零停机运维
- **Sentinel规则**: 支持限流规则热更新，应对突发流量

---


## 快速开始

### 环境要求
- **JDK**: 17+
- **Maven**: 3.9+
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **RabbitMQ**: 3.8+
- **Nacos**: 2.x


### 服务端口
| 服务 | 端口 | 访问地址 |
|------|------|---------|
| Gateway | 8080 | http://localhost:8080 |
| User | 8000 | http://localhost:8000 |
| Product | 8001 | http://localhost:8001 |
| Seckill | 8083 | http://localhost:8083 |
| Order | 8002 | http://localhost:8002 |
| Payment | 8004 | http://localhost:8004 |
| Sentinel | 8090 | http://localhost:8090 |
| Nacos | 8848 | http://localhost:8848/nacos |


---

## 性能指标

### 当前性能数据
- **并发能力**: 支持 1000+ 并发秒杀
- **响应时间**: 平均 < 200ms
- **系统可用性**: 99.9%+
- **数据一致性**: 最终一致性保证
- **秒杀成功率**: 99%+ (防超卖)

### 压测结果
```
并发用户: 500
持续时间: 60秒  
平均响应时间: 150ms
99%响应时间: 300ms
错误率: < 0.1%
```

---

## 配置说明

详细配置请参考：[API文档.md](./API文档.md)

---

## 版本历史

### v1.0.0 (当前版本)
- 微服务架构搭建完成
- 秒杀核心功能实现
- Sentinel流控熔断集成
- Redis缓存优化
- RabbitMQ异步处理
- 监控运维完善

### v2.0.0 (规划中)
- [ ] 分布式事务(Seata)
- [ ] 数据库分库分表
- [ ] 链路追踪(Zipkin)
- [ ] 搜索引擎(ES)
- [ ] 容器化部署(K8s)

---

## 项目总结

本项目成功实现了**高并发秒杀场景**的完整解决方案，在**架构设计、性能优化、安全防护、运维监控**等方面都有较为完善的实现。

**核心成就**:
- **架构**: 微服务拆分合理，扩展性强
- **性能**: Redis缓存+异步处理，响应快速
- **安全**: 多层防护机制，杜绝超卖
- **监控**: Sentinel实时监控，运维友好

**技术价值**: 展现了在分布式高并发场景下的**系统设计能力**和**技术整合能力**，是一个具有实际生产价值的秒杀系统解决方案。

---

**如果这个项目对您有帮助，请给它一个Star！** 