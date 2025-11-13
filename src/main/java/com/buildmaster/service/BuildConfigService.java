package com.buildmaster.service;

import com.buildmaster.model.BuildConfig;
import com.buildmaster.model.Component;
import com.buildmaster.repository.BuildConfigRepository;
import com.buildmaster.repository.ComponentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BuildConfigService {
    
    private final BuildConfigRepository buildConfigRepository;
    private final ComponentRepository componentRepository;
    
    public List<BuildConfig> getAllConfigs() {
        return buildConfigRepository.findByIsPublicTrue();
    }
    
    public List<BuildConfig> getUserConfigs(Long userId) {
        return buildConfigRepository.findByUserId(userId);
    }
    
    public List<BuildConfig> searchConfigs(String keyword, Long userId) {
        return buildConfigRepository.searchByKeyword(keyword, userId);
    }
    
    public Optional<BuildConfig> getConfigById(Long id) {
        return buildConfigRepository.findById(id);
    }
    
    public BuildConfig saveConfig(BuildConfig config) {
        // 计算总价格
        if (config.getComponents() != null) {
            BigDecimal totalPrice = config.getComponents().stream()
                .map(component -> component.getUnitPrice().multiply(BigDecimal.valueOf(component.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            config.setTotalPrice(totalPrice);
        }
        
        return buildConfigRepository.save(config);
    }
    
    public void deleteConfig(Long id) {
        buildConfigRepository.deleteById(id);
    }
    
    public BuildConfig createConfigFromComponents(String name, String description, List<Long> componentIds, Long userId) {
        BuildConfig config = new BuildConfig();
        config.setName(name);
        config.setDescription(description);
        config.setUserId(userId);
        config.setIsPublic(false);
        
        // 计算总价格
        BigDecimal totalPrice = componentIds.stream()
            .map(componentRepository::findById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(Component::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        config.setTotalPrice(totalPrice);
        
        return buildConfigRepository.save(config);
    }
}
