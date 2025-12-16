package com.zjfc.smartgarbage.controller;

import com.zjfc.smartgarbage.model.entity.GarbageCategory;
import com.zjfc.smartgarbage.model.vo.ApiResponse;
import com.zjfc.smartgarbage.service.GarbageCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class GarbageCategoryController {

    @Autowired
    private GarbageCategoryService categoryService;

    @GetMapping
    public ApiResponse<List<GarbageCategory>> getAllCategories() {
        List<GarbageCategory> categories = categoryService.getAllCategories();
        return ApiResponse.success(categories);
    }

    @GetMapping("/{id}")
    public ApiResponse<GarbageCategory> getCategoryById(@PathVariable Integer id) {
        GarbageCategory category = categoryService.getCategoryById(id);
        return ApiResponse.success(category);
    }

    @GetMapping("/code/{code}")
    public ApiResponse<GarbageCategory> getCategoryByCode(@PathVariable String code) {
        GarbageCategory category = categoryService.getCategoryByCode(code);
        return ApiResponse.success(category);
    }

    @PostMapping
    public ApiResponse<GarbageCategory> createCategory(@RequestBody GarbageCategory category) {
        GarbageCategory created = categoryService.createCategory(category);
        return ApiResponse.success("创建成功", created);
    }

    @PutMapping("/{id}")
    public ApiResponse<GarbageCategory> updateCategory(@PathVariable Integer id,
            @RequestBody GarbageCategory category) {
        GarbageCategory updated = categoryService.updateCategory(id, category);
        return ApiResponse.success("更新成功", updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return ApiResponse.success("删除成功", null);
    }
}