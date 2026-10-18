package com.daijia.user.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限（目录 / 菜单 / 按钮）
 * 映射数据库表sys_permission表
 */
@Data
@TableName("sys_permission")
public class SysPermission implements Serializable {

    @TableId(type = IdType.AUTO)// 主键，自增
    private Long id;// 主键ID

    private Long parentId;// 父权限ID


    private String permCode;// 权限编码

    private String permName;// 权限名称

    /** 1=目录 2=菜单 3=按钮/接口 */
    private Integer permType;

    private String path;// 路径

    private String component;// 组件路径

    private String icon;// 图标

    private Integer sortNo;// 排序号

    private Integer status;// 状态

    // 创建时间
    /*
     * - `INSERT` ： 只在插入 时自动填充`createTime` ；
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 更新时间
    /*
     * `INSERT_UPDATE` ： 插入和更新 都自动填充`updateTime` 。
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 删除标记（0=未删除 1=已删除，非时间戳）
    /*
     * `TableLogic` ： 逻辑删除，删除时将`deleted`字段设置为1。
     */
    @TableLogic
    private Integer deleted;
}