package com.zjfc.smartgarbage.repository;

import com.zjfc.smartgarbage.model.entity.GarbageCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface GarbageCategoryRepository extends JpaRepository<GarbageCategory, Integer> {

    Optional<GarbageCategory> findByCategoryCode(String categoryCode);

    Optional<GarbageCategory> findByCategoryName(String categoryName);
}