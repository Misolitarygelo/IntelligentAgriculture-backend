package com.agriculture.demo.controller;

import com.agriculture.demo.common.Result;
import com.agriculture.demo.entity.Plot;
import com.agriculture.demo.service.PlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 地块控制器
 */
@RestController
@RequestMapping("/api/plots")
@CrossOrigin
public class PlotController {
    
    @Autowired
    private PlotService plotService;

    /**
     * 获取所有地块列表
     */
    @GetMapping
    public Result<List<Plot>> getAllPlots() {
        return Result.success(plotService.getAllPlots());
    }

    /**
     * 获取地块详情
     */
    @GetMapping("/{id}")
    public Result<Plot> getPlotById(@PathVariable Long id) {
        Plot plot = plotService.getPlotById(id);
        if (plot == null) {
            return Result.error("地块不存在");
        }
        return Result.success(plot);
    }

    /**
     * 获取地块实时数据
     */
    @GetMapping("/{id}/realtime")
    public Result<Map<String, Object>> getPlotRealtimeData(@PathVariable Long id) {
        return Result.success(plotService.getPlotRealtimeData(id));
    }

    /**
     * 更新灌溉模式
     */
    @PutMapping("/{id}/irrigation-mode")
    public Result<Void> updateIrrigationMode(@PathVariable Long id, @RequestParam String mode) {
        boolean success = plotService.updateIrrigationMode(id, mode);
        if (!success) {
            return Result.error("更新失败");
        }
        return Result.success();
    }
}
