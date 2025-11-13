package com.buildmaster.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "build_config_components")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildConfigComponent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "build_config_id", nullable = false)
    private BuildConfig buildConfig;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = false)
    private Component component;
    
    @Column(name = "quantity")
    private Integer quantity = 1;
    
    @Column(name = "unit_price", precision = 10, scale = 2)
    private java.math.BigDecimal unitPrice;
}
