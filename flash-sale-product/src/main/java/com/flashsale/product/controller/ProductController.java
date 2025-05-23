package com.flashsale.product.controller;

import com.flashsale.common.result.Result;
import com.flashsale.common.result.PageResult;
import com.flashsale.product.dto.ProductDTO;
import com.flashsale.product.service.ProductService;
import com.flashsale.product.vo.ProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 商品控制器
 * @author 21311
 */
@Slf4j
@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 添加商品
     */
    @PostMapping
    public Result<Void> addProduct(@Valid @RequestBody ProductDTO productDTO) {
        return productService.addProduct(productDTO);
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public Result<Void> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO productDTO) {
        return productService.updateProduct(id, productDTO);
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        return productService.deleteProduct(id);
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public Result<ProductVO> getProductDetail(@PathVariable Long id) {
        return productService.getProductDetail(id);
    }

    /**
     * 分页查询商品列表
     */
    @GetMapping("/list")
    public Result<PageResult<ProductVO>> listProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        return productService.listProducts(page, size, categoryId, keyword);
    }

    /**
     * 更新商品状态
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateProductStatus(@PathVariable Long id, @RequestParam Integer status) {
        return productService.updateProductStatus(id, status);
    }

    /**
     * 扣减库存（内部接口）
     */
    @PostMapping("/{id}/decrease-stock")
    public Result<Boolean> decreaseStock(@PathVariable Long id, @RequestParam Integer quantity) {
        return productService.decreaseStock(id, quantity);
    }

    /**
     * 增加库存（内部接口）
     */
    @PostMapping("/{id}/increase-stock")
    public Result<Boolean> increaseStock(@PathVariable Long id, @RequestParam Integer quantity) {
        return productService.increaseStock(id, quantity);
    }
} 