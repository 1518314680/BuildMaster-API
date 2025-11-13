package com.buildmaster.service;

import com.buildmaster.model.Component;
import com.buildmaster.repository.ComponentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 价格更新定时任务服务
 * 
 * 功能：
 * 1. 定时更新所有配件的价格
 * 2. 定时更新推广链接
 * 3. 提供手动触发更新的方法
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriceUpdateScheduler {

    private final ComponentRepository componentRepository;
    private final JDApiService jdApiService;

    /**
     * 定时更新价格（每2小时执行一次）
     * cron表达式: 0 0 *\\2 * * ? 表示每2小时的整点执行
     */
    @Scheduled(cron = "${price-update.cron:0 0 */2 * * ?}")
    @Transactional
    public void scheduledPriceUpdate() {
        if (!jdApiService.isApiConfigured()) {
            log.warn("京东API未配置，跳过价格更新");
            return;
        }

        log.info("开始定时更新价格...");
        
        try {
            UpdateResult result = updateAllPrices();
            log.info("价格更新完成 - 成功: {}, 失败: {}, 跳过: {}", 
                    result.successCount, result.failureCount, result.skipCount);
        } catch (Exception e) {
            log.error("定时价格更新失败", e);
        }
    }

    /**
     * 定时更新推广链接（每天凌晨3点执行一次）
     * cron表达式: 0 0 3 * * ? 表示每天凌晨3点执行
     */
    @Scheduled(cron = "${promotion-url-update.cron:0 0 3 * * ?}")
    @Transactional
    public void scheduledPromotionUrlUpdate() {
        if (!jdApiService.isApiConfigured()) {
            log.warn("京东API未配置，跳过推广链接更新");
            return;
        }

        log.info("开始定时更新推广链接...");
        
        try {
            UpdateResult result = updateAllPromotionUrls();
            log.info("推广链接更新完成 - 成功: {}, 失败: {}, 跳过: {}", 
                    result.successCount, result.failureCount, result.skipCount);
        } catch (Exception e) {
            log.error("定时推广链接更新失败", e);
        }
    }

    /**
     * 更新所有配件价格
     * 
     * @return 更新结果
     */
    public UpdateResult updateAllPrices() {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger skipCount = new AtomicInteger(0);

        // 获取所有有京东SKU ID的配件
        List<Component> components = componentRepository.findAll().stream()
                .filter(c -> c.getJdSkuId() != null && !c.getJdSkuId().isEmpty())
                .collect(Collectors.toList());

        if (components.isEmpty()) {
            log.warn("没有找到需要更新价格的配件");
            return new UpdateResult(0, 0, 0);
        }

        log.info("找到 {} 个配件需要更新价格", components.size());

        // 批量查询价格
        List<String> skuIds = components.stream()
                .map(Component::getJdSkuId)
                .collect(Collectors.toList());

        Map<String, BigDecimal> prices = jdApiService.batchGetPrices(skuIds);

        // 更新价格
        for (Component component : components) {
            try {
                String skuId = component.getJdSkuId();
                BigDecimal newPrice = prices.get(skuId);

                if (newPrice != null && newPrice.compareTo(BigDecimal.ZERO) > 0) {
                    // 保存旧价格作为原价
                    if (component.getOriginalPrice() == null) {
                        component.setOriginalPrice(component.getPrice());
                    }

                    component.setPrice(newPrice);
                    component.setPriceUpdatedAt(LocalDateTime.now());
                    componentRepository.save(component);

                    log.debug("更新配件价格成功: {} - {}", component.getName(), newPrice);
                    successCount.incrementAndGet();
                } else {
                    log.debug("配件价格未变化，跳过: {}", component.getName());
                    skipCount.incrementAndGet();
                }
            } catch (Exception e) {
                log.error("更新配件价格失败: {}", component.getName(), e);
                failureCount.incrementAndGet();
            }
        }

        return new UpdateResult(successCount.get(), failureCount.get(), skipCount.get());
    }

    /**
     * 更新所有推广链接
     * 
     * @return 更新结果
     */
    public UpdateResult updateAllPromotionUrls() {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger skipCount = new AtomicInteger(0);

        // 获取所有有京东SKU ID的配件
        List<Component> components = componentRepository.findAll().stream()
                .filter(c -> c.getJdSkuId() != null && !c.getJdSkuId().isEmpty())
                .collect(Collectors.toList());

        if (components.isEmpty()) {
            log.warn("没有找到需要更新推广链接的配件");
            return new UpdateResult(0, 0, 0);
        }

        log.info("找到 {} 个配件需要更新推广链接", components.size());

        // 逐个生成推广链接（避免频率限制）
        for (Component component : components) {
            try {
                String skuId = component.getJdSkuId();
                String promotionUrl = jdApiService.generatePromotionUrl(skuId);

                if (promotionUrl != null && !promotionUrl.isEmpty()) {
                    component.setPurchaseUrl(promotionUrl);
                    componentRepository.save(component);

                    log.debug("更新推广链接成功: {}", component.getName());
                    successCount.incrementAndGet();

                    // 延迟避免频率限制
                    Thread.sleep(1000);
                } else {
                    log.debug("推广链接生成失败，跳过: {}", component.getName());
                    skipCount.incrementAndGet();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("推广链接更新被中断");
                break;
            } catch (Exception e) {
                log.error("更新推广链接失败: {}", component.getName(), e);
                failureCount.incrementAndGet();
            }
        }

        return new UpdateResult(successCount.get(), failureCount.get(), skipCount.get());
    }

    /**
     * 更新单个配件价格
     * 
     * @param componentId 配件ID
     * @return 是否成功
     */
    @Transactional
    public boolean updateSinglePrice(Long componentId) {
        try {
            Component component = componentRepository.findById(componentId)
                    .orElseThrow(() -> new RuntimeException("配件不存在"));

            if (component.getJdSkuId() == null || component.getJdSkuId().isEmpty()) {
                log.warn("配件没有京东SKU ID: {}", component.getName());
                return false;
            }

            BigDecimal newPrice = jdApiService.getPrice(component.getJdSkuId());

            if (newPrice != null && newPrice.compareTo(BigDecimal.ZERO) > 0) {
                if (component.getOriginalPrice() == null) {
                    component.setOriginalPrice(component.getPrice());
                }

                component.setPrice(newPrice);
                component.setPriceUpdatedAt(LocalDateTime.now());
                componentRepository.save(component);

                log.info("更新配件价格成功: {} - {}", component.getName(), newPrice);
                return true;
            }

            return false;

        } catch (Exception e) {
            log.error("更新单个配件价格失败: {}", componentId, e);
            return false;
        }
    }

    /**
     * 更新单个配件推广链接
     * 
     * @param componentId 配件ID
     * @return 是否成功
     */
    @Transactional
    public boolean updateSinglePromotionUrl(Long componentId) {
        try {
            Component component = componentRepository.findById(componentId)
                    .orElseThrow(() -> new RuntimeException("配件不存在"));

            if (component.getJdSkuId() == null || component.getJdSkuId().isEmpty()) {
                log.warn("配件没有京东SKU ID: {}", component.getName());
                return false;
            }

            String promotionUrl = jdApiService.generatePromotionUrl(component.getJdSkuId());

            if (promotionUrl != null && !promotionUrl.isEmpty()) {
                component.setPurchaseUrl(promotionUrl);
                componentRepository.save(component);

                log.info("更新推广链接成功: {}", component.getName());
                return true;
            }

            return false;

        } catch (Exception e) {
            log.error("更新单个配件推广链接失败: {}", componentId, e);
            return false;
        }
    }

    /**
     * 更新结果统计
     */
    public static class UpdateResult {
        public final int successCount;
        public final int failureCount;
        public final int skipCount;

        public UpdateResult(int successCount, int failureCount, int skipCount) {
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.skipCount = skipCount;
        }

        public int getTotalCount() {
            return successCount + failureCount + skipCount;
        }

        @Override
        public String toString() {
            return String.format("UpdateResult{success=%d, failure=%d, skip=%d, total=%d}",
                    successCount, failureCount, skipCount, getTotalCount());
        }
    }
}

