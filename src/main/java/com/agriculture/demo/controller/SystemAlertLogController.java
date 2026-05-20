package com.agriculture.demo.controller;

import com.agriculture.demo.entity.SystemAlertLog;
import com.agriculture.demo.service.SystemAlertLogService;
import com.agriculture.demo.common.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 系统告警日志控制器
 */
@RestController
@RequestMapping("/api/system-alerts")
public class SystemAlertLogController {

    @Autowired
    private SystemAlertLogService systemAlertLogService;

    /**
     * 获取告警列表（分页）
     */
    @GetMapping
    public Result<Page<SystemAlertLog>> getAlertList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String alertType,
            @RequestParam(required = false) String alertLevel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        Page<SystemAlertLog> alertPage = systemAlertLogService.getAlertList(
                page, size, alertType, alertLevel, status, startTime, endTime);

        return Result.success(alertPage);
    }

    /**
     * 获取告警详情
     */
    @GetMapping("/{id}")
    public Result<SystemAlertLog> getAlertById(@PathVariable Long id) {
        SystemAlertLog alert = systemAlertLogService.getAlertById(id);
        if (alert == null) {
            return Result.error("告警不存在");
        }
        return Result.success(alert);
    }

    /**
     * 处理告警
     */
    @PutMapping("/{id}/handle")
    public Result<String> handleAlert(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String handleType = request.get("handleType");

        if (handleType == null || handleType.isEmpty()) {
            return Result.error("处理类型不能为空");
        }

        boolean success = systemAlertLogService.handleAlert(id, handleType);
        if (success) {
            return Result.success("处理成功");
        }
        return Result.error("处理失败，告警不存在");
    }

    /**
     * 获取告警统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getAlertStats() {
        Map<String, Object> stats = systemAlertLogService.getAlertStats();
        return Result.success(stats);
    }

}
