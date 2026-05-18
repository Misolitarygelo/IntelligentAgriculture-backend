package com.agriculture.demo.service;

import com.agriculture.demo.entity.*;
import com.agriculture.demo.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 阈值配置服务类
 */
@Service
public class ThresholdService {
    
    @Autowired
    private ThresholdConfigMapper thresholdConfigMapper;

    /**
     * 获取地块阈值配置
     */
    public Map<String, Object> getThresholds(Long plotId) {
        Map<String, Object> result = new HashMap<>();
        result.put("plotId", plotId);
        
        List<ThresholdConfig> configs = thresholdConfigMapper.selectList(
            new LambdaQueryWrapper<ThresholdConfig>()
                .eq(ThresholdConfig::getPlotId, plotId)
        );
        
        for (ThresholdConfig config : configs) {
            switch (config.getConfigType()) {
                case "TEMP_LOW":
                    result.put("tempLow", config.getThresholdValue());
                    break;
                case "TEMP_HIGH":
                    result.put("tempHigh", config.getThresholdValue());
                    break;
                case "MOISTURE_LOW":
                    result.put("moistureLow", config.getThresholdValue());
                    break;
                case "MOISTURE_HIGH":
                    result.put("moistureHigh", config.getThresholdValue());
                    break;
            }
        }
        
        return result;
    }

    /**
     * 更新阈值配置
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateThresholds(Long plotId, BigDecimal tempLow, BigDecimal tempHigh, 
                                    BigDecimal moistureLow, BigDecimal moistureHigh) {
        // 更新或创建温度下限
        saveOrUpdateThreshold(plotId, "TEMP_LOW", tempLow);
        
        // 更新或创建温度上限
        saveOrUpdateThreshold(plotId, "TEMP_HIGH", tempHigh);
        
        // 更新或创建湿度下限
        saveOrUpdateThreshold(plotId, "MOISTURE_LOW", moistureLow);
        
        // 更新或创建湿度上限
        saveOrUpdateThreshold(plotId, "MOISTURE_HIGH", moistureHigh);
        
        return true;
    }

    /**
     * 保存或更新单个阈值配置
     */
    private void saveOrUpdateThreshold(Long plotId, String configType, BigDecimal value) {
        if (value == null) {
            return;
        }
        
        ThresholdConfig config = thresholdConfigMapper.selectOne(
            new LambdaQueryWrapper<ThresholdConfig>()
                .eq(ThresholdConfig::getPlotId, plotId)
                .eq(ThresholdConfig::getConfigType, configType)
        );
        
        if (config == null) {
            config = new ThresholdConfig();
            config.setPlotId(plotId);
            config.setConfigType(configType);
            config.setThresholdValue(value);
            thresholdConfigMapper.insert(config);
        } else {
            config.setThresholdValue(value);
            thresholdConfigMapper.updateById(config);
        }
    }
}
