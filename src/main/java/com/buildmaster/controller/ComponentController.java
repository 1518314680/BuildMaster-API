package com.buildmaster.controller;

import com.buildmaster.model.Component;
import com.buildmaster.service.ComponentService;
import com.buildmaster.service.ComponentImportService;
import com.buildmaster.service.PriceUpdateScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/components")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "配件管理", description = "配件CRUD、搜索、批量导入等接口")
public class ComponentController {
    
    private final ComponentService componentService;
    private final ComponentImportService importService;
    private final PriceUpdateScheduler priceUpdateScheduler;
    
    @GetMapping
    public ResponseEntity<List<Component>> getAllComponents() {
        List<Component> components = componentService.getAllComponents();
        return ResponseEntity.ok(components);
    }
    
    @GetMapping("/page")
    public ResponseEntity<Page<Component>> getComponentsPage(Pageable pageable) {
        Page<Component> components = componentService.getComponentsPage(pageable);
        return ResponseEntity.ok(components);
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Component>> getComponentsByType(@PathVariable Component.ComponentType type) {
        List<Component> components = componentService.getComponentsByType(type);
        return ResponseEntity.ok(components);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Component>> searchComponents(@RequestParam String keyword) {
        List<Component> components = componentService.searchComponents(keyword);
        return ResponseEntity.ok(components);
    }
    
    @GetMapping("/search/page")
    @Operation(summary = "分页搜索配件", description = "支持关键词搜索配件名称、品牌、描述（分页）")
    public ResponseEntity<Page<Component>> searchComponentsPage(
            @RequestParam String keyword,
            Pageable pageable) {
        Page<Component> components = componentService.searchComponentsPage(keyword, pageable);
        return ResponseEntity.ok(components);
    }
    
    @GetMapping("/search/fulltext")
    @Operation(summary = "全文搜索配件", description = "使用全文索引进行高性能搜索（需先执行 performance_optimization.sql）")
    public ResponseEntity<List<Component>> searchComponentsFulltext(@RequestParam String keyword) {
        List<Component> components = componentService.searchComponentsFulltext(keyword);
        return ResponseEntity.ok(components);
    }
    
    @GetMapping("/price-range")
    public ResponseEntity<List<Component>> getComponentsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        List<Component> components = componentService.getComponentsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(components);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Component> getComponentById(@PathVariable Long id) {
        Optional<Component> component = componentService.getComponentById(id);
        return component.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Component> createComponent(@RequestBody Component component) {
        Component savedComponent = componentService.saveComponent(component);
        return ResponseEntity.ok(savedComponent);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Component> updateComponent(@PathVariable Long id, @RequestBody Component component) {
        component.setId(id);
        Component updatedComponent = componentService.saveComponent(component);
        return ResponseEntity.ok(updatedComponent);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComponent(@PathVariable Long id) {
        componentService.deleteComponent(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 爬取配件信息
     */
    @PostMapping("/crawl")
    @Operation(summary = "爬取配件信息", description = "从电商平台爬取配件数据（需谨慎使用）")
    public ResponseEntity<List<Component>> crawlComponents(
            @RequestParam String type,
            @RequestParam(required = false, defaultValue = "jd") String source,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "10") int maxCount) {
        
        // TODO: 注入 ComponentCrawlerService
        // List<Component> components = crawlerService.crawlComponents(type, source, keyword, maxCount);
        // return ResponseEntity.ok(components);
        
        // 暂时返回空列表
        return ResponseEntity.ok(List.of());
    }
    
    /**
     * 批量导入热门配件（预定义数据）
     */
    @PostMapping("/import/popular")
    @Operation(summary = "导入热门配件", description = "一键导入30+热门配件数据")
    public ResponseEntity<Map<String, Object>> importPopularComponents() {
        ComponentImportService.ImportResult result = importService.importPopularComponents();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", !result.isFailed());
        response.put("successCount", result.getSuccessCount());
        response.put("skippedCount", result.getSkippedCount());
        response.put("errorCount", result.getErrorCount());
        response.put("errors", result.getErrors());
        response.put("skipped", result.getSkipped());
        response.put("message", result.isFailed() ? result.getMessage() : "导入完成");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 从CSV文件批量导入
     */
    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "CSV批量导入", description = "上传CSV文件批量导入配件")
    public ResponseEntity<Map<String, Object>> importFromCSV(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "文件不能为空");
            return ResponseEntity.badRequest().body(error);
        }
        
        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "只支持CSV格式文件");
            return ResponseEntity.badRequest().body(error);
        }
        
        ComponentImportService.ImportResult result = importService.importFromCSV(file);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", !result.isFailed());
        response.put("successCount", result.getSuccessCount());
        response.put("skippedCount", result.getSkippedCount());
        response.put("errorCount", result.getErrorCount());
        response.put("errors", result.getErrors());
        response.put("skipped", result.getSkipped());
        response.put("message", result.isFailed() ? result.getMessage() : "导入完成");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 下载CSV模板
     */
    @GetMapping(value = "/import/template", produces = "text/csv")
    @Operation(summary = "下载CSV模板", description = "下载配件导入CSV模板文件")
    public ResponseEntity<String> downloadCSVTemplate() {
        String template = importService.generateCSVTemplate();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"component_template.csv\"")
                .body(template);
    }
    
    /**
     * 手动触发所有配件价格更新
     */
    @PostMapping("/update-prices")
    @Operation(summary = "更新所有价格", description = "手动触发所有配件价格更新（需要配置京东API）")
    public ResponseEntity<Map<String, Object>> updateAllPrices() {
        PriceUpdateScheduler.UpdateResult result = priceUpdateScheduler.updateAllPrices();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("successCount", result.successCount);
        response.put("failureCount", result.failureCount);
        response.put("skipCount", result.skipCount);
        response.put("totalCount", result.getTotalCount());
        response.put("message", "价格更新完成");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新单个配件价格
     */
    @PostMapping("/{id}/update-price")
    @Operation(summary = "更新单个配件价格", description = "手动触发单个配件价格更新")
    public ResponseEntity<Map<String, Object>> updateSinglePrice(@PathVariable Long id) {
        boolean success = priceUpdateScheduler.updateSinglePrice(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "价格更新成功" : "价格更新失败");
        
        if (!success) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 手动触发所有推广链接更新
     */
    @PostMapping("/update-promotion-urls")
    @Operation(summary = "更新所有推广链接", description = "手动触发所有配件推广链接更新（需要配置京东API）")
    public ResponseEntity<Map<String, Object>> updateAllPromotionUrls() {
        PriceUpdateScheduler.UpdateResult result = priceUpdateScheduler.updateAllPromotionUrls();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("successCount", result.successCount);
        response.put("failureCount", result.failureCount);
        response.put("skipCount", result.skipCount);
        response.put("totalCount", result.getTotalCount());
        response.put("message", "推广链接更新完成");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新单个配件推广链接
     */
    @PostMapping("/{id}/update-promotion-url")
    @Operation(summary = "更新单个配件推广链接", description = "手动触发单个配件推广链接更新")
    public ResponseEntity<Map<String, Object>> updateSinglePromotionUrl(@PathVariable Long id) {
        boolean success = priceUpdateScheduler.updateSinglePromotionUrl(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "推广链接更新成功" : "推广链接更新失败");
        
        if (!success) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 批量更新所有配件图片（从京东API获取）
     */
    @PostMapping("/update-images")
    @Operation(summary = "批量更新配件图片", description = "从京东API批量获取并更新所有配件的图片URL")
    public ResponseEntity<Map<String, Object>> updateAllImages() {
        ComponentService.ImageUpdateResult result = componentService.updateAllImagesFromJD();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", result.successCount > 0);
        response.put("successCount", result.successCount);
        response.put("failureCount", result.failureCount);
        response.put("skipCount", result.skipCount);
        response.put("message", result.message);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新单个配件图片（从京东API获取）
     */
    @PostMapping("/{id}/update-image")
    @Operation(summary = "更新单个配件图片", description = "从京东API获取并更新指定配件的图片URL")
    public ResponseEntity<Map<String, Object>> updateSingleImage(@PathVariable Long id) {
        boolean success = componentService.updateSingleImageFromJD(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "图片更新成功" : "图片更新失败（请检查是否有京东SKU ID）");
        
        if (!success) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
}
