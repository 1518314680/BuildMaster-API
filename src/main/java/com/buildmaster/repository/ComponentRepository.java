package com.buildmaster.repository;

import com.buildmaster.model.Component;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComponentRepository extends JpaRepository<Component, Long> {
    
    List<Component> findByType(Component.ComponentType type);
    
    List<Component> findByIsAvailableTrue();
    
    List<Component> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT c FROM Component c WHERE c.type = :type AND c.isAvailable = true")
    List<Component> findAvailableByType(@Param("type") Component.ComponentType type);
    
    @Query("SELECT c FROM Component c WHERE c.price BETWEEN :minPrice AND :maxPrice AND c.isAvailable = true")
    List<Component> findByPriceRange(@Param("minPrice") java.math.BigDecimal minPrice, 
                                   @Param("maxPrice") java.math.BigDecimal maxPrice);
    
    // 传统 LIKE 查询（小数据量适用）
    @Query("SELECT c FROM Component c WHERE c.name LIKE %:keyword% OR c.description LIKE %:keyword%")
    List<Component> searchByKeyword(@Param("keyword") String keyword);
    
    // 全文索引查询（性能优化，适合大数据量）
    @Query(value = "SELECT * FROM component WHERE MATCH(name, brand, description) AGAINST(:keyword IN BOOLEAN MODE) AND is_available = TRUE", 
           nativeQuery = true)
    List<Component> searchByKeywordFulltext(@Param("keyword") String keyword);
    
    // 分页搜索（推荐使用）
    @Query("SELECT c FROM Component c WHERE (c.name LIKE %:keyword% OR c.description LIKE %:keyword% OR c.brand LIKE %:keyword%) AND c.isAvailable = true")
    Page<Component> searchByKeywordPage(@Param("keyword") String keyword, Pageable pageable);
    
    java.util.Optional<Component> findByNameAndBrand(String name, String brand);
}
