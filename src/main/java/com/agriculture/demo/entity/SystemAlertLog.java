package com.agriculture.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统告警日志实体类
 */
@Data
@TableName("system_alert_log")
public class SystemAlertLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 告警类型
     */
    @TableField("alert_type")
    private String alertType;

    /**
     * 告警级别（INFO/WARN/ERROR）
     */
    @TableField("alert_level")
    private String alertLevel;

    /**
     * 告警消息内容
     */
    @TableField("message")
    private String message;

    /**
     * 详细信息（JSON格式）
     */
    @TableField("detail")
    private String detail;

    /**
     * 告警来源（模块/类名）
     */
    @TableField("source")
    private String source;

    /**
     * 处理状态（PENDING/PROCESSING/DONE/IGNORED）
     */
    @TableField("status")
    private String status;

    /**
     * 处理时间
     */
    @TableField("handle_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handleTime;

    /**
     * 创建时间
     */
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 告警类型名称（非数据库字段）
     */
    @TableField(exist = false)
    private String alertTypeName;

    /**
     * 告警级别名称（非数据库字段）
     */
    @TableField(exist = false)
    private String alertLevelName;

    /**
     * 处理状态名称（非数据库字段）
     */
    @TableField(exist = false)
    private String statusName;
}
