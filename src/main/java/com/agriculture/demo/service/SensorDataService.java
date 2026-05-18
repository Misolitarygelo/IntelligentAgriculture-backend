package com.agriculture.demo.service;

import com.agriculture.demo.entity.*;
import com.agriculture.demo.enums.*;
import com.agriculture.demo.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 传感器数据服务类
 */
@Service
public class SensorDataService {

    @Autowired
    private SensorDataMapper sensorDataMapper;

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private PlotMapper plotMapper;

    /**
     * 获取地块实时数据
     */
    public Map<String, Object> getPlotRealtimeData(Long plotId) {
        Map<String, Object> result = new HashMap<>();

        Plot plot = plotMapper.selectById(plotId);
        if (plot != null) {
            result.put("plotId", plot.getId());
            result.put("plotName", plot.getPlotName());
        }

        // 查询该地块的温湿度设备
        Device device = deviceMapper.selectOne(
                new LambdaQueryWrapper<Device>()
                        .eq(Device::getPlotId, plotId)
                        .eq(Device::getDeviceType, DeviceTypeEnum.MOISTURE_TEMP.getCode()));

        if (device != null) {
            // 查询最新的土壤湿度数据
            SensorData moistureData = sensorDataMapper.selectOne(
                    new LambdaQueryWrapper<SensorData>()
                            .eq(SensorData::getDeviceId, device.getId())
                            .eq(SensorData::getDataType, DataTypeEnum.SOIL_MOISTURE.getCode())
                            .orderByDesc(SensorData::getCollectTime)
                            .last("LIMIT 1"));

            // 查询最新的空气温度数据
            SensorData tempData = sensorDataMapper.selectOne(
                    new LambdaQueryWrapper<SensorData>()
                            .eq(SensorData::getDeviceId, device.getId())
                            .eq(SensorData::getDataType, DataTypeEnum.AIR_TEMP.getCode())
                            .orderByDesc(SensorData::getCollectTime)
                            .last("LIMIT 1"));

            if (moistureData != null) {
                Map<String, Object> soilMoisture = new HashMap<>();
                soilMoisture.put("value", moistureData.getValue());
                soilMoisture.put("collectTime", moistureData.getCollectTime());
                soilMoisture.put("deviceCode", device.getDeviceCode());
                result.put("soilMoisture", soilMoisture);
            }

            if (tempData != null) {
                Map<String, Object> airTemp = new HashMap<>();
                airTemp.put("value", tempData.getValue());
                airTemp.put("collectTime", tempData.getCollectTime());
                airTemp.put("deviceCode", device.getDeviceCode());
                result.put("airTemp", airTemp);
            }
        }

        return result;
    }

    /**
     * 获取历史数据（支持多种粒度）
     * 
     * @param plotId      地块ID
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param metric      指标类型：both/moisture/temp
     * @param granularity 数据粒度：second/minute/hour/day
     * @return 聚合后的数据
     */
    public Map<String, Object> getHistoryData(Long plotId, LocalDateTime startTime, LocalDateTime endTime,
            String metric, String granularity) {
        Map<String, Object> result = new HashMap<>();

        // 查询该地块的温湿度设备
        Device device = deviceMapper.selectOne(
                new LambdaQueryWrapper<Device>()
                        .eq(Device::getPlotId, plotId)
                        .eq(Device::getDeviceType, DeviceTypeEnum.MOISTURE_TEMP.getCode()));

        if (device == null) {
            return result;
        }

        List<String> times = new ArrayList<>();
        List<BigDecimal> moistureData = new ArrayList<>();
        List<BigDecimal> tempData = new ArrayList<>();

        // 获取粒度枚举
        GranularityEnum granularityEnum = GranularityEnum.fromCode(granularity);

        // 根据指标查询数据
        if ("both".equals(metric) || "moisture".equals(metric)) {
            List<Map<String, Object>> dataList = queryAggregatedData(device.getId(),
                    DataTypeEnum.SOIL_MOISTURE.getCode(), startTime, endTime, granularityEnum);

            for (Map<String, Object> data : dataList) {
                times.add(data.get("time").toString());
                moistureData.add(new BigDecimal(data.get("avg_value").toString()));
            }
        }

        if ("both".equals(metric) || "temp".equals(metric)) {
            List<Map<String, Object>> dataList = queryAggregatedData(device.getId(),
                    DataTypeEnum.AIR_TEMP.getCode(), startTime, endTime, granularityEnum);

            // 如果times已包含时间（从湿度查询），只需添加温度值
            if (times.isEmpty()) {
                for (Map<String, Object> data : dataList) {
                    times.add(data.get("time").toString());
                }
            }

            for (Map<String, Object> data : dataList) {
                tempData.add(new BigDecimal(data.get("avg_value").toString()));
            }
        }

        result.put("times", times);
        result.put("moistureData", moistureData);
        result.put("tempData", tempData);
        result.put("granularity", granularityEnum.getCode());

        return result;
    }

    /**
     * 根据粒度查询聚合数据
     */
    private List<Map<String, Object>> queryAggregatedData(Long deviceId, String dataType,
            LocalDateTime startTime, LocalDateTime endTime, GranularityEnum granularity) {
        switch (granularity) {
            case SECOND:
                return sensorDataMapper.getRawDataBySecond(deviceId, dataType, startTime, endTime);
            case MINUTE:
                return sensorDataMapper.getAggregatedDataByMinute(deviceId, dataType, startTime, endTime);
            case HOUR:
                return sensorDataMapper.getAggregatedDataByHour(deviceId, dataType, startTime, endTime);
            case DAY:
                return sensorDataMapper.getAggregatedDataByDay(deviceId, dataType, startTime, endTime);
            default:
                return sensorDataMapper.getAggregatedDataByMinute(deviceId, dataType, startTime, endTime);
        }
    }

    /**
     * 获取7天趋势数据（按天聚合）
     */
    public Map<String, Object> getTrendData(Long plotId) {
        return getTrendData(plotId, 7);
    }

    /**
     * 获取指定天数的趋势数据（按天聚合）
     * 
     * @param plotId 地块ID
     * @param days   天数
     * @return 趋势数据
     */
    public Map<String, Object> getTrendData(Long plotId, int days) {
        Map<String, Object> result = new HashMap<>();

        // 查询该地块的温湿度设备
        Device device = deviceMapper.selectOne(
                new LambdaQueryWrapper<Device>()
                        .eq(Device::getPlotId, plotId)
                        .eq(Device::getDeviceType, DeviceTypeEnum.MOISTURE_TEMP.getCode()));

        if (device == null) {
            return result;
        }

        // 计算起始时间（days天前的0点）
        LocalDateTime startTime = LocalDateTime.of(LocalDate.now().minusDays(days), LocalTime.MIN);

        List<String> dates = new ArrayList<>();
        List<BigDecimal> moistureData = new ArrayList<>();
        List<BigDecimal> tempData = new ArrayList<>();

        // 查询土壤湿度趋势
        List<Map<String, Object>> moistureList = sensorDataMapper.getTrendDataByDay(
                device.getId(), DataTypeEnum.SOIL_MOISTURE.getCode(), startTime);

        // 查询温度趋势
        List<Map<String, Object>> tempList = sensorDataMapper.getTrendDataByDay(
                device.getId(), DataTypeEnum.AIR_TEMP.getCode(), startTime);

        // 构建日期列表（包含所有天数，即使没有数据）
        Map<String, BigDecimal> moistureMap = new HashMap<>();
        Map<String, BigDecimal> tempMap = new HashMap<>();

        for (Map<String, Object> data : moistureList) {
            moistureMap.put(data.get("date").toString(), new BigDecimal(data.get("avg_value").toString()));
        }

        for (Map<String, Object> data : tempList) {
            tempMap.put(data.get("date").toString(), new BigDecimal(data.get("avg_value").toString()));
        }

        // 生成完整的日期序列
        for (int i = days - 1; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).toString();
            dates.add(date);
            moistureData.add(moistureMap.getOrDefault(date, BigDecimal.ZERO));
            tempData.add(tempMap.getOrDefault(date, BigDecimal.ZERO));
        }

        result.put("dates", dates);
        result.put("moistureData", moistureData);
        result.put("tempData", tempData);
        result.put("startDate", LocalDate.now().minusDays(days - 1).toString());
        result.put("endDate", LocalDate.now().toString());

        return result;
    }
}