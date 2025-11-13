package com.buildmaster.controller;

import com.buildmaster.model.BuildConfig;
import com.buildmaster.service.BuildConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/configs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BuildConfigController {
    
    private final BuildConfigService buildConfigService;
    
    @GetMapping
    public ResponseEntity<List<BuildConfig>> getAllConfigs() {
        List<BuildConfig> configs = buildConfigService.getAllConfigs();
        return ResponseEntity.ok(configs);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BuildConfig>> getUserConfigs(@PathVariable Long userId) {
        List<BuildConfig> configs = buildConfigService.getUserConfigs(userId);
        return ResponseEntity.ok(configs);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<BuildConfig>> searchConfigs(
            @RequestParam String keyword,
            @RequestParam(required = false) Long userId) {
        List<BuildConfig> configs = buildConfigService.searchConfigs(keyword, userId);
        return ResponseEntity.ok(configs);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BuildConfig> getConfigById(@PathVariable Long id) {
        Optional<BuildConfig> config = buildConfigService.getConfigById(id);
        return config.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<BuildConfig> createConfig(@RequestBody BuildConfig config) {
        BuildConfig savedConfig = buildConfigService.saveConfig(config);
        return ResponseEntity.ok(savedConfig);
    }
    
    @PostMapping("/from-components")
    public ResponseEntity<BuildConfig> createConfigFromComponents(
            @RequestParam String name,
            @RequestParam String description,
            @RequestBody List<Long> componentIds,
            @RequestParam Long userId) {
        BuildConfig config = buildConfigService.createConfigFromComponents(name, description, componentIds, userId);
        return ResponseEntity.ok(config);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<BuildConfig> updateConfig(@PathVariable Long id, @RequestBody BuildConfig config) {
        config.setId(id);
        BuildConfig updatedConfig = buildConfigService.saveConfig(config);
        return ResponseEntity.ok(updatedConfig);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConfig(@PathVariable Long id) {
        buildConfigService.deleteConfig(id);
        return ResponseEntity.noContent().build();
    }
}
