package com.zjfc.smartgarbage.service;

import com.zjfc.smartgarbage.model.entity.GarbageCategory;
import java.util.List;

public interface GarbageCategoryService {

    
    List<GarbageCategory> getAllCategories();

    GarbageCategory getCategoryById(Integer categoryId);

    GarbageCategory getCategoryByCode(String categoryCode);

    GarbageCategory createCategory(GarbageCategory category);

    GarbageCategory updateCategory(Integer categoryId, GarbageCategory category);

    void deleteCategory(Integer categoryId);
}