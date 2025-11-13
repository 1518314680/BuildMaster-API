package com.buildmaster.service;

import com.buildmaster.dto.DashboardStatsDTO;
import com.buildmaster.model.Component;
import com.buildmaster.repository.BuildConfigRepository;
import com.buildmaster.repository.ComponentRepository;
import com.buildmaster.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final ComponentRepository componentRepository;
    private final BuildConfigRepository buildConfigRepository;

    /**
     * 获取仪表盘统计数据
     */
    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // 基础统计
        stats.setTotalUsers(userRepository.count());
        stats.setTotalComponents(componentRepository.count());
        stats.setTotalConfigs(buildConfigRepository.count());
        stats.setTodayRegistrations(getTodayRegistrations());

        // 配件类型分布
        stats.setComponentTypeDistribution(getComponentTypeDistribution());

        // 价格区间分布
        stats.setPriceRangeDistribution(getPriceRangeDistribution());

        // 最近7天注册趋势
        stats.setRegistrationTrend(getRegistrationTrend());

        // 最近7天配件添加趋势
        stats.setComponentTrend(getComponentTrend());

        // 热门品牌TOP5
        stats.setTopBrands(getTopBrands());

        // 库存统计
        stats.setStockStats(getStockStats());

        return stats;
    }

    /**
     * 获取今日注册用户数
     */
    private Long getTodayRegistrations() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return userRepository.findAll().stream()
                .filter(user -> user.getCreatedAt() != null && user.getCreatedAt().isAfter(startOfDay))
                .count();
    }

    /**
     * 获取配件类型分布
     */
    private Map<String, Long> getComponentTypeDistribution() {
        List<Component> components = componentRepository.findAll();
        return components.stream()
                .collect(Collectors.groupingBy(
                        component -> component.getType().name(),
                        Collectors.counting()
                ));
    }

    /**
     * 获取价格区间分布
     */
    private Map<String, Long> getPriceRangeDistribution() {
        List<Component> components = componentRepository.findAll();
        Map<String, Long> distribution = new LinkedHashMap<>();

        distribution.put("0-500", components.stream()
                .filter(c -> c.getPrice().doubleValue() < 500).count());
        distribution.put("500-1000", components.stream()
                .filter(c -> c.getPrice().doubleValue() >= 500 && c.getPrice().doubleValue() < 1000).count());
        distribution.put("1000-2000", components.stream()
                .filter(c -> c.getPrice().doubleValue() >= 1000 && c.getPrice().doubleValue() < 2000).count());
        distribution.put("2000-5000", components.stream()
                .filter(c -> c.getPrice().doubleValue() >= 2000 && c.getPrice().doubleValue() < 5000).count());
        distribution.put("5000+", components.stream()
                .filter(c -> c.getPrice().doubleValue() >= 5000).count());

        return distribution;
    }

    /**
     * 获取最近7天注册趋势
     */
    private List<DashboardStatsDTO.DailyStats> getRegistrationTrend() {
        List<DashboardStatsDTO.DailyStats> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

            long count = userRepository.findAll().stream()
                    .filter(user -> user.getCreatedAt() != null &&
                            user.getCreatedAt().isAfter(startOfDay) &&
                            user.getCreatedAt().isBefore(endOfDay))
                    .count();

            trend.add(new DashboardStatsDTO.DailyStats(date.format(formatter), count));
        }

        return trend;
    }

    /**
     * 获取最近7天配件添加趋势
     */
    private List<DashboardStatsDTO.DailyStats> getComponentTrend() {
        List<DashboardStatsDTO.DailyStats> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

            long count = componentRepository.findAll().stream()
                    .filter(component -> component.getCreatedAt() != null &&
                            component.getCreatedAt().isAfter(startOfDay) &&
                            component.getCreatedAt().isBefore(endOfDay))
                    .count();

            trend.add(new DashboardStatsDTO.DailyStats(date.format(formatter), count));
        }

        return trend;
    }

    /**
     * 获取热门品牌TOP5
     */
    private List<DashboardStatsDTO.BrandStats> getTopBrands() {
        List<Component> components = componentRepository.findAll();
        
        return components.stream()
                .filter(c -> c.getBrand() != null && !c.getBrand().isEmpty())
                .collect(Collectors.groupingBy(Component::getBrand, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> new DashboardStatsDTO.BrandStats(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 获取库存统计
     */
    private DashboardStatsDTO.StockStats getStockStats() {
        List<Component> components = componentRepository.findAll();

        long inStock = components.stream()
                .filter(c -> c.getStockQuantity() != null && c.getStockQuantity() >= 10)
                .count();

        long lowStock = components.stream()
                .filter(c -> c.getStockQuantity() != null && 
                        c.getStockQuantity() > 0 && c.getStockQuantity() < 10)
                .count();

        long outOfStock = components.stream()
                .filter(c -> c.getStockQuantity() == null || c.getStockQuantity() == 0)
                .count();

        return new DashboardStatsDTO.StockStats(inStock, lowStock, outOfStock);
    }
}

