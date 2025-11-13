package com.buildmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    
    // 基础统计
    private Long totalUsers;
    private Long totalComponents;
    private Long totalConfigs;
    private Long todayRegistrations;
    
    // 配件类型分布
    private Map<String, Long> componentTypeDistribution;
    
    // 价格区间分布
    private Map<String, Long> priceRangeDistribution;
    
    // 最近7天注册趋势
    private List<DailyStats> registrationTrend;
    
    // 最近7天配件添加趋势
    private List<DailyStats> componentTrend;
    
    // 热门品牌TOP5
    private List<BrandStats> topBrands;
    
    // 库存统计
    private StockStats stockStats;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyStats {
        private String date;
        private Long count;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandStats {
        private String brand;
        private Long count;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockStats {
        private Long inStock;      // 有库存
        private Long lowStock;     // 库存不足（<10）
        private Long outOfStock;   // 缺货
    }
}

