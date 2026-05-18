package com.agriculture.demo.service;

import com.agriculture.demo.entity.*;
import com.agriculture.demo.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备服务类
 */
@Service
public class DeviceService {
    
    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private PlotMapper plotMapper;

    /**
     * 分页获取设备列表
     */
    public Page<Device> getDeviceList(int page, int size, String keyword, Long plotId, String status) {
        Page<Device> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Device::getDeviceCode, keyword)
                .or().like(Device::getDeviceName, keyword));
        }
        if (plotId != null) {
            wrapper.eq(Device::getPlotId, plotId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Device::getStatus, status);
        }
        
        wrapper.orderByDesc(Device::getCreateTime);
        Page<Device> devicePage = deviceMapper.selectPage(pageParam, wrapper);
        
        // 填充地块名称
        fillPlotNames(devicePage.getRecords());
        
        return devicePage;
    }

    /**
     * 获取设备详情
     */
    public Device getDeviceById(Long id) {
        Device device = deviceMapper.selectById(id);
        if (device != null) {
            fillPlotName(device);
        }
        return device;
    }

    /**
     * 绑定新设备
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean bindDevice(Device device) {
        // 检查设备编码是否已存在
        Device existDevice = deviceMapper.selectOne(
            new LambdaQueryWrapper<Device>()
                .eq(Device::getDeviceCode, device.getDeviceCode())
        );
        if (existDevice != null) {
            return false;
        }
        
        return deviceMapper.insert(device) > 0;
    }

    /**
     * 更新设备信息
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDevice(Device device) {
        return deviceMapper.updateById(device) > 0;
    }

    /**
     * 解绑设备
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean unbindDevice(Long id) {
        return deviceMapper.deleteById(id) > 0;
    }

    /**
     * 获取设备统计数据
     */
    public Map<String, Object> getDeviceStats() {
        Map<String, Object> result = new HashMap<>();
        
        Long total = deviceMapper.selectCount(null);
        Long online = deviceMapper.selectCount(
            new LambdaQueryWrapper<Device>()
                .eq(Device::getStatus, "ONLINE")
        );
        Long offline = deviceMapper.selectCount(
            new LambdaQueryWrapper<Device>()
                .eq(Device::getStatus, "OFFLINE")
        );
        
        result.put("total", total);
        result.put("online", online);
        result.put("offline", offline);
        
        return result;
    }

    /**
     * 填充设备列表的地块名称
     */
    private void fillPlotNames(List<Device> devices) {
        for (Device device : devices) {
            fillPlotName(device);
        }
    }

    /**
     * 填充单个设备的地块名称
     */
    private void fillPlotName(Device device) {
        if (device.getPlotId() != null) {
            Plot plot = plotMapper.selectById(device.getPlotId());
            if (plot != null) {
                device.setPlotName(plot.getPlotName());
            }
        }
    }
}