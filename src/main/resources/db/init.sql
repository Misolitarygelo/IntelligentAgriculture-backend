-- ============================================================
-- 智慧农业系统数据库初始化脚本
-- ============================================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. 用户表（sys_user）
-- ============================================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（加密）',
  `real_name` VARCHAR(50) COMMENT '真实姓名',
  `role` VARCHAR(20) NOT NULL COMMENT '角色：FARMER/ADMIN/SYSTEM_ADMIN',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 2. 地块表（plot）
-- ============================================================
DROP TABLE IF EXISTS `plot`;
CREATE TABLE `plot` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '地块ID',
  `plot_code` VARCHAR(50) NOT NULL COMMENT '地块编码',
  `plot_name` VARCHAR(100) NOT NULL COMMENT '地块名称',
  `location` VARCHAR(200) COMMENT '位置描述',
  `area` DECIMAL(10,2) COMMENT '面积（亩）',
  `user_id` BIGINT NOT NULL COMMENT '所属农户ID',
  `irrigation_mode` VARCHAR(20) NOT NULL DEFAULT 'MANUAL' COMMENT '灌溉模式：MANUAL-手动，AUTO-自动',
  `current_irrigation_status` VARCHAR(20) NOT NULL DEFAULT 'CLOSED' COMMENT '当前灌溉状态：OPEN-已开启，CLOSED-已关闭',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plot_code` (`plot_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地块表';

-- ============================================================
-- 3. 设备表（device）
-- ============================================================
DROP TABLE IF EXISTS `device`;
CREATE TABLE `device` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `device_code` VARCHAR(50) NOT NULL COMMENT '设备编码',
  `device_name` VARCHAR(100) NOT NULL COMMENT '设备名称',
  `device_type` VARCHAR(20) NOT NULL COMMENT '设备类型：MOISTURE_TEMP-温湿度传感器，IRRIGATION-灌溉控制器',
  `plot_id` BIGINT COMMENT '绑定的地块ID',
  `status` VARCHAR(20) NOT NULL DEFAULT 'OFFLINE' COMMENT '状态：ONLINE-在线，OFFLINE-离线',
  `last_heartbeat` DATETIME COMMENT '最后心跳时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_code` (`device_code`),
  KEY `idx_plot_id` (`plot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- ============================================================
-- 4. 传感器数据表（sensor_data）
-- ============================================================
DROP TABLE IF EXISTS `sensor_data`;
CREATE TABLE `sensor_data` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据ID',
  `device_id` BIGINT NOT NULL COMMENT '设备ID',
  `data_type` VARCHAR(20) NOT NULL COMMENT '数据类型：SOIL_MOISTURE-土壤湿度，AIR_TEMP-空气温度',
  `value` DECIMAL(10,2) NOT NULL COMMENT '数据值',
  `collect_time` DATETIME NOT NULL COMMENT '采集时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_collect_time` (`collect_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='传感器数据表';

-- ============================================================
-- 5. 阈值配置表（threshold_config）
-- ============================================================
DROP TABLE IF EXISTS `threshold_config`;
CREATE TABLE `threshold_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `plot_id` BIGINT NOT NULL COMMENT '地块ID',
  `config_type` VARCHAR(50) NOT NULL COMMENT '配置类型：TEMP_LOW-温度下限，TEMP_HIGH-温度上限，MOISTURE_LOW-湿度下限，MOISTURE_HIGH-湿度上限',
  `threshold_value` DECIMAL(10,2) NOT NULL COMMENT '阈值',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plot_config` (`plot_id`, `config_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='阈值配置表';

-- ============================================================
-- 6. 告警记录表（alert_log）
-- ============================================================
DROP TABLE IF EXISTS `alert_log`;
CREATE TABLE `alert_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '告警ID',
  `device_id` BIGINT COMMENT '设备ID',
  `plot_id` BIGINT NOT NULL COMMENT '地块ID',
  `alert_type` VARCHAR(50) NOT NULL COMMENT '告警类型：TEMP_LOW-温度过低，TEMP_HIGH-温度过高，MOISTURE_LOW-湿度过低，MOISTURE_HIGH-湿度过高，DEVICE_OFFLINE-设备离线',
  `trigger_value` DECIMAL(10,2) COMMENT '触发值',
  `threshold_value` DECIMAL(10,2) COMMENT '阈值',
  `alert_message` VARCHAR(255) NOT NULL COMMENT '告警信息',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待处理，PROCESSING-处理中，DONE-已完成，IGNORED-已忽略',
  `handle_type` VARCHAR(20) COMMENT '处理类型：PROCESS-处理，COMPLETE-完成处理，IGNORE-忽略',
  `handle_time` DATETIME COMMENT '处理时间',
  `operator_id` BIGINT COMMENT '处理人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_plot_id` (`plot_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警记录表';

-- ============================================================
-- 7. 灌溉控制记录表（irrigation_log）
-- ============================================================
DROP TABLE IF EXISTS `irrigation_log`;
CREATE TABLE `irrigation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `plot_id` BIGINT NOT NULL COMMENT '地块ID',
  `operation` VARCHAR(20) NOT NULL COMMENT '操作：OPEN-开启，CLOSE-关闭',
  `operation_source` VARCHAR(20) NOT NULL COMMENT '操作来源：MANUAL-手动，AUTO-自动',
  `msg_id` VARCHAR(100) COMMENT '消息ID（用于匹配MQTT响应）',
  `result` VARCHAR(20) COMMENT '结果：SUCCESS-成功，FAILED-失败',
  `remark` VARCHAR(255) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_plot_id` (`plot_id`),
  KEY `idx_msg_id` (`msg_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='灌溉控制记录表';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 8. 系统告警日志表（system_alert_log）
-- ============================================================
DROP TABLE IF EXISTS `system_alert_log`;
CREATE TABLE `system_alert_log` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    alert_type VARCHAR(50) NOT NULL COMMENT '告警类型',
    alert_level VARCHAR(20) NOT NULL COMMENT '告警级别(INFO/WARN/ERROR)',
    message VARCHAR(500) NOT NULL COMMENT '告警消息内容',
    detail TEXT COMMENT '详细信息(JSON格式)',
    source VARCHAR(200) COMMENT '告警来源(模块/类名)',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '处理状态(PENDING/PROCESSING/DONE/IGNORED)',
    handle_time DATETIME COMMENT '处理时间',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    INDEX idx_alert_type (alert_type),
    INDEX idx_alert_level (alert_level),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统告警日志表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 初始化用户
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role`)
VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'ADMIN'),
('farmer1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三', 'FARMER');

-- 初始化地块
INSERT INTO `plot` (`plot_code`, `plot_name`, `location`, `area`, `user_id`, `irrigation_mode`)
VALUES 
('PLOT001', '一号地块', '东区A栋', 10.5, 2, 'MANUAL'),
('PLOT002', '二号地块', '东区B栋', 8.0, 2, 'AUTO'),
('PLOT003', '三号地块', '西区C栋', 12.0, 2, 'MANUAL');

-- 初始化设备
INSERT INTO `device` (`device_code`, `device_name`, `device_type`, `plot_id`, `status`)
VALUES 
('bearpi01', '温湿度传感器01', 'MOISTURE_TEMP', 1, 'OFFLINE'),
('temp_humisensor_002', '温湿度传感器02', 'MOISTURE_TEMP', 2, 'OFFLINE'),
('temp_humisensor_003', '温湿度传感器03', 'MOISTURE_TEMP', 3, 'OFFLINE'),
('irrigation_001', '灌溉控制器01', 'IRRIGATION', 1, 'OFFLINE'),
('irrigation_002', '灌溉控制器02', 'IRRIGATION', 2, 'OFFLINE'),
('irrigation_003', '灌溉控制器03', 'IRRIGATION', 3, 'OFFLINE');

-- 初始化阈值配置
INSERT INTO `threshold_config` (`plot_id`, `config_type`, `threshold_value`)
VALUES 
(1, 'TEMP_LOW', 5.0),
(1, 'TEMP_HIGH', 35.0),
(1, 'MOISTURE_LOW', 30.0),
(1, 'MOISTURE_HIGH', 70.0),
(2, 'TEMP_LOW', 5.0),
(2, 'TEMP_HIGH', 35.0),
(2, 'MOISTURE_LOW', 35.0),
(2, 'MOISTURE_HIGH', 75.0),
(3, 'TEMP_LOW', 5.0),
(3, 'TEMP_HIGH', 35.0),
(3, 'MOISTURE_LOW', 30.0),
(3, 'MOISTURE_HIGH', 70.0);

-- ============================================================
-- 初始化完成
-- ============================================================
