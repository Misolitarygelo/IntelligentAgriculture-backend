package com.agriculture.demo.service;

import com.agriculture.demo.entity.Device;
import com.agriculture.demo.entity.Plot;
import com.agriculture.demo.entity.SensorData;
import com.agriculture.demo.enums.DataTypeEnum;
import com.agriculture.demo.mapper.DeviceMapper;
import com.agriculture.demo.mapper.PlotMapper;
import com.agriculture.demo.mapper.SensorDataMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 地块服务类
 */
@Service
public class PlotService {

    @Autowired
    private PlotMapper plotMapper;

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private SensorDataMapper sensorDataMapper;

    @Autowired
    private WebSocketService webSocketService;

    /**
     * 获取所有地块列表
     */
    public List<Plot> getAllPlots() {
        return plotMapper.selectList(new LambdaQueryWrapper<>());
    }

    /**
     * 获取地块详情
     */
    public Plot getPlotById(Long id) {
        return plotMapper.selectById(id);
    }

    /**
     * 获取地块实时数据
     */
    public Map<String, Object> getPlotRealtimeData(Long plotId) {
        Map<String, Object> result = new HashMap<>();
        Plot plot = plotMapper.selectById(plotId);

        if (plot != null) {
            result.put("plotId", plot.getId());
            result.put("plotName", plot.getPlotName());
            result.put("irrigationMode", plot.getIrrigationMode());
            result.put("irrigationStatus", plot.getCurrentIrrigationStatus());

            // 获取该地块的所有设备
            List<Device> devices = deviceMapper.selectList(
                    new LambdaQueryWrapper<Device>().eq(Device::getPlotId, plotId));
            int deviceTotalCount = devices.size();
            int deviceOnlineCount = 0;
            Long sensorDeviceId = null;

            for (Device device : devices) {
                if ("ONLINE".equals(device.getStatus())) {
                    deviceOnlineCount++;
                }
                // 找到传感器设备（用于获取温湿度数据）
                if ("SENSOR".equals(device.getDeviceType())) {
                    sensorDeviceId = device.getId();
                }
            }

            result.put("deviceTotalCount", deviceTotalCount);
            result.put("deviceOnlineCount", deviceOnlineCount);

            // 获取最新的传感器数据
            if (sensorDeviceId != null) {
                // 获取最新的土壤湿度数据
                SensorData moistureData = sensorDataMapper.selectOne(
                        new LambdaQueryWrapper<SensorData>()
                                .eq(SensorData::getDeviceId, sensorDeviceId)
                                .eq(SensorData::getDataType, DataTypeEnum.SOIL_MOISTURE.getCode())
                                .orderByDesc(SensorData::getCollectTime)
                                .last("LIMIT 1"));

                // 获取最新的空气温度数据
                SensorData tempData = sensorDataMapper.selectOne(
                        new LambdaQueryWrapper<SensorData>()
                                .eq(SensorData::getDeviceId, sensorDeviceId)
                                .eq(SensorData::getDataType, DataTypeEnum.AIR_TEMP.getCode())
                                .orderByDesc(SensorData::getCollectTime)
                                .last("LIMIT 1"));

                if (moistureData != null && moistureData.getValue() != null) {
                    result.put("soilMoisture", moistureData.getValue().doubleValue());
                } else {
                    result.put("soilMoisture", null);
                }

                if (tempData != null && tempData.getValue() != null) {
                    result.put("airTemp", tempData.getValue().doubleValue());
                } else {
                    result.put("airTemp", null);
                }
            } else {
                result.put("soilMoisture", null);
                result.put("airTemp", null);
            }
        }

        return result;
    }

    /**
     * 更新灌溉模式
     */
    public boolean updateIrrigationMode(Long plotId, String mode) {
        Plot plot = plotMapper.selectById(plotId);
        if (plot == null) {
            return false;
        }

        plot.setIrrigationMode(mode);
        int rows = plotMapper.updateById(plot);

        if (rows > 0) {
            webSocketService.pushIrrigationMode(plot);
        }

        return rows > 0;
    }
}
