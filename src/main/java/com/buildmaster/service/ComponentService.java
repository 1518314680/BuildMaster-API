package com.buildmaster.service;

import com.buildmaster.entity.Component;
import com.buildmaster.repository.ComponentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComponentService {
    
    private final ComponentRepository componentRepository;
    
    @Autowired(required = false)
    private JDApiService jdApiService;
    
    public List<Component> getAllComponents() {
        return componentRepository.findByIsAvailableTrue();
    }
    
    @Cacheable(value = "components", key = "#type")
    public List<Component> getComponentsByType(Component.ComponentType type) {
        return componentRepository.findAvailableByType(type);
    }
    
    public List<Component> searchComponents(String keyword) {
        return componentRepository.searchByKeyword(keyword);
    }
    
    /**
     * 搜索配件（分页，推荐使用）
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 分页结果
     */
    public Page<Component> searchComponentsPage(String keyword, Pageable pageable) {
        return componentRepository.searchByKeywordPage(keyword, pageable);
    }
    
    /**
     * 搜索配件（全文索引，性能优化）
     * 注意：需要先执行 sql/performance_optimization.sql 创建全文索引
     * @param keyword 关键词
     * @return 配件列表
     */
    public List<Component> searchComponentsFulltext(String keyword) {
        try {
            return componentRepository.searchByKeywordFulltext(keyword);
        } catch (Exception e) {
            log.warn("全文索引查询失败，降级为 LIKE 查询: {}", e.getMessage());
            // 降级为普通 LIKE 查询
            return componentRepository.searchByKeyword(keyword);
        }
    }
    
    public List<Component> getComponentsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return componentRepository.findByPriceRange(minPrice, maxPrice);
    }
    
    public Optional<Component> getComponentById(Long id) {
        return componentRepository.findById(id);
    }
    
    public Component saveComponent(Component component) {
        return componentRepository.save(component);
    }
    
    public void deleteComponent(Long id) {
        componentRepository.deleteById(id);
    }
    
    public Page<Component> getComponentsPage(Pageable pageable) {
        return componentRepository.findAll(pageable);
    }
    
    /**
     * 从京东API批量更新配件图片
     * @return 更新结果统计
     */
    @Transactional
    public ImageUpdateResult updateAllImagesFromJD() {
        if (jdApiService == null) {
            log.warn("京东API服务未配置，无法更新图片");
            return new ImageUpdateResult(0, 0, 0, "京东API服务未配置");
        }
        
        List<Component> components = componentRepository.findAll();
        int successCount = 0;
        int failureCount = 0;
        int skipCount = 0;
        
        for (Component component : components) {
            // 跳过已有图片的配件
            if (component.getImageUrl() != null && !component.getImageUrl().isEmpty() 
                && !component.getImageUrl().contains("placeholder")) {
                skipCount++;
                continue;
            }
            
            // 跳过没有京东SKU ID的配件
            if (component.getJdSkuId() == null || component.getJdSkuId().isEmpty()) {
                skipCount++;
                continue;
            }
            
            try {
                log.info("获取配件图片: {} (SKU: {})", component.getName(), component.getJdSkuId());
                
                // 调用京东API获取商品详情
                Component jdProduct = jdApiService.getProductDetail(component.getJdSkuId());
                
                if (jdProduct != null && jdProduct.getImageUrl() != null && !jdProduct.getImageUrl().isEmpty()) {
                    component.setImageUrl(jdProduct.getImageUrl());
                    componentRepository.save(component);
                    
                    log.info("✅ 更新成功: {} -> {}", component.getName(), jdProduct.getImageUrl());
                    successCount++;
                } else {
                    log.warn("❌ 未获取到图片: {}", component.getName());
                    failureCount++;
                }
                
                // 避免API请求过快，添加延迟
                Thread.sleep(200);
                
            } catch (Exception e) {
                log.error("更新配件图片失败: {}", component.getName(), e);
                failureCount++;
            }
        }
        
        String message = String.format("图片更新完成 - 成功: %d, 失败: %d, 跳过: %d", 
            successCount, failureCount, skipCount);
        
        log.info(message);
        return new ImageUpdateResult(successCount, failureCount, skipCount, message);
    }
    
    /**
     * 更新单个配件的图片
     * @param id 配件ID
     * @return 是否成功
     */
    @Transactional
    public boolean updateSingleImageFromJD(Long id) {
        if (jdApiService == null) {
            log.warn("京东API服务未配置");
            return false;
        }
        
        Optional<Component> optionalComponent = componentRepository.findById(id);
        if (optionalComponent.isEmpty()) {
            log.warn("配件不存在: {}", id);
            return false;
        }
        
        Component component = optionalComponent.get();
        
        if (component.getJdSkuId() == null || component.getJdSkuId().isEmpty()) {
            log.warn("配件没有京东SKU ID: {}", component.getName());
            return false;
        }
        
        try {
            Component jdProduct = jdApiService.getProductDetail(component.getJdSkuId());
            
            if (jdProduct != null && jdProduct.getImageUrl() != null) {
                component.setImageUrl(jdProduct.getImageUrl());
                componentRepository.save(component);
                
                log.info("更新配件图片成功: {} -> {}", component.getName(), jdProduct.getImageUrl());
                return true;
            }
        } catch (Exception e) {
            log.error("更新配件图片失败: {}", component.getName(), e);
        }
        
        return false;
    }
    
    /**
     * 图片更新结果
     */
    public static class ImageUpdateResult {
        public final int successCount;
        public final int failureCount;
        public final int skipCount;
        public final String message;
        
        public ImageUpdateResult(int successCount, int failureCount, int skipCount, String message) {
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.skipCount = skipCount;
            this.message = message;
        }
    }
}
