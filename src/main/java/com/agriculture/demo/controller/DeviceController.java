package com.agriculture.demo.controller;

import com.agriculture.demo.common.Result;
import com.agriculture.demo.entity.Device;
import com.agriculture.demo.service.DeviceService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 设备控制器
 */
@RestController
@RequestMapping("/api/devices")
@CrossOrigin
public class DeviceController {
    
    @Autowired
    private DeviceService deviceService;

    /**
     * 获取设备列表（分页）
     */
    @GetMapping
    public Result<Page<Device>> getDeviceList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long plotId,
            @RequestParam(required = false) String status) {
        return Result.success(deviceService.getDeviceList(page, size, keyword, plotId, status));
    }

    /**
     * 获取设备详情
     */
    @GetMapping("/{id}")
    public Result<Device> getDeviceById(@PathVariable Long id) {
        Device device = deviceService.getDeviceById(id);
        if (device == null) {
            return Result.error("设备不存在");
        }
        return Result.success(device);
    }

    /**
     * 绑定新设备
     */
    @PostMapping
    public Result<Void> bindDevice(@RequestBody Device device) {
        boolean success = deviceService.bindDevice(device);
        if (!success) {
            return Result.error("设备编码已存在");
        }
        return Result.success();
    }

    /**
     * 更新设备信息
     */
    @PutMapping("/{id}")
    public Result<Void> updateDevice(@PathVariable Long id, @RequestBody Device device) {
        device.setId(id);
        boolean success = deviceService.updateDevice(device);
        if (!success) {
            return Result.error("更新失败");
        }
        return Result.success();
    }

    /**
     * 解绑设备
     */
    @DeleteMapping("/{id}")
    public Result<Void> unbindDevice(@PathVariable Long id) {
        boolean success = deviceService.unbindDevice(id);
        if (!success) {
            return Result.error("解绑失败");
        }
        return Result.success();
    }

    /**
     * 获取设备统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getDeviceStats() {
        return Result.success(deviceService.getDeviceStats());
    }
}
