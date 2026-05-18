package com.agriculture.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agriculture.demo.entity.SensorData;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 传感器数据表 Mapper 接口
 */
public interface SensorDataMapper extends BaseMapper<SensorData> {

        /**
         * 按秒获取原始数据
         */
        @Select("SELECT collect_time AS time, AVG(value) AS avg_value " +
                        "FROM sensor_data " +
                        "WHERE device_id = #{deviceId} AND data_type = #{dataType} " +
                        "AND collect_time BETWEEN #{startTime} AND #{endTime} " +
                        "GROUP BY collect_time " +
                        "ORDER BY collect_time ASC")
        List<Map<String, Object>> getRawDataBySecond(
                        @Param("deviceId") Long deviceId,
                        @Param("dataType") String dataType,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        /**
         * 按分钟聚合数据（每分钟均值）
         */
        @Select("SELECT DATE_FORMAT(collect_time, '%Y-%m-%d %H:%i:00') AS time, AVG(value) AS avg_value " +
                        "FROM sensor_data " +
                        "WHERE device_id = #{deviceId} AND data_type = #{dataType} " +
                        "AND collect_time BETWEEN #{startTime} AND #{endTime} " +
                        "GROUP BY DATE_FORMAT(collect_time, '%Y-%m-%d %H:%i:00') " +
                        "ORDER BY time ASC")
        List<Map<String, Object>> getAggregatedDataByMinute(
                        @Param("deviceId") Long deviceId,
                        @Param("dataType") String dataType,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        /**
         * 按小时聚合数据（每小时均值）
         */
        @Select("SELECT DATE_FORMAT(collect_time, '%Y-%m-%d %H:00:00') AS time, AVG(value) AS avg_value " +
                        "FROM sensor_data " +
                        "WHERE device_id = #{deviceId} AND data_type = #{dataType} " +
                        "AND collect_time BETWEEN #{startTime} AND #{endTime} " +
                        "GROUP BY DATE_FORMAT(collect_time, '%Y-%m-%d %H:00:00') " +
                        "ORDER BY time ASC")
        List<Map<String, Object>> getAggregatedDataByHour(
                        @Param("deviceId") Long deviceId,
                        @Param("dataType") String dataType,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        /**
         * 按天聚合数据（每天均值）
         */
        @Select("SELECT DATE_FORMAT(collect_time, '%Y-%m-%d 00:00:00') AS time, AVG(value) AS avg_value " +
                        "FROM sensor_data " +
                        "WHERE device_id = #{deviceId} AND data_type = #{dataType} " +
                        "AND collect_time BETWEEN #{startTime} AND #{endTime} " +
                        "GROUP BY DATE_FORMAT(collect_time, '%Y-%m-%d 00:00:00') " +
                        "ORDER BY time ASC")
        List<Map<String, Object>> getAggregatedDataByDay(
                        @Param("deviceId") Long deviceId,
                        @Param("dataType") String dataType,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        /**
         * 获取指定天数的趋势数据（按天聚合）
         */
        @Select("SELECT DATE_FORMAT(collect_time, '%Y-%m-%d') AS date, AVG(value) AS avg_value " +
                        "FROM sensor_data " +
                        "WHERE device_id = #{deviceId} AND data_type = #{dataType} " +
                        "AND collect_time >= #{startTime} " +
                        "GROUP BY DATE_FORMAT(collect_time, '%Y-%m-%d') " +
                        "ORDER BY date ASC")
        List<Map<String, Object>> getTrendDataByDay(
                        @Param("deviceId") Long deviceId,
                        @Param("dataType") String dataType,
                        @Param("startTime") LocalDateTime startTime);
}