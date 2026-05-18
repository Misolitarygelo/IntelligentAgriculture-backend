package com.agriculture.demo.controller;

import com.agriculture.demo.common.Result;
import com.agriculture.demo.entity.AlertLog;
import com.agriculture.demo.service.AlertService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 告警控制器
 */
@RestController
@RequestMapping("/api/alerts")
@CrossOrigin
public class AlertController {
    
    @Autowired
    private AlertService alertService;

    /**
     * 获取告警列表（分页）
     */
    @GetMapping
    public Result<Page<AlertLog>> getAlertList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long plotId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String alertType) {
        return Result.success(alertService.getAlertList(page, size, plotId, status, alertType));
    }

    /**
     * 获取最近告警
     */
    @GetMapping("/recent")
    public Result<List<AlertLog>> getRecentAlerts(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(alertService.getRecentAlerts(limit));
    }

    /**
     * 获取告警详情
     */
    @GetMapping("/{id}")
    public Result<AlertLog> getAlertById(@PathVariable Long id) {
        AlertLog alertLog = alertService.getAlertById(id);
        if (alertLog == null) {
            return Result.error("告警不存在");
        }
        return Result.success(alertLog);
    }

    /**
     * 处理告警
     */
    @PutMapping("/{id}/handle")
    public Result<Void> handleAlert(
            @PathVariable Long id,
            @RequestParam String handleType) {
        boolean success = alertService.processAlert(id, handleType);
        if (!success) {
            return Result.error("处理失败");
        }
        return Result.success();
    }
}
