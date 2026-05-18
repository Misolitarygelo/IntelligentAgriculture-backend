package com.agriculture.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 数据库初始化配置类
 * 应用启动时重建所有表并插入测试数据
 */
@Component
public class DatabaseInitConfig implements CommandLineRunner {

  private static final Logger logger = LoggerFactory.getLogger(DatabaseInitConfig.class);

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Override
  public void run(String... args) throws Exception {
    logger.info("开始初始化数据库（重建表结构）...");

    try {
      // 先关闭外键检查
      jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

      // 删除所有旧表
      String[] tableNames = {
          "irrigation_log", "alert_log", "threshold_config",
          "sensor_data", "device", "plot", "sys_user"
      };

      for (String tableName : tableNames) {
        try {
          jdbcTemplate.execute("DROP TABLE IF EXISTS `" + tableName + "`");
          logger.info("删除表: {}", tableName);
        } catch (Exception e) {
          logger.debug("删除表失败（可能不存在）: {}", tableName);
        }
      }

      // 重新创建所有表
      createAllTables();

      // 插入测试数据
      insertTestData();

      // 恢复外键检查
      jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

      logger.info("数据库初始化完成！所有表已重建并插入测试数据！");
    } catch (Exception e) {
      logger.error("初始化数据库时出错: {}", e.getMessage(), e);
    }
  }

  /**
   * 创建所有表
   */
  private void createAllTables() {
    // sys_user
    jdbcTemplate.execute(
        "CREATE TABLE `sys_user` (" +
            "  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID'," +
            "  `username` VARCHAR(50) NOT NULL COMMENT '用户名'," +
            "  `password` VARCHAR(100) NOT NULL COMMENT '密码（加密）'," +
            "  `real_name` VARCHAR(50) COMMENT '真实姓名'," +
            "  `role` VARCHAR(20) NOT NULL COMMENT '角色：FARMER/ADMIN/SYSTEM_ADMIN'," +
            "  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
            "  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
            "  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记'," +
            "  PRIMARY KEY (`id`)," +
            "  UNIQUE KEY `uk_username` (`username`)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表'");

    // plot
    jdbcTemplate.execute(
        "CREATE TABLE `plot` (" +
            "  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '地块ID'," +
            "  `plot_code` VARCHAR(50) NOT NULL COMMENT '地块编码'," +
            "  `plot_name` VARCHAR(100) NOT NULL COMMENT '地块名称'," +
            "  `location` VARCHAR(200) COMMENT '位置描述'," +
            "  `area` DECIMAL(10,2) COMMENT '面积（亩）'," +
            "  `user_id` BIGINT NOT NULL COMMENT '所属农户ID'," +
            "  `irrigation_mode` VARCHAR(20) NOT NULL DEFAULT 'MANUAL' COMMENT '灌溉模式'," +
            "  `current_irrigation_status` VARCHAR(20) NOT NULL DEFAULT 'CLOSED' COMMENT '当前灌溉状态'," +
            "  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
            "  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
            "  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记'," +
            "  PRIMARY KEY (`id`)," +
            "  UNIQUE KEY `uk_plot_code` (`plot_code`)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地块表'");

    // device
    jdbcTemplate.execute(
        "CREATE TABLE `device` (" +
            "  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '设备ID'," +
            "  `device_code` VARCHAR(50) NOT NULL COMMENT '设备编码'," +
            "  `device_name` VARCHAR(100) NOT NULL COMMENT '设备名称'," +
            "  `device_type` VARCHAR(20) NOT NULL COMMENT '设备类型'," +
            "  `plot_id` BIGINT COMMENT '绑定的地块ID'," +
            "  `status` VARCHAR(20) NOT NULL DEFAULT 'OFFLINE' COMMENT '状态'," +
            "  `last_heartbeat` DATETIME COMMENT '最后心跳时间'," +
            "  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
            "  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
            "  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记'," +
            "  PRIMARY KEY (`id`)," +
            "  UNIQUE KEY `uk_device_code` (`device_code`)," +
            "  KEY `idx_plot_id` (`plot_id`)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表'");

    // sensor_data
    jdbcTemplate.execute(
        "CREATE TABLE `sensor_data` (" +
            "  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据ID'," +
            "  `device_id` BIGINT NOT NULL COMMENT '设备ID'," +
            "  `data_type` VARCHAR(20) NOT NULL COMMENT '数据类型'," +
            "  `value` DECIMAL(10,2) NOT NULL COMMENT '数据值'," +
            "  `collect_time` DATETIME NOT NULL COMMENT '采集时间'," +
            "  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
            "  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记'," +
            "  PRIMARY KEY (`id`)," +
            "  KEY `idx_device_id` (`device_id`)," +
            "  KEY `idx_collect_time` (`collect_time`)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='传感器数据表'");

    // threshold_config
    jdbcTemplate.execute(
        "CREATE TABLE `threshold_config` (" +
            "  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID'," +
            "  `plot_id` BIGINT NOT NULL COMMENT '地块ID'," +
            "  `config_type` VARCHAR(50) NOT NULL COMMENT '配置类型'," +
            "  `threshold_value` DECIMAL(10,2) NOT NULL COMMENT '阈值'," +
            "  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
            "  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
            "  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记'," +
            "  PRIMARY KEY (`id`)," +
            "  UNIQUE KEY `uk_plot_config` (`plot_id`, `config_type`)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='阈值配置表'");

    // alert_log
    jdbcTemplate.execute(
        "CREATE TABLE `alert_log` (" +
            "  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '告警ID'," +
            "  `device_id` BIGINT COMMENT '设备ID'," +
            "  `plot_id` BIGINT NOT NULL COMMENT '地块ID'," +
            "  `alert_type` VARCHAR(50) NOT NULL COMMENT '告警类型'," +
            "  `trigger_value` DECIMAL(10,2) COMMENT '触发值'," +
            "  `threshold_value` DECIMAL(10,2) COMMENT '阈值'," +
            "  `alert_message` VARCHAR(255) NOT NULL COMMENT '告警信息'," +
            "  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态'," +
            "  `handle_type` VARCHAR(20) COMMENT '处理类型'," +
            "  `handle_time` DATETIME COMMENT '处理时间'," +
            "  `operator_id` BIGINT COMMENT '处理人ID'," +
            "  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
            "  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
            "  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记'," +
            "  PRIMARY KEY (`id`)," +
            "  KEY `idx_plot_id` (`plot_id`)," +
            "  KEY `idx_status` (`status`)," +
            "  KEY `idx_create_time` (`create_time`)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警记录表'");

    // irrigation_log
    jdbcTemplate.execute(
        "CREATE TABLE `irrigation_log` (" +
            "  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID'," +
            "  `plot_id` BIGINT NOT NULL COMMENT '地块ID'," +
            "  `operation` VARCHAR(20) NOT NULL COMMENT '操作'," +
            "  `operation_source` VARCHAR(20) NOT NULL COMMENT '操作来源'," +
            "  `msg_id` VARCHAR(100) COMMENT '消息ID'," +
            "  `result` VARCHAR(20) COMMENT '结果'," +
            "  `remark` VARCHAR(255) COMMENT '备注'," +
            "  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
            "  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记'," +
            "  PRIMARY KEY (`id`)," +
            "  KEY `idx_plot_id` (`plot_id`)," +
            "  KEY `idx_msg_id` (`msg_id`)," +
            "  KEY `idx_create_time` (`create_time`)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='灌溉控制记录表'");
  }

  /**
   * 插入测试数据
   */
  private void insertTestData() {
    // 插入用户
    jdbcTemplate.update(
        "INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role`) VALUES (?, ?, ?, ?)",
        "admin", "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi", "系统管理员", "ADMIN");
    jdbcTemplate.update(
        "INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role`) VALUES (?, ?, ?, ?)",
        "farmer1", "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi", "张三", "FARMER");

    // 插入地块
    jdbcTemplate.update(
        "INSERT INTO `plot` (`plot_code`, `plot_name`, `location`, `area`, `user_id`, `irrigation_mode`) VALUES (?, ?, ?, ?, ?, ?)",
        "PLOT001", "一号地块", "东区A栋", 10.5, 2L, "MANUAL");
    jdbcTemplate.update(
        "INSERT INTO `plot` (`plot_code`, `plot_name`, `location`, `area`, `user_id`, `irrigation_mode`) VALUES (?, ?, ?, ?, ?, ?)",
        "PLOT002", "二号地块", "东区B栋", 8.0, 2L, "AUTO");
    jdbcTemplate.update(
        "INSERT INTO `plot` (`plot_code`, `plot_name`, `location`, `area`, `user_id`, `irrigation_mode`) VALUES (?, ?, ?, ?, ?, ?)",
        "PLOT003", "三号地块", "西区C栋", 12.0, 2L, "MANUAL");

    // 插入设备
    jdbcTemplate.update(
        "INSERT INTO `device` (`device_code`, `device_name`, `device_type`, `plot_id`, `status`) VALUES (?, ?, ?, ?, ?)",
        "bearpi01", "温湿度传感器01", "MOISTURE_TEMP", 1L, "OFFLINE");
    jdbcTemplate.update(
        "INSERT INTO `device` (`device_code`, `device_name`, `device_type`, `plot_id`, `status`) VALUES (?, ?, ?, ?, ?)",
        "temp_humisensor_002", "温湿度传感器02", "MOISTURE_TEMP", 2L, "OFFLINE");
    jdbcTemplate.update(
        "INSERT INTO `device` (`device_code`, `device_name`, `device_type`, `plot_id`, `status`) VALUES (?, ?, ?, ?, ?)",
        "temp_humisensor_003", "温湿度传感器03", "MOISTURE_TEMP", 3L, "OFFLINE");
    jdbcTemplate.update(
        "INSERT INTO `device` (`device_code`, `device_name`, `device_type`, `plot_id`, `status`) VALUES (?, ?, ?, ?, ?)",
        "irrigation_001", "灌溉控制器01", "IRRIGATION", 1L, "OFFLINE");
    jdbcTemplate.update(
        "INSERT INTO `device` (`device_code`, `device_name`, `device_type`, `plot_id`, `status`) VALUES (?, ?, ?, ?, ?)",
        "irrigation_002", "灌溉控制器02", "IRRIGATION", 2L, "OFFLINE");
    jdbcTemplate.update(
        "INSERT INTO `device` (`device_code`, `device_name`, `device_type`, `plot_id`, `status`) VALUES (?, ?, ?, ?, ?)",
        "irrigation_003", "灌溉控制器03", "IRRIGATION", 3L, "OFFLINE");

    // 插入阈值配置
    jdbcTemplate.update(
        "INSERT INTO `threshold_config` (`plot_id`, `config_type`, `threshold_value`) VALUES (1, 'TEMP_LOW', 5.0)");
    jdbcTemplate.update(
        "INSERT INTO `threshold_config` (`plot_id`, `config_type`, `threshold_value`) VALUES (1, 'TEMP_HIGH', 35.0)");
    jdbcTemplate.update(
        "INSERT INTO `threshold_config` (`plot_id`, `config_type`, `threshold_value`) VALUES (1, 'MOISTURE_LOW', 30.0)");
    jdbcTemplate.update(
        "INSERT INTO `threshold_config` (`plot_id`, `config_type`, `threshold_value`) VALUES (1, 'MOISTURE_HIGH', 70.0)");
    jdbcTemplate.update(
        "INSERT INTO `threshold_config` (`plot_id`, `config_type`, `threshold_value`) VALUES (2, 'TEMP_LOW', 5.0)");
    jdbcTemplate.update(
        "INSERT INTO `threshold_config` (`plot_id`, `config_type`, `threshold_value`) VALUES (2, 'TEMP_HIGH', 35.0)");
    jdbcTemplate.update(
        "INSERT INTO `threshold_config` (`plot_id`, `config_type`, `threshold_value`) VALUES (2, 'MOISTURE_LOW', 35.0)");
    jdbcTemplate.update(
        "INSERT INTO `threshold_config` (`plot_id`, `config_type`, `threshold_value`) VALUES (2, 'MOISTURE_HIGH', 75.0)");
    jdbcTemplate.update(
        "INSERT INTO `threshold_config` (`plot_id`, `config_type`, `threshold_value`) VALUES (3, 'TEMP_LOW', 5.0)");
    jdbcTemplate.update(
        "INSERT INTO `threshold_config` (`plot_id`, `config_type`, `threshold_value`) VALUES (3, 'TEMP_HIGH', 35.0)");
    jdbcTemplate.update(
        "INSERT INTO `threshold_config` (`plot_id`, `config_type`, `threshold_value`) VALUES (3, 'MOISTURE_LOW', 30.0)");
    jdbcTemplate.update(
        "INSERT INTO `threshold_config` (`plot_id`, `config_type`, `threshold_value`) VALUES (3, 'MOISTURE_HIGH', 70.0)");
  }
}
