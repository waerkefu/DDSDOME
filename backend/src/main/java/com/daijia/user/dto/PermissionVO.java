package com.daijia.user.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 权限信息
 */
@Data
public class PermissionVO implements Serializable {

    private Long id;// 权限ID

    private Long parentId;// 父权限ID

    private String permCode;// 权限编码

    private String permName;// 权限名称

    /** 1=目录 2=菜单 3=按钮/接口 */
    private Integer permType;

    private String path;// 路径

    private String component;// 组件路径

    private String icon;// 图标

    private Integer sortNo;// 排序号
}
