package com.daijia.provider.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 服务商档案新增 / 修改请求
 */
@Data
public class ProviderSaveRequest implements Serializable {

    /** 关联用户 ID */
    @NotNull(message = "关联用户 ID 不能为空")
    private Long userId;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotBlank(message = "联系电话不能为空")
    private String phone;

    private String idCardNo;//身份证号

    /** 可服务类型，逗号分隔 */
    @NotBlank(message = "可服务类型不能为空")
    private String serviceTypes;

    private String cityCode;//城市编码

    private String vehiclePlate;//车牌号

    private String vehicleModel;//车辆型号

    private Integer drivingYears;//驾驶经验

    private String remark;//备注
}
