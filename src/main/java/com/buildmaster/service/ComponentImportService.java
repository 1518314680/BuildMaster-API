package com.buildmaster.service;

import com.buildmaster.model.Component;
import com.buildmaster.repository.ComponentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 配件批量导入服务
 * 支持CSV、Excel格式批量导入配件数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComponentImportService {

    private final ComponentRepository componentRepository;

    /**
     * 从CSV文件导入配件
     * 
     * CSV格式:
     * 名称,类型,品牌,型号,价格,规格说明,图片URL,库存,京东SKU
     * Intel Core i7-13700K,CPU,Intel,i7-13700K,2599.00,"16核24线程",https://...,50,100012345
     */
    @Transactional
    public ImportResult importFromCSV(MultipartFile file) {
        ImportResult result = new ImportResult();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), "UTF-8"))) {
            
            // 跳过标题行
            String line = reader.readLine();
            int lineNumber = 1;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                try {
                    Component component = parseCSVLine(line);
                    
                    // 检查是否已存在
                    if (componentRepository.findByNameAndBrand(
                            component.getName(), component.getBrand()).isPresent()) {
                        result.addSkipped(lineNumber, "配件已存在: " + component.getName());
                        continue;
                    }
                    
                    // 保存配件
                    componentRepository.save(component);
                    result.addSuccess(lineNumber);
                    
                } catch (Exception e) {
                    result.addError(lineNumber, e.getMessage());
                    log.error("解析CSV第{}行失败", lineNumber, e);
                }
            }
            
            log.info("CSV导入完成: 成功 {}, 跳过 {}, 失败 {}", 
                result.getSuccessCount(), result.getSkippedCount(), result.getErrorCount());
            
        } catch (Exception e) {
            log.error("CSV文件读取失败", e);
            result.setFailed(true);
            result.setMessage("文件读取失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 解析CSV行
     */
    private Component parseCSVLine(String line) {
        String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // 处理包含逗号的字段
        
        if (fields.length < 6) {
            throw new IllegalArgumentException("CSV格式错误，字段数不足");
        }
        
        Component component = new Component();
        component.setName(cleanField(fields[0]));
        component.setType(Component.ComponentType.valueOf(cleanField(fields[1])));
        component.setBrand(cleanField(fields[2]));
        component.setModel(cleanField(fields[3]));
        component.setPrice(new BigDecimal(cleanField(fields[4])));
        component.setSpecs(cleanField(fields[5]));
        
        if (fields.length > 6 && !fields[6].isEmpty()) {
            component.setImageUrl(cleanField(fields[6]));
        }
        
        if (fields.length > 7 && !fields[7].isEmpty()) {
            component.setStockQuantity(Integer.parseInt(cleanField(fields[7])));
        }
        
        if (fields.length > 8 && !fields[8].isEmpty()) {
            component.setJdSkuId(cleanField(fields[8]));
        }
        
        component.setCreatedAt(LocalDateTime.now());
        
        return component;
    }

    /**
     * 清理字段（去除引号和空格）
     */
    private String cleanField(String field) {
        if (field == null) {
            return "";
        }
        return field.trim().replaceAll("^\"|\"$", "");
    }

    /**
     * 生成示例CSV模板
     */
    public String generateCSVTemplate() {
        StringBuilder csv = new StringBuilder();
        csv.append("名称,类型,品牌,型号,价格,规格说明,图片URL,库存,京东SKU\n");
        csv.append("Intel Core i7-13700K,CPU,Intel,i7-13700K,2599.00,\"16核24线程，主频3.4GHz\",https://example.com/cpu.jpg,50,100012345\n");
        csv.append("NVIDIA GeForce RTX 4070,GPU,NVIDIA,RTX 4070,4599.00,\"12GB GDDR6X显存\",https://example.com/gpu.jpg,30,100012346\n");
        csv.append("技嘉 B760M AORUS ELITE,MOTHERBOARD,Gigabyte,B760M AORUS ELITE,899.00,\"支持DDR5，M.2接口\",https://example.com/mb.jpg,20,100012347\n");
        return csv.toString();
    }

    /**
     * 从预定义列表导入热门配件
     */
    @Transactional
    public ImportResult importPopularComponents() {
        ImportResult result = new ImportResult();
        List<Component> popularComponents = getPopularComponentsList();
        
        int lineNumber = 0;
        for (Component component : popularComponents) {
            lineNumber++;
            
            try {
                // 检查是否已存在
                if (componentRepository.findByNameAndBrand(
                        component.getName(), component.getBrand()).isPresent()) {
                    result.addSkipped(lineNumber, "配件已存在");
                    continue;
                }
                
                componentRepository.save(component);
                result.addSuccess(lineNumber);
                
            } catch (Exception e) {
                result.addError(lineNumber, e.getMessage());
                log.error("导入预定义配件失败", e);
            }
        }
        
        log.info("热门配件导入完成: 成功 {}, 跳过 {}, 失败 {}", 
            result.getSuccessCount(), result.getSkippedCount(), result.getErrorCount());
        
        return result;
    }

    /**
     * 获取热门配件列表
     */
    private List<Component> getPopularComponentsList() {
        List<Component> list = new ArrayList<>();
        
        // CPU - Intel
        list.add(createComponent("Intel Core i9-14900K", Component.ComponentType.CPU, "Intel", "i9-14900K", 
            new BigDecimal("4299.00"), "24核32线程，主频3.2GHz，睿频6.0GHz", 50));
        list.add(createComponent("Intel Core i7-14700K", Component.ComponentType.CPU, "Intel", "i7-14700K", 
            new BigDecimal("3099.00"), "20核28线程，主频3.4GHz，睿频5.6GHz", 80));
        list.add(createComponent("Intel Core i5-14600K", Component.ComponentType.CPU, "Intel", "i5-14600K", 
            new BigDecimal("2099.00"), "14核20线程，主频3.5GHz，睿频5.3GHz", 120));
        
        // CPU - AMD
        list.add(createComponent("AMD Ryzen 9 7950X", Component.ComponentType.CPU, "AMD", "7950X", 
            new BigDecimal("3899.00"), "16核32线程，主频4.5GHz，加速5.7GHz", 60));
        list.add(createComponent("AMD Ryzen 7 7800X3D", Component.ComponentType.CPU, "AMD", "7800X3D", 
            new BigDecimal("2799.00"), "8核16线程，3D V-Cache技术", 90));
        list.add(createComponent("AMD Ryzen 5 7600X", Component.ComponentType.CPU, "AMD", "7600X", 
            new BigDecimal("1599.00"), "6核12线程，主频4.7GHz", 150));
        
        // GPU - NVIDIA
        list.add(createComponent("NVIDIA GeForce RTX 4090", Component.ComponentType.GPU, "NVIDIA", "RTX 4090", 
            new BigDecimal("12999.00"), "24GB GDDR6X显存，16384个CUDA核心", 20));
        list.add(createComponent("NVIDIA GeForce RTX 4080", Component.ComponentType.GPU, "NVIDIA", "RTX 4080", 
            new BigDecimal("8999.00"), "16GB GDDR6X显存，9728个CUDA核心", 40));
        list.add(createComponent("NVIDIA GeForce RTX 4070 Ti", Component.ComponentType.GPU, "NVIDIA", "RTX 4070 Ti", 
            new BigDecimal("5999.00"), "12GB GDDR6X显存，7680个CUDA核心", 60));
        list.add(createComponent("NVIDIA GeForce RTX 4060 Ti", Component.ComponentType.GPU, "NVIDIA", "RTX 4060 Ti", 
            new BigDecimal("3199.00"), "8GB GDDR6显存，4352个CUDA核心", 100));
        
        // GPU - AMD
        list.add(createComponent("AMD Radeon RX 7900 XTX", Component.ComponentType.GPU, "AMD", "RX 7900 XTX", 
            new BigDecimal("7499.00"), "24GB GDDR6显存，6144个流处理器", 30));
        list.add(createComponent("AMD Radeon RX 7800 XT", Component.ComponentType.GPU, "AMD", "RX 7800 XT", 
            new BigDecimal("4299.00"), "16GB GDDR6显存，3840个流处理器", 50));
        
        // 主板 - Intel
        list.add(createComponent("华硕 ROG STRIX Z790-E GAMING", Component.ComponentType.MOTHERBOARD, "ASUS", "Z790-E", 
            new BigDecimal("2999.00"), "支持DDR5，PCIe 5.0，WiFi 6E", 40));
        list.add(createComponent("微星 MAG B760M MORTAR", Component.ComponentType.MOTHERBOARD, "MSI", "B760M MORTAR", 
            new BigDecimal("1299.00"), "支持DDR5，M.2 Gen4", 80));
        
        // 主板 - AMD
        list.add(createComponent("华硕 ROG STRIX X670E-E GAMING", Component.ComponentType.MOTHERBOARD, "ASUS", "X670E-E", 
            new BigDecimal("3499.00"), "支持DDR5，PCIe 5.0，WiFi 6E", 35));
        list.add(createComponent("技嘉 B650 AORUS ELITE AX", Component.ComponentType.MOTHERBOARD, "Gigabyte", "B650 AORUS", 
            new BigDecimal("1599.00"), "支持DDR5，WiFi 6", 70));
        
        // 内存
        list.add(createComponent("金士顿 DDR5 6000MHz 32GB (16GBx2)", Component.ComponentType.MEMORY, "Kingston", "FURY Beast DDR5", 
            new BigDecimal("899.00"), "DDR5 6000MHz，CL36，RGB灯效", 200));
        list.add(createComponent("芝奇 DDR5 6400MHz 32GB (16GBx2)", Component.ComponentType.MEMORY, "G.SKILL", "Trident Z5 RGB", 
            new BigDecimal("1099.00"), "DDR5 6400MHz，CL32，RGB灯效", 150));
        list.add(createComponent("海盗船 DDR4 3600MHz 16GB (8GBx2)", Component.ComponentType.MEMORY, "Corsair", "Vengeance RGB Pro", 
            new BigDecimal("499.00"), "DDR4 3600MHz，CL18，RGB灯效", 300));
        
        // 硬盘 - SSD
        list.add(createComponent("三星 980 PRO 2TB", Component.ComponentType.STORAGE, "Samsung", "980 PRO", 
            new BigDecimal("1299.00"), "PCIe 4.0 NVMe，读取7000MB/s", 100));
        list.add(createComponent("西部数据 SN850X 1TB", Component.ComponentType.STORAGE, "WD", "SN850X", 
            new BigDecimal("799.00"), "PCIe 4.0 NVMe，读取7300MB/s", 150));
        list.add(createComponent("铠侠 RC20 1TB", Component.ComponentType.STORAGE, "KIOXIA", "RC20", 
            new BigDecimal("399.00"), "PCIe 3.0 NVMe，读取3400MB/s", 200));
        
        // 电源
        list.add(createComponent("海韵 FOCUS GX-850", Component.ComponentType.POWER_SUPPLY, "Seasonic", "FOCUS GX-850", 
            new BigDecimal("899.00"), "850W，80PLUS金牌，全模组", 60));
        list.add(createComponent("振华 LEADEX G 750W", Component.ComponentType.POWER_SUPPLY, "Super Flower", "LEADEX G", 
            new BigDecimal("699.00"), "750W，80PLUS金牌，全模组", 80));
        list.add(createComponent("安钛克 HCG850", Component.ComponentType.POWER_SUPPLY, "Antec", "HCG850", 
            new BigDecimal("799.00"), "850W，80PLUS金牌，全模组", 70));
        
        // 机箱
        list.add(createComponent("联力 O11 Dynamic EVO", Component.ComponentType.CASE, "Lian Li", "O11D EVO", 
            new BigDecimal("899.00"), "中塔，支持360水冷，钢化玻璃", 50));
        list.add(createComponent("酷冷至尊 TD500 Mesh", Component.ComponentType.CASE, "Cooler Master", "TD500", 
            new BigDecimal("599.00"), "中塔，网孔面板，RGB风扇", 100));
        list.add(createComponent("追风者 P400A", Component.ComponentType.CASE, "Phanteks", "P400A", 
            new BigDecimal("699.00"), "中塔，高风道设计，钢化玻璃", 80));
        
        // 散热器
        list.add(createComponent("猫头鹰 NH-D15", Component.ComponentType.COOLER, "Noctua", "NH-D15", 
            new BigDecimal("699.00"), "双塔风冷，静音设计，TDP 220W", 100));
        list.add(createComponent("九州风神 AK620", Component.ComponentType.COOLER, "DeepCool", "AK620", 
            new BigDecimal("299.00"), "双塔风冷，6热管，TDP 260W", 150));
        list.add(createComponent("恩杰 Kraken Z63", Component.ComponentType.COOLER, "NZXT", "Kraken Z63", 
            new BigDecimal("1599.00"), "280mm一体水冷，LCD显示屏", 60));
        
        return list;
    }

    /**
     * 创建配件对象
     */
    private Component createComponent(String name, Component.ComponentType type, 
                                     String brand, String model, BigDecimal price, 
                                     String specs, int stock) {
        Component component = new Component();
        component.setName(name);
        component.setType(type);
        component.setBrand(brand);
        component.setModel(model);
        component.setPrice(price);
        component.setSpecs(specs);
        component.setStockQuantity(stock);
        component.setCreatedAt(LocalDateTime.now());
        return component;
    }

    /**
     * 导入结果
     */
    public static class ImportResult {
        private int successCount = 0;
        private int errorCount = 0;
        private int skippedCount = 0;
        private List<String> errors = new ArrayList<>();
        private List<String> skipped = new ArrayList<>();
        private boolean failed = false;
        private String message = "";

        public void addSuccess(int lineNumber) {
            successCount++;
        }

        public void addError(int lineNumber, String error) {
            errorCount++;
            errors.add("行" + lineNumber + ": " + error);
        }

        public void addSkipped(int lineNumber, String reason) {
            skippedCount++;
            skipped.add("行" + lineNumber + ": " + reason);
        }

        // Getters and Setters
        public int getSuccessCount() { return successCount; }
        public int getErrorCount() { return errorCount; }
        public int getSkippedCount() { return skippedCount; }
        public List<String> getErrors() { return errors; }
        public List<String> getSkipped() { return skipped; }
        public boolean isFailed() { return failed; }
        public void setFailed(boolean failed) { this.failed = failed; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}


