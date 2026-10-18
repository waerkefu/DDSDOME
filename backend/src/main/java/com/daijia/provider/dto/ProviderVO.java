package com.daijia.provider.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务商档案信息
 */
@Data
public class ProviderVO implements Serializable {

    private Long id;//主键

    private Long userId;//用户ID

    private String providerNo;//服务商编号

    private String realName;//真实姓名

    private String phone;//联系电话

    private String idCardNo;//身份证号

    /** 可服务类型，逗号分隔 */
    private String serviceTypes;//可服务类型

    /** 可服务类型列表 */
    private List<String> serviceTypeList;

    private String cityCode;//城市编码

    private String vehiclePlate;//车牌号

    private String vehicleModel;//车辆型号

    private Integer drivingYears;//驾驶经验

    private BigDecimal rating;//评分

    private Integer orderCount;//订单数量

    /** 0=离线 1=在线 */
    private Integer onlineStatus;

    /** 0=停止接单 1=接单中 */
    private Integer acceptEnabled;

    /** 0=待审核 1=通过 2=驳回 */
    private Integer auditStatus;

    /** 1=启用 0=禁用 */
    private Integer status;//状态

    private String remark;//备注

    private LocalDateTime createTime;//创建时间

    /** 关联账号 */
    private String username;
}
