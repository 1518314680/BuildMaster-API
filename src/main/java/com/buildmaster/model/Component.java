package com.buildmaster.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "components")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Component {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComponentType type;
    
    private String brand;
    
    private String model;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "specifications", columnDefinition = "JSON")
    private String specifications;
    
    @Column(name = "is_available")
    private Boolean isAvailable = true;
    
    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;
    
    @Column(name = "jd_sku_id")
    private String jdSkuId;  // 京东商品ID，用于价格更新
    
    @Column(name = "purchase_url", columnDefinition = "TEXT")
    private String purchaseUrl;  // 购买链接（京东联盟推广链接）
    
    @Column(name = "price_updated_at")
    private LocalDateTime priceUpdatedAt;  // 价格最后更新时间
    
    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;  // 原价
    
    @Column(name = "commission_rate")
    private BigDecimal commissionRate;  // 佣金比例
    
    @Column(name = "specs", columnDefinition = "TEXT")
    private String specs;  // 规格说明（简化版）
    
    // 配件子分类
    @Column(name = "sub_type")
    private String subType;  // 子分类（机箱风扇、鼠标等）
    
    // 显示器专属字段
    @Column(name = "resolution")
    private String resolution;  // 分辨率（1920x1080、2560x1440等）
    
    @Column(name = "screen_size")
    private String screenSize;  // 屏幕尺寸（24、27、32等）
    
    @Column(name = "refresh_rate")
    private Integer refreshRate;  // 刷新率（Hz）
    
    @Column(name = "panel_type")
    private String panelType;  // 面板类型（IPS、VA、TN等）
    
    // 兼容性字段
    @Column(name = "cpu_socket")
    private String cpuSocket;  // CPU接口（AM4、AM5、LGA1700等）
    
    @Column(name = "chipset")
    private String chipset;  // 主板芯片组
    
    @Column(name = "memory_type")
    private String memoryType;  // 内存类型（DDR4、DDR5）
    
    @Column(name = "psu_wattage")
    private Integer psuWattage;  // 电源功率（W）
    
    @Column(name = "form_factor")
    private String formFactor;  // 板型/尺寸（ATX、M-ATX、ITX等）
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum ComponentType {
        // 核心配件
        CPU,            // 处理器
        MOTHERBOARD,    // 主板
        GPU,            // 显卡
        MEMORY,         // 内存
        STORAGE,        // 存储
        POWER_SUPPLY,   // 电源
        COOLER,         // 散热
        CASE,           // 机箱
        
        // 可选配件
        MONITOR,        // 显示器
        ACCESSORY,      // 配件
        PERIPHERAL      // 外设
    }
}
