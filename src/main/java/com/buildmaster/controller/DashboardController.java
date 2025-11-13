package com.buildmaster.controller;

import com.buildmaster.dto.ApiResponse;
import com.buildmaster.dto.DashboardStatsDTO;
import com.buildmaster.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "仪表盘管理", description = "管理系统仪表盘统计数据接口")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/stats")
    @Operation(summary = "获取仪表盘统计数据", description = "获取包含用户、配件、趋势等统计信息")
    public ApiResponse<DashboardStatsDTO> getDashboardStats() {
        try {
            DashboardStatsDTO stats = dashboardService.getDashboardStats();
            return ApiResponse.success("获取统计数据成功", stats);
        } catch (Exception e) {
            log.error("Failed to get dashboard stats", e);
            return ApiResponse.error("获取统计数据失败：" + e.getMessage());
        }
    }
}

