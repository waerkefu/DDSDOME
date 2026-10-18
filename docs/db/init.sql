-- =====================================================================
-- 代驾代办 H5 服务平台 - 数据库初始化脚本（Task 2）
-- 数据库：MySQL 8.x    字符集：utf8mb4    排序规则：utf8mb4_general_ci
-- 说明：
--   1. 本脚本包含【建表 DDL】与【初始化数据 DML】两大部分，可直接整体执行。
--   2. 脚本中未使用物理外键约束，表间关系通过索引与业务层保证（便于分库与逻辑删除）。
--   3. 涉及金额的字段统一使用 INT，单位为「分」，避免浮点精度问题。
--   4. 所有业务表均包含 create_time / update_time / deleted(逻辑删除) 审计字段。
--
-- ⚠️ 警告：脚本开头包含 DROP TABLE，会删除同名表及其数据。
--          仅可在开发/测试环境的初始化阶段执行，禁止在生产库直接运行。
-- =====================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 建议手动创建数据库后再执行本脚本（库名需与 application-dev.yml 中的 DDS 对齐）：
-- CREATE DATABASE IF NOT EXISTS `DDS`
--   DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
-- USE `DDS`;

-- =====================================================================
-- 一、建表 DDL
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. 系统用户表
--    user_type：1=客户  2=服务商  3=管理员
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `username`        VARCHAR(64)  NOT NULL                COMMENT '登录账号',
  `phone`           VARCHAR(20)  NOT NULL                COMMENT '手机号',
  `password`        VARCHAR(100) NOT NULL                COMMENT '密码（PBKDF2 加密）',
  `nickname`        VARCHAR(64)  DEFAULT NULL            COMMENT '昵称/姓名',
  `avatar`          VARCHAR(255) DEFAULT NULL            COMMENT '头像 URL',
  `user_type`       TINYINT      NOT NULL DEFAULT 1      COMMENT '用户类型：1=客户 2=服务商 3=管理员',
  `status`          TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1=启用 0=禁用',
  `last_login_time` DATETIME     DEFAULT NULL            COMMENT '最后登录时间',
  `remark`          VARCHAR(255) DEFAULT NULL            COMMENT '备注',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`         TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_user_type_status` (`user_type`, `status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '系统用户表';

-- ---------------------------------------------------------------------
-- 2. 角色表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `role_code`   VARCHAR(64)  NOT NULL                COMMENT '角色编码（如 CUSTOMER / PROVIDER / ADMIN）',
  `role_name`   VARCHAR(64)  NOT NULL                COMMENT '角色名称',
  `description` VARCHAR(255) DEFAULT NULL            COMMENT '角色描述',
  `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1=启用 0=禁用',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色表';

-- ---------------------------------------------------------------------
-- 3. 用户-角色关联表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id`     BIGINT   NOT NULL                COMMENT '用户 ID',
  `role_id`     BIGINT   NOT NULL                COMMENT '角色 ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户角色关联表';

-- ---------------------------------------------------------------------
-- 4. 权限（菜单/按钮/接口）表
--    perm_type：1=目录 2=菜单 3=按钮/接口
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `parent_id`   BIGINT       NOT NULL DEFAULT 0      COMMENT '父级 ID，0=顶级',
  `perm_code`   VARCHAR(128) NOT NULL                COMMENT '权限编码（如 system:user）',
  `perm_name`   VARCHAR(64)  NOT NULL                COMMENT '权限名称',
  `perm_type`   TINYINT      NOT NULL DEFAULT 2      COMMENT '类型：1=目录 2=菜单 3=按钮/接口',
  `path`        VARCHAR(255) DEFAULT NULL            COMMENT '前端路由路径',
  `component`   VARCHAR(255) DEFAULT NULL            COMMENT '前端组件路径',
  `icon`        VARCHAR(64)  DEFAULT NULL            COMMENT '菜单图标',
  `sort_no`     INT          NOT NULL DEFAULT 0      COMMENT '排序号',
  `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1=启用 0=禁用',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_perm_code` (`perm_code`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '权限表';

-- ---------------------------------------------------------------------
-- 5. 角色-权限关联表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
  `id`            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `role_id`       BIGINT   NOT NULL                COMMENT '角色 ID',
  `permission_id` BIGINT   NOT NULL                COMMENT '权限 ID',
  `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色权限关联表';

-- ---------------------------------------------------------------------
-- 6. 服务商（司机）档案表
--    service_types：逗号分隔，取值 IMMEDIATE/SCHEDULED/INSPECTION/RENEWAL
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `provider`;
CREATE TABLE `provider` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id`        BIGINT        NOT NULL                COMMENT '关联 sys_user.id',
  `provider_no`    VARCHAR(32)   NOT NULL                COMMENT '服务商编号',
  `real_name`      VARCHAR(64)   NOT NULL                COMMENT '真实姓名',
  `phone`          VARCHAR(20)   NOT NULL                COMMENT '联系电话',
  `id_card_no`     VARCHAR(32)   DEFAULT NULL            COMMENT '身份证号',
  `service_types`  VARCHAR(128)  DEFAULT NULL            COMMENT '可服务类型（逗号分隔）',
  `city_code`      VARCHAR(32)   DEFAULT NULL            COMMENT '所属城市编码',
  `vehicle_plate`  VARCHAR(16)   DEFAULT NULL            COMMENT '车辆车牌号',
  `vehicle_model`  VARCHAR(64)   DEFAULT NULL            COMMENT '车辆型号',
  `driving_years`  INT           NOT NULL DEFAULT 0      COMMENT '驾龄（年）',
  `rating`         DECIMAL(3, 2) NOT NULL DEFAULT 5.00   COMMENT '评分（0.00-5.00）',
  `order_count`    INT           NOT NULL DEFAULT 0      COMMENT '累计完成订单数',
  `online_status`  TINYINT       NOT NULL DEFAULT 0      COMMENT '在线状态：0=离线 1=在线',
  `accept_enabled` TINYINT       NOT NULL DEFAULT 0      COMMENT '接单开关：0=停止接单 1=接单中',
  `audit_status`   TINYINT       NOT NULL DEFAULT 0      COMMENT '审核状态：0=待审核 1=通过 2=驳回',
  `status`         TINYINT       NOT NULL DEFAULT 1      COMMENT '状态：1=启用 0=禁用',
  `longitude`      DECIMAL(10, 7) DEFAULT NULL           COMMENT '当前经度',
  `latitude`       DECIMAL(10, 7) DEFAULT NULL           COMMENT '当前纬度',
  `remark`         VARCHAR(255)  DEFAULT NULL            COMMENT '备注',
  `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_provider_user` (`user_id`),
  UNIQUE KEY `uk_provider_no` (`provider_no`),
  KEY `idx_city_accept` (`city_code`, `online_status`, `accept_enabled`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '服务商（司机）档案表';

-- ---------------------------------------------------------------------
-- 7. 用户车辆表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `vehicle`;
CREATE TABLE `vehicle` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id`       BIGINT       NOT NULL                COMMENT '车主用户 ID',
  `plate_no`      VARCHAR(16)  NOT NULL                COMMENT '车牌号',
  `vehicle_type`  VARCHAR(32)  NOT NULL                COMMENT '车型编码（对应计价规则）',
  `vehicle_model` VARCHAR(64)  DEFAULT NULL            COMMENT '车辆型号',
  `color`         VARCHAR(16)  DEFAULT NULL            COMMENT '车辆颜色',
  `is_default`    TINYINT      NOT NULL DEFAULT 0      COMMENT '是否默认车辆：0=否 1=是',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户车辆表';

-- ---------------------------------------------------------------------
-- 8. 用户常用地址表
--    address_type：1=起点 2=终点
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id`        BIGINT        NOT NULL                COMMENT '所属用户 ID',
  `contact_name`   VARCHAR(64)   DEFAULT NULL            COMMENT '联系人',
  `contact_phone`  VARCHAR(20)   DEFAULT NULL            COMMENT '联系电话',
  `address_type`   TINYINT       NOT NULL DEFAULT 1      COMMENT '地址类型：1=起点 2=终点',
  `city_code`      VARCHAR(32)   DEFAULT NULL            COMMENT '城市编码',
  `address_detail` VARCHAR(255)  NOT NULL                COMMENT '详细地址',
  `longitude`      DECIMAL(10, 7) DEFAULT NULL           COMMENT '经度',
  `latitude`       DECIMAL(10, 7) DEFAULT NULL           COMMENT '纬度',
  `is_default`     TINYINT      NOT NULL DEFAULT 0      COMMENT '是否默认：0=否 1=是',
  `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_type` (`user_id`, `address_type`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户常用地址表';

-- ---------------------------------------------------------------------
-- 9. 计价规则表
--    金额字段单位均为「分」；一个「城市 + 车型」维度仅保留一条有效规则
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `pricing_rule`;
CREATE TABLE `pricing_rule` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `rule_name`        VARCHAR(64)   NOT NULL                COMMENT '规则名称',
  `city_code`        VARCHAR(32)   NOT NULL                COMMENT '城市编码',
  `city_name`        VARCHAR(64)   DEFAULT NULL            COMMENT '城市名称',
  `vehicle_type`     VARCHAR(32)   NOT NULL                COMMENT '车型编码',
  `base_price`       INT           NOT NULL DEFAULT 0      COMMENT '起步价（分）',
  `base_distance_m`  INT           NOT NULL DEFAULT 0      COMMENT '起步包含里程（米）',
  `base_duration_s`  INT           NOT NULL DEFAULT 0      COMMENT '起步包含时长（秒）',
  `price_per_km`     INT           NOT NULL DEFAULT 0      COMMENT '超出部分每公里单价（分）',
  `price_per_min`    INT           NOT NULL DEFAULT 0      COMMENT '超出部分每分钟单价（分）',
  `min_price`        INT           NOT NULL DEFAULT 0      COMMENT '最低消费金额（分）',
  `night_start`      TIME          DEFAULT NULL            COMMENT '夜间加价开始时间',
  `night_end`        TIME          DEFAULT NULL            COMMENT '夜间加价结束时间',
  `night_ratio`      DECIMAL(4, 2) NOT NULL DEFAULT 1.00   COMMENT '夜间加价系数',
  `booking_ratio`    DECIMAL(4, 2) NOT NULL DEFAULT 1.00   COMMENT '预约加价系数',
  `effective_time`   DATETIME      DEFAULT NULL            COMMENT '生效时间，为空表示立即生效',
  `status`           TINYINT       NOT NULL DEFAULT 1      COMMENT '状态：1=启用 0=禁用',
  `create_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`          TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_city_vehicle` (`city_code`, `vehicle_type`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '计价规则表';

-- ---------------------------------------------------------------------
-- 10. 订单主表
--     表名使用 orders 以规避 MySQL 保留字 ORDER，对应 spec 中的 order 实体
--
--     order_type ：IMMEDIATE=即时代驾 SCHEDULED=预约代驾 INSPECTION=年审代办 RENEWAL=换证代办
--     status     ：PENDING_ACCEPT 待接单 / ACCEPTED 已接单 / IN_SERVICE 服务中
--                  PENDING_PAYMENT 待支付 / COMPLETED 已完成 / CANCELLED 已取消 / CLOSED 超时关闭
--     pay_status ：UNPAID 未支付 / PAYING 支付中 / PAID 已支付 / REFUNDING 退款中
--                  REFUNDED 已退款 / PARTIAL_REFUNDED 部分退款
--
--     version 字段用于乐观锁：UPDATE ... SET version = version + 1 WHERE version = #{version}
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id`                 BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `order_no`           VARCHAR(32)   NOT NULL                COMMENT '订单号（业务唯一）',
  `order_type`         VARCHAR(20)   NOT NULL                COMMENT '订单类型',
  `status`             VARCHAR(30)   NOT NULL                COMMENT '订单状态',
  `customer_id`        BIGINT        NOT NULL                COMMENT '下单客户 ID',
  `provider_id`        BIGINT        DEFAULT NULL            COMMENT '接单服务商 ID，未接单为空',
  `city_code`          VARCHAR(32)   DEFAULT NULL            COMMENT '城市编码',
  `vehicle_type`       VARCHAR(32)   DEFAULT NULL            COMMENT '车型编码',
  `vehicle_plate`      VARCHAR(16)   DEFAULT NULL            COMMENT '车辆车牌号',
  `contact_name`       VARCHAR(64)   DEFAULT NULL            COMMENT '联系人',
  `contact_phone`      VARCHAR(20)   DEFAULT NULL            COMMENT '联系电话',
  `start_address`      VARCHAR(255)  DEFAULT NULL            COMMENT '起点地址',
  `start_longitude`    DECIMAL(10, 7) DEFAULT NULL           COMMENT '起点经度',
  `start_latitude`     DECIMAL(10, 7) DEFAULT NULL           COMMENT '起点纬度',
  `end_address`        VARCHAR(255)  DEFAULT NULL            COMMENT '终点地址',
  `end_longitude`      DECIMAL(10, 7) DEFAULT NULL           COMMENT '终点经度',
  `end_latitude`       DECIMAL(10, 7) DEFAULT NULL           COMMENT '终点纬度',
  `booking_time`       DATETIME      DEFAULT NULL            COMMENT '预约用车时间（预约单/代办单使用）',
  `accept_time`        DATETIME      DEFAULT NULL            COMMENT '接单时间',
  `service_start_time` DATETIME      DEFAULT NULL            COMMENT '开始服务时间',
  `service_end_time`   DATETIME      DEFAULT NULL            COMMENT '结束服务时间',
  `finish_time`        DATETIME      DEFAULT NULL            COMMENT '订单完成时间',
  `cancel_time`        DATETIME      DEFAULT NULL            COMMENT '取消时间',
  `cancel_reason`      VARCHAR(255)  DEFAULT NULL            COMMENT '取消原因',
  `distance_m`         INT           NOT NULL DEFAULT 0      COMMENT '里程（米），结束服务后为实际里程',
  `duration_s`         INT           NOT NULL DEFAULT 0      COMMENT '时长（秒），结束服务后为实际时长',
  `estimate_amount`    INT           NOT NULL DEFAULT 0      COMMENT '预估金额（分）',
  `actual_amount`      INT           NOT NULL DEFAULT 0      COMMENT '实际结算金额（分）',
  `pay_amount`         INT           NOT NULL DEFAULT 0      COMMENT '已支付金额（分）',
  `refund_amount`      INT           NOT NULL DEFAULT 0      COMMENT '已退款金额（分）',
  `pay_status`         VARCHAR(20)   NOT NULL DEFAULT 'UNPAID' COMMENT '支付状态',
  `remark`             VARCHAR(255)  DEFAULT NULL            COMMENT '客户备注',
  `expire_time`        DATETIME      DEFAULT NULL            COMMENT '待接单超时时间，超时后自动关闭',
  `version`            INT           NOT NULL DEFAULT 0      COMMENT '乐观锁版本号',
  `create_time`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`            TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_customer_status` (`customer_id`, `status`),
  KEY `idx_provider_status` (`provider_id`, `status`),
  KEY `idx_status_expire` (`status`, `expire_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单主表';

-- ---------------------------------------------------------------------
-- 11. 订单状态流转日志表
--     operator_type：1=客户 2=服务商 3=管理员 4=系统
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `order_status_log`;
CREATE TABLE `order_status_log` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `order_id`      BIGINT       NOT NULL                COMMENT '订单 ID',
  `order_no`      VARCHAR(32)  NOT NULL                COMMENT '订单号',
  `from_status`   VARCHAR(30)  DEFAULT NULL            COMMENT '变更前状态，创建订单时为空',
  `to_status`     VARCHAR(30)  NOT NULL                COMMENT '变更后状态',
  `operator_id`   BIGINT       DEFAULT NULL            COMMENT '操作人 ID，系统操作为空',
  `operator_type` TINYINT      NOT NULL DEFAULT 4      COMMENT '操作人类型：1=客户 2=服务商 3=管理员 4=系统',
  `operator_name` VARCHAR(64)  DEFAULT NULL            COMMENT '操作人名称',
  `remark`        VARCHAR(255) DEFAULT NULL            COMMENT '备注',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单状态流转日志表';

-- ---------------------------------------------------------------------
-- 12. 支付单表
--     channel：模拟支付固定为 MOCK，后续可扩展 WECHAT / ALIPAY
--     status ：INIT=初始 PAYING=支付中 SUCCESS=支付成功 FAILED=支付失败
--              CLOSED=已关闭 REFUNDED=已退款
--     idempotency_key：幂等键，数据库唯一约束兜底防止重复支付
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `payment`;
CREATE TABLE `payment` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `pay_no`          VARCHAR(32)  NOT NULL                COMMENT '支付单号',
  `order_id`        BIGINT       NOT NULL                COMMENT '订单 ID',
  `order_no`        VARCHAR(32)  NOT NULL                COMMENT '订单号',
  `customer_id`     BIGINT       NOT NULL                COMMENT '支付客户 ID',
  `amount`          INT          NOT NULL                COMMENT '支付金额（分）',
  `channel`         VARCHAR(20)  NOT NULL DEFAULT 'MOCK' COMMENT '支付渠道：MOCK/WECHAT/ALIPAY',
  `status`          VARCHAR(20)  NOT NULL DEFAULT 'INIT' COMMENT '支付状态',
  `idempotency_key` VARCHAR(64)  NOT NULL                COMMENT '幂等键',
  `trade_no`        VARCHAR(64)  DEFAULT NULL            COMMENT '渠道交易流水号',
  `pay_time`        DATETIME     DEFAULT NULL            COMMENT '支付完成时间',
  `notify_time`     DATETIME     DEFAULT NULL            COMMENT '回调时间',
  `remark`          VARCHAR(255) DEFAULT NULL            COMMENT '备注',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`         TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pay_no` (`pay_no`),
  UNIQUE KEY `uk_idempotency_key` (`idempotency_key`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '支付单表';

-- ---------------------------------------------------------------------
-- 13. 退款单表
--     status：INIT=初始 PROCESSING=退款中 SUCCESS=退款成功 FAILED=退款失败
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `refund`;
CREATE TABLE `refund` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `refund_no`       VARCHAR(32)  NOT NULL                COMMENT '退款单号',
  `payment_id`      BIGINT       NOT NULL                COMMENT '支付单 ID',
  `pay_no`          VARCHAR(32)  NOT NULL                COMMENT '支付单号',
  `order_id`        BIGINT       NOT NULL                COMMENT '订单 ID',
  `order_no`        VARCHAR(32)  NOT NULL                COMMENT '订单号',
  `amount`          INT          NOT NULL                COMMENT '退款金额（分）',
  `reason`          VARCHAR(255) DEFAULT NULL            COMMENT '退款原因',
  `status`          VARCHAR(20)  NOT NULL DEFAULT 'INIT' COMMENT '退款状态',
  `idempotency_key` VARCHAR(64)  NOT NULL                COMMENT '幂等键',
  `refund_time`     DATETIME     DEFAULT NULL            COMMENT '退款完成时间',
  `operator_id`     BIGINT       DEFAULT NULL            COMMENT '操作人 ID',
  `remark`          VARCHAR(255) DEFAULT NULL            COMMENT '备注',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`         TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  UNIQUE KEY `uk_idempotency_key` (`idempotency_key`),
  KEY `idx_payment_id` (`payment_id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '退款单表';

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- 二、初始化数据（DML）
--     所有演示账号的登录密码明文均为：admin123
--     （存储值为 PBKDF2WithHmacSHA256 加密结果，仅供开发测试环境使用）
-- =====================================================================

-- 1. 角色
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `status`) VALUES
  (1, 'CUSTOMER', '客户',   '移动端下单用户', 1),
  (2, 'PROVIDER', '服务商', '接单并提供代驾/代办服务的司机', 1),
  (3, 'ADMIN',    '管理员', '平台运营与管理人员', 1);

-- 2. 权限（管理后台菜单）
INSERT INTO `sys_permission` (`id`, `parent_id`, `perm_code`, `perm_name`, `perm_type`, `path`, `component`, `icon`, `sort_no`, `status`) VALUES
  (1, 0, 'system',           '系统管理',     1, '/system',          NULL,                      'Setting', 1, 1),
  (2, 1, 'system:user',      '用户管理',     2, '/system/user',     'system/user/index',       'User',    1, 1),
  (3, 1, 'system:provider',  '服务商管理',   2, '/system/provider', 'system/provider/index',   'Avatar',  2, 1),
  (4, 1, 'system:role',      '角色权限',     2, '/system/role',     'system/role/index',       'Lock',    3, 1),
  (5, 0, 'order',            '订单管理',     1, '/order',           NULL,                      'List',    2, 1),
  (6, 5, 'order:list',       '订单列表',     2, '/order/list',      'order/list/index',        'Tickets', 1, 1),
  (7, 0, 'pricing',          '计价规则',     1, '/pricing',         NULL,                      'Money',   3, 1),
  (8, 7, 'pricing:rule',     '计价规则管理', 2, '/pricing/rule',    'pricing/rule/index',      'Money',   1, 1);

-- 3. 管理员角色拥有全部权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
  (3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 7), (3, 8);

-- 4. 演示账号
--    密码明文：admin123（PBKDF2 哈希）
INSERT INTO `sys_user` (`id`, `username`, `phone`, `password`, `nickname`, `user_type`, `status`) VALUES
  (1, 'admin',     '13800000000', 'pbkdf2$10000$SSLI7E5PgdG/MAwtJ3Z8/Q==$iaahVhYp1AORvWg7lhkuWxSieRkg4bfmMxCE31iUdzA=', '平台管理员', 3, 1),
  (2, 'customer1', '13900000001', 'pbkdf2$10000$SSLI7E5PgdG/MAwtJ3Z8/Q==$iaahVhYp1AORvWg7lhkuWxSieRkg4bfmMxCE31iUdzA=', '演示客户',   1, 1),
  (3, 'provider1', '13900000002', 'pbkdf2$10000$SSLI7E5PgdG/MAwtJ3Z8/Q==$iaahVhYp1AORvWg7lhkuWxSieRkg4bfmMxCE31iUdzA=', '演示服务商', 2, 1);

INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
  (1, 3),
  (2, 1),
  (3, 2);

-- 5. 演示服务商档案
INSERT INTO `provider` (`id`, `user_id`, `provider_no`, `real_name`, `phone`, `service_types`, `city_code`, `vehicle_plate`, `vehicle_model`, `driving_years`, `rating`, `online_status`, `accept_enabled`, `audit_status`, `status`) VALUES
  (1, 3, 'P20260001', '演示服务商', '13900000002', 'IMMEDIATE,SCHEDULED,INSPECTION,RENEWAL', '310100', '沪A88888', '大众朗逸', 5, 5.00, 1, 1, 1, 1);

-- 6. 示例计价规则（上海）
INSERT INTO `pricing_rule` (`id`, `rule_name`, `city_code`, `city_name`, `vehicle_type`, `base_price`, `base_distance_m`, `base_duration_s`, `price_per_km`, `price_per_min`, `min_price`, `night_start`, `night_end`, `night_ratio`, `booking_ratio`, `status`) VALUES
  (1, '上海-舒适型-标准计价', '310100', '上海市', 'COMFORT', 1800, 3000, 600, 260, 50, 1800, '22:00:00', '06:00:00', 1.30, 1.20, 1),
  (2, '上海-商务型-标准计价', '310100', '上海市', 'BUSINESS', 2500, 3000, 600, 350, 70, 2500, '22:00:00', '06:00:00', 1.30, 1.20, 1);
