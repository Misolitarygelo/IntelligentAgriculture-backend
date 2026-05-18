package com.agriculture.demo.controller;

import com.agriculture.demo.common.Result;
import com.agriculture.demo.entity.IrrigationLog;
import com.agriculture.demo.service.IrrigationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 灌溉控制控制器
 */
@RestController
@RequestMapping("/api/irrigation")
@CrossOrigin
public class IrrigationController {

    @Autowired
    private IrrigationService irrigationService;

    /**
     * 获取灌溉状态
     */
    @GetMapping("/plot/{plotId}/status")
    public Result<Map<String, Object>> getIrrigationStatus(@PathVariable Long plotId) {
        return Result.success(irrigationService.getIrrigationStatus(plotId));
    }

    /**
     * 发送灌溉控制指令
     */
    @PostMapping("/plot/{plotId}/control")
    public Result<Map<String, Object>> controlIrrigation(
            @PathVariable Long plotId,
            @RequestParam String operation) {
        Map<String, Object> result = irrigationService.controlIrrigation(plotId, operation);
        Boolean success = (Boolean) result.get("success");
        if (!success) {
            return Result.error((String) result.get("message"));
        }
        return Result.success(result);
    }

    /**
     * 获取灌溉记录
     */
    @GetMapping("/logs")
    public Result<Page<IrrigationLog>> getIrrigationLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long plotId) {
        return Result.success(irrigationService.getIrrigationLogs(page, size, plotId));
    }
}
