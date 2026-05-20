package com.agriculture.demo.service;

import com.agriculture.demo.entity.SystemAlertLog;
import com.agriculture.demo.enums.SystemAlertLevelEnum;
import com.agriculture.demo.enums.SystemAlertStatusEnum;
import com.agriculture.demo.enums.SystemAlertTypeEnum;
import com.agriculture.demo.mapper.SystemAlertLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统告警日志服务类
 */
@Service
public class SystemAlertLogService {

    private static final Logger logger = LoggerFactory.getLogger(SystemAlertLogService.class);

    @Autowired
    private SystemAlertLogMapper systemAlertLogMapper;

    /**
     * 获取告警列表（分页）
     */
    public Page<SystemAlertLog> getAlertList(int page, int size, String alertType,
            String alertLevel, String status,
            LocalDateTime startTime, LocalDateTime endTime) {
        Page<SystemAlertLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SystemAlertLog> wrapper = new LambdaQueryWrapper<>();

        if (alertType != null && !alertType.isEmpty()) {
            wrapper.eq(SystemAlertLog::getAlertType, alertType);
        }
        if (alertLevel != null && !alertLevel.isEmpty()) {
            wrapper.eq(SystemAlertLog::getAlertLevel, alertLevel);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(SystemAlertLog::getStatus, status);
        }
        if (startTime != null) {
            wrapper.ge(SystemAlertLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(SystemAlertLog::getCreateTime, endTime);
        }

        wrapper.orderByDesc(SystemAlertLog::getCreateTime);
        Page<SystemAlertLog> alertPage = systemAlertLogMapper.selectPage(pageParam, wrapper);

        // 填充枚举名称
        fillEnumNames(alertPage.getRecords());

        return alertPage;
    }

    /**
     * 获取告警详情
     */
    public SystemAlertLog getAlertById(Long id) {
        SystemAlertLog alert = systemAlertLogMapper.selectById(id);
        if (alert != null) {
            fillEnumName(alert);
        }
        return alert;
    }

    /**
     * 处理告警
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean handleAlert(Long id, String handleType) {
        SystemAlertLog alert = systemAlertLogMapper.selectById(id);
        if (alert == null) {
            return false;
        }

        // 根据处理类型设置状态
        if ("PROCESSING".equals(handleType)) {
            alert.setStatus(SystemAlertStatusEnum.PROCESSING.getCode());
        } else if ("DONE".equals(handleType)) {
            alert.setStatus(SystemAlertStatusEnum.DONE.getCode());
            alert.setHandleTime(LocalDateTime.now());
        } else if ("IGNORED".equals(handleType)) {
            alert.setStatus(SystemAlertStatusEnum.IGNORED.getCode());
            alert.setHandleTime(LocalDateTime.now());
        }

        alert.setUpdateTime(LocalDateTime.now());
        int rows = systemAlertLogMapper.updateById(alert);

        logger.info("处理系统告警 - 告警ID: {}, 处理类型: {}, 状态: {}", id, handleType, alert.getStatus());

        return rows > 0;
    }

    /**
     * 获取告警统计
     */
    public Map<String, Object> getAlertStats() {
        Map<String, Object> result = new HashMap<>();

        // 总数
        Long total = systemAlertLogMapper.selectCount(null);

        // 按状态统计
        Long pending = systemAlertLogMapper.selectCount(
                new LambdaQueryWrapper<SystemAlertLog>()
                        .eq(SystemAlertLog::getStatus, SystemAlertStatusEnum.PENDING.getCode()));
        Long processing = systemAlertLogMapper.selectCount(
                new LambdaQueryWrapper<SystemAlertLog>()
                        .eq(SystemAlertLog::getStatus, SystemAlertStatusEnum.PROCESSING.getCode()));
        Long done = systemAlertLogMapper.selectCount(
                new LambdaQueryWrapper<SystemAlertLog>()
                        .eq(SystemAlertLog::getStatus, SystemAlertStatusEnum.DONE.getCode()));
        Long ignored = systemAlertLogMapper.selectCount(
                new LambdaQueryWrapper<SystemAlertLog>()
                        .eq(SystemAlertLog::getStatus, SystemAlertStatusEnum.IGNORED.getCode()));

        // 按级别统计
        Map<String, Long> byLevel = new HashMap<>();
        for (SystemAlertLevelEnum level : SystemAlertLevelEnum.values()) {
            Long count = systemAlertLogMapper.selectCount(
                    new LambdaQueryWrapper<SystemAlertLog>()
                            .eq(SystemAlertLog::getAlertLevel, level.getCode()));
            byLevel.put(level.getCode(), count);
        }

        // 按类型统计
        Map<String, Long> byType = new HashMap<>();
        for (SystemAlertTypeEnum type : SystemAlertTypeEnum.values()) {
            Long count = systemAlertLogMapper.selectCount(
                    new LambdaQueryWrapper<SystemAlertLog>()
                            .eq(SystemAlertLog::getAlertType, type.getCode()));
            byType.put(type.getCode(), count);
        }

        result.put("total", total);
        result.put("pending", pending);
        result.put("processing", processing);
        result.put("done", done);
        result.put("ignored", ignored);
        result.put("byLevel", byLevel);
        result.put("byType", byType);

        return result;
    }

    /**
     * 记录系统告警
     */
    @Transactional(rollbackFor = Exception.class)
    public void logAlert(SystemAlertTypeEnum alertType, SystemAlertLevelEnum alertLevel,
            String message, String detail, String source) {
        SystemAlertLog alert = new SystemAlertLog();
        alert.setAlertType(alertType.getCode());
        alert.setAlertLevel(alertLevel.getCode());
        alert.setMessage(message);
        alert.setDetail(detail);
        alert.setSource(source);
        alert.setStatus(SystemAlertStatusEnum.PENDING.getCode());
        alert.setCreateTime(LocalDateTime.now());
        alert.setUpdateTime(LocalDateTime.now());

        systemAlertLogMapper.insert(alert);

        logger.info("记录系统告警 - 类型: {}, 级别: {}, 消息: {}", alertType.getCode(), alertLevel.getCode(), message);
    }

    /**
     * 填充告警列表的枚举名称
     */
    private void fillEnumNames(List<SystemAlertLog> alerts) {
        for (SystemAlertLog alert : alerts) {
            fillEnumName(alert);
        }
    }

    /**
     * 填充单个告警的枚举名称
     */
    private void fillEnumName(SystemAlertLog alert) {
        alert.setAlertTypeName(SystemAlertTypeEnum.getByCode(alert.getAlertType()).getName());
        alert.setAlertLevelName(SystemAlertLevelEnum.getByCode(alert.getAlertLevel()).getName());
        alert.setStatusName(SystemAlertStatusEnum.getByCode(alert.getStatus()).getName());
    }
}
