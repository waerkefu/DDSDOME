package com.daijia.provider.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务商（司机）档案
 */
@Data
@TableName("provider")
public class Provider implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联 sys_user.id */
    private Long userId;

    private String providerNo;

    private String realName;

    private String phone;

    private String idCardNo;

    /** 可服务类型，逗号分隔：IMMEDIATE,SCHEDULED,INSPECTION,RENEWAL */
    private String serviceTypes;

    private String cityCode;

    private String vehiclePlate;

    private String vehicleModel;

    private Integer drivingYears;

    private BigDecimal rating;

    private Integer orderCount;

    /** 0=离线 1=在线 */
    private Integer onlineStatus;

    /** 0=停止接单 1=接单中 */
    private Integer acceptEnabled;

    /** 0=待审核 1=通过 2=驳回 */
    private Integer auditStatus;

    /** 1=启用 0=禁用 */
    private Integer status;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    /** 是否可被派单 / 抢单 */
    public boolean canAcceptOrder() {
        return Integer.valueOf(1).equals(status)
                && Integer.valueOf(1).equals(auditStatus)
                && Integer.valueOf(1).equals(onlineStatus)
                && Integer.valueOf(1).equals(acceptEnabled);
    }
}
