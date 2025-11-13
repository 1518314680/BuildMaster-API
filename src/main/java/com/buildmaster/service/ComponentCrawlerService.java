package com.buildmaster.service;

import com.buildmaster.model.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 配件信息爬虫服务
 * 注意：这是一个模拟实现，实际生产环境中需要真实的爬虫逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComponentCrawlerService {

    private final ComponentService componentService;
    private final Random random = new Random();

    /**
     * 爬取配件信息
     *
     * @param type     配件类型
     * @param source   数据来源（jd/tmall/taobao）
     * @param keyword  搜索关键词
     * @param maxCount 最大爬取数量
     * @return 爬取到的配件列表
     */
    public List<Component> crawlComponents(String type, String source, String keyword, int maxCount) {
        log.info("开始爬取配件: type={}, source={}, keyword={}, maxCount={}", type, source, keyword, maxCount);

        List<Component> crawledComponents = new ArrayList<>();

        try {
            // 模拟爬取过程
            // 实际生产环境中，这里应该调用真实的爬虫逻辑
            // 可以使用 Jsoup、Selenium、HttpClient 等工具

            for (int i = 0; i < maxCount; i++) {
                Component component = generateMockComponent(type, source, keyword, i);
                
                // 保存到数据库
                Component saved = componentService.saveComponent(component);
                crawledComponents.add(saved);

                // 模拟爬取延迟
                Thread.sleep(100);
            }

            log.info("爬取完成，共获取 {} 个配件", crawledComponents.size());
        } catch (Exception e) {
            log.error("爬取配件失败", e);
        }

        return crawledComponents;
    }

    /**
     * 生成模拟配件数据
     * 实际生产中应该替换为真实的爬虫逻辑
     */
    private Component generateMockComponent(String type, String source, String keyword, int index) {
        Component component = new Component();

        // 设置基本信息
        component.setType(Component.ComponentType.valueOf(type));

        // 根据类型生成品牌和型号
        String[] brands = getBrandsByType(type);
        String brand = brands[random.nextInt(brands.length)];
        component.setBrand(brand);

        String model = generateModel(type, brand, index);
        component.setModel(model);

        // 生成名称
        String name = brand + " " + model;
        if (keyword != null && !keyword.isEmpty()) {
            name = keyword + " " + name;
        }
        component.setName(name);

        // 生成价格（根据类型设置价格范围）
        BigDecimal price = generatePrice(type);
        component.setPrice(price);

        // 设置库存和可用性
        component.setStockQuantity(random.nextInt(100) + 10);
        component.setIsAvailable(true);

        // 设置描述
        component.setDescription(String.format("从%s爬取的%s配件", getSourceName(source), type));

        // 注意：实际爬取中还应该获取图片URL
        // component.setImageUrl(crawledImageUrl);

        log.debug("生成模拟配件: {}", component.getName());
        return component;
    }

    /**
     * 根据类型获取品牌列表
     */
    private String[] getBrandsByType(String type) {
        return switch (type) {
            case "CPU" -> new String[]{"Intel", "AMD"};
            case "GPU" -> new String[]{"NVIDIA", "AMD", "Intel"};
            case "MOTHERBOARD" -> new String[]{"ASUS", "MSI", "Gigabyte", "ASRock"};
            case "RAM" -> new String[]{"Corsair", "G.Skill", "Kingston", "Crucial"};
            case "STORAGE" -> new String[]{"Samsung", "WD", "Seagate", "Kingston"};
            case "PSU" -> new String[]{"Corsair", "EVGA", "Seasonic", "Thermaltake"};
            case "CASE" -> new String[]{"NZXT", "Corsair", "Fractal Design", "Cooler Master"};
            case "COOLER" -> new String[]{"Noctua", "Cooler Master", "be quiet!", "Arctic"};
            default -> new String[]{"Generic"};
        };
    }

    /**
     * 生成型号
     */
    private String generateModel(String type, String brand, int index) {
        return switch (type) {
            case "CPU" -> brand.equals("Intel") ? 
                String.format("Core i%d-%d00K", random.nextInt(3) + 5, 130 + index) :
                String.format("Ryzen %d %d00X", random.nextInt(3) + 5, 7000 + index * 100);
            case "GPU" -> brand.equals("NVIDIA") ?
                String.format("RTX %d", 4060 + index * 10) :
                String.format("RX %d", 7600 + index * 100);
            case "RAM" -> String.format("DDR%d %dMHz %dGB", random.nextInt(2) + 4, 3200 + index * 400, 8 * (1 << random.nextInt(3)));
            case "STORAGE" -> String.format("%dGB %s", 256 * (1 << random.nextInt(4)), random.nextBoolean() ? "NVMe SSD" : "SATA SSD");
            case "PSU" -> String.format("%dW %s", 500 + index * 100, random.nextBoolean() ? "80+ Gold" : "80+ Bronze");
            case "MOTHERBOARD" -> String.format("%s-%d", brand, 600 + index);
            case "CASE" -> String.format("Model-%d", 100 + index);
            case "COOLER" -> String.format("Cool-%d", 100 + index);
            default -> String.format("Model-%d", index);
        };
    }

    /**
     * 生成价格
     */
    private BigDecimal generatePrice(String type) {
        int basePrice = switch (type) {
            case "CPU" -> 1000 + random.nextInt(3000);
            case "GPU" -> 2000 + random.nextInt(5000);
            case "MOTHERBOARD" -> 800 + random.nextInt(1500);
            case "RAM" -> 300 + random.nextInt(700);
            case "STORAGE" -> 400 + random.nextInt(1000);
            case "PSU" -> 300 + random.nextInt(800);
            case "CASE" -> 200 + random.nextInt(600);
            case "COOLER" -> 100 + random.nextInt(500);
            default -> 500;
        };
        return new BigDecimal(basePrice);
    }

    /**
     * 获取数据源名称
     */
    private String getSourceName(String source) {
        return switch (source) {
            case "jd" -> "京东";
            case "tmall" -> "天猫";
            case "taobao" -> "淘宝";
            default -> source;
        };
    }
}

