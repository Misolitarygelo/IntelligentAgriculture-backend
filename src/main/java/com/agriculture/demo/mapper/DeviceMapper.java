package com.agriculture.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agriculture.demo.entity.Device;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 设备表 Mapper 接口
 */
public interface DeviceMapper extends BaseMapper<Device> {

    /**
     * 查询设备（包括已删除的记录）
     */
    @Select("SELECT * FROM device WHERE device_code = #{deviceCode}")
    Device selectByDeviceCodeIncludeDeleted(@Param("deviceCode") String deviceCode);

    /**
     * 恢复已删除的设备（更新deleted=0）
     */
    @Update("UPDATE device SET deleted = 0, plot_id = #{plotId}, device_name = #{deviceName}, device_type = #{deviceType}, status = #{status} WHERE id = #{id}")
    int restoreDevice(@Param("id") Long id,
            @Param("plotId") Long plotId,
            @Param("deviceName") String deviceName,
            @Param("deviceType") String deviceType,
            @Param("status") String status);
}
