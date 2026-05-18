package com.agriculture.demo.controller;

import com.agriculture.demo.common.Result;
import com.agriculture.demo.service.SensorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 传感器数据控制器
 */
@RestController
@RequestMapping("/api/sensor-data")
@CrossOrigin
public class SensorDataController {

    @Autowired
    private SensorDataService sensorDataService;

    /**
     * 获取地块实时数据
     */
    @GetMapping("/plot/{plotId}/realtime")
    public Result<Map<String, Object>> getPlotRealtimeData(@PathVariable Long plotId) {
        return Result.success(sensorDataService.getPlotRealtimeData(plotId));
    }

    /**
     * 获取历史数据（支持多种粒度）
     * 
     * @param plotId      地块ID
     * @param startTime   开始时间（格式：yyyy-MM-dd HH:mm:ss）
     * @param endTime     结束时间（格式：yyyy-MM-dd HH:mm:ss）
     * @param metric      指标类型：both/moisture/temp，默认both
     * @param granularity 数据粒度：second/minute/hour/day，默认minute
     */
    @GetMapping("/history")
    public Result<Map<String, Object>> getHistoryData(
            @RequestParam Long plotId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "both") String metric,
            @RequestParam(defaultValue = "minute") String granularity) {
        return Result.success(sensorDataService.getHistoryData(plotId, startTime, endTime, metric, granularity));
    }

    /**
     * 获取7天趋势数据（按天聚合）
     */
    @GetMapping("/plot/{plotId}/trend")
    public Result<Map<String, Object>> getTrendData(@PathVariable Long plotId) {
        return Result.success(sensorDataService.getTrendData(plotId));
    }
}