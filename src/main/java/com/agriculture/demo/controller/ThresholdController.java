package com.agriculture.demo.controller;

import com.agriculture.demo.common.Result;
import com.agriculture.demo.service.ThresholdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 阈值配置控制器
 */
@RestController
@RequestMapping("/api/thresholds")
@CrossOrigin
public class ThresholdController {
    
    @Autowired
    private ThresholdService thresholdService;

    /**
     * 获取地块阈值配置
     */
    @GetMapping("/plot/{plotId}")
    public Result<Map<String, Object>> getThresholds(@PathVariable Long plotId) {
        return Result.success(thresholdService.getThresholds(plotId));
    }

    /**
     * 更新阈值配置
     */
    @PutMapping("/plot/{plotId}")
    public Result<Void> updateThresholds(
            @PathVariable Long plotId,
            @RequestParam(required = false) BigDecimal tempLow,
            @RequestParam(required = false) BigDecimal tempHigh,
            @RequestParam(required = false) BigDecimal moistureLow,
            @RequestParam(required = false) BigDecimal moistureHigh) {
        thresholdService.updateThresholds(plotId, tempLow, tempHigh, moistureLow, moistureHigh);
        return Result.success();
    }
}
