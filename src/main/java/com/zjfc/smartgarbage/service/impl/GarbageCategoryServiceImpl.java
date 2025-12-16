package com.zjfc.smartgarbage.service.impl;

import com.zjfc.smartgarbage.model.entity.GarbageCategory;
import com.zjfc.smartgarbage.repository.GarbageCategoryRepository;
import com.zjfc.smartgarbage.service.GarbageCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GarbageCategoryServiceImpl implements GarbageCategoryService {
    
    @Autowired
    private GarbageCategoryRepository categoryRepository;
    
    @Override
    public List<GarbageCategory> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    @Override
    public GarbageCategory getCategoryById(Integer categoryId) {
        Optional<GarbageCategory> optional = categoryRepository.findById(categoryId);
        return optional.orElseThrow(() -> new RuntimeException("垃圾分类类别不存在"));
    }
    
    @Override
    public GarbageCategory getCategoryByCode(String categoryCode) {
        Optional<GarbageCategory> optional = categoryRepository.findByCategoryCode(categoryCode);
        return optional.orElseThrow(() -> new RuntimeException("垃圾分类类别不存在"));
    }
    
    @Override
    public GarbageCategory createCategory(GarbageCategory category) {
        // 检查类别编码是否已存在
        if (categoryRepository.findByCategoryCode(category.getCategoryCode()).isPresent()) {
            throw new RuntimeException("类别编码已存在");
        }
        
        return categoryRepository.save(category);
    }
    
    @Override
    public GarbageCategory updateCategory(Integer categoryId, GarbageCategory category) {
        GarbageCategory existingCategory = getCategoryById(categoryId);
        
        // 更新字段
        if (category.getCategoryName() != null) {
            existingCategory.setCategoryName(category.getCategoryName());
        }
        if (category.getColor() != null) {
            existingCategory.setColor(category.getColor());
        }
        if (category.getDescription() != null) {
            existingCategory.setDescription(category.getDescription());
        }
        if (category.getDisposalGuide() != null) {
            existingCategory.setDisposalGuide(category.getDisposalGuide());
        }
        if (category.getExamples() != null) {
            existingCategory.setExamples(category.getExamples());
        }
        if (category.getSortOrder() != null) {
            existingCategory.setSortOrder(category.getSortOrder());
        }
        
        return categoryRepository.save(existingCategory);
    }
    
    @Override
    public void deleteCategory(Integer categoryId) {
        categoryRepository.deleteById(categoryId);
    }
}