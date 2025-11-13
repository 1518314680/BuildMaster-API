-- ============================================================================
-- BuildMaster 配件信息管理系统 - 数据库初始化脚本
-- 数据库：buildmaster
-- 数据库类型：MySQL 8.0+
-- 创建时间：2025-10-20
-- ============================================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS buildmaster CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE buildmaster;

-- ============================================================================
-- 表结构定义
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 用户表 (users)
-- 存储系统用户信息
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    password_hash VARCHAR(255) COMMENT '密码哈希',
    display_name VARCHAR(100) COMMENT '显示名称',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ----------------------------------------------------------------------------
-- 配件表 (components)
-- 存储所有电脑配件信息（CPU/GPU/主板/内存/电源/硬盘/机箱等）
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS components;
CREATE TABLE components (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配件ID',
    name VARCHAR(200) NOT NULL COMMENT '配件名称',
    brand VARCHAR(100) COMMENT '品牌',
    model VARCHAR(100) COMMENT '型号',
    type VARCHAR(50) NOT NULL COMMENT '配件类型：CPU/GPU/MOTHERBOARD/MEMORY/STORAGE/CASE/POWER_SUPPLY/COOLER',
    description TEXT COMMENT '配件描述',
    price DECIMAL(10, 2) NOT NULL COMMENT '当前价格',
    original_price DECIMAL(10, 2) COMMENT '原价',
    image_url VARCHAR(255) COMMENT '图片URL',
    specifications JSON COMMENT '规格参数（JSON格式）',
    specs TEXT COMMENT '规格说明（简化版）',
    is_available BOOLEAN DEFAULT TRUE COMMENT '是否可用',
    stock_quantity INT DEFAULT 0 COMMENT '库存数量',
    jd_sku_id VARCHAR(50) COMMENT '京东商品SKU ID',
    purchase_url TEXT COMMENT '购买链接（京东联盟推广链接）',
    price_updated_at DATETIME COMMENT '价格最后更新时间',
    commission_rate DECIMAL(5, 2) COMMENT '佣金比例',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_type (type),
    INDEX idx_brand (brand),
    INDEX idx_price (price),
    INDEX idx_is_available (is_available),
    INDEX idx_name (name),
    INDEX idx_jd_sku_id (jd_sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配件表';

-- ----------------------------------------------------------------------------
-- 配置单表 (build_configs)
-- 存储用户的装机配置单
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS build_configs;
CREATE TABLE build_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置单ID',
    name VARCHAR(200) NOT NULL COMMENT '配置单名称',
    description TEXT COMMENT '配置单描述',
    total_price DECIMAL(10, 2) COMMENT '总价格',
    user_id BIGINT COMMENT '用户ID',
    is_public BOOLEAN DEFAULT FALSE COMMENT '是否公开',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_is_public (is_public),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配置单表';

-- ----------------------------------------------------------------------------
-- 配置单配件关联表 (build_config_components)
-- 存储配置单中的配件列表
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS build_config_components;
CREATE TABLE build_config_components (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    build_config_id BIGINT NOT NULL COMMENT '配置单ID',
    component_id BIGINT NOT NULL COMMENT '配件ID',
    quantity INT DEFAULT 1 COMMENT '数量',
    unit_price DECIMAL(10, 2) COMMENT '单价（保存时的价格）',
    INDEX idx_build_config_id (build_config_id),
    INDEX idx_component_id (component_id),
    FOREIGN KEY (build_config_id) REFERENCES build_configs(id) ON DELETE CASCADE,
    FOREIGN KEY (component_id) REFERENCES components(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配置单配件关联表';

-- ============================================================================
-- 示例数据插入
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 插入示例用户数据
-- 密码：password (BCrypt 加密)
-- ----------------------------------------------------------------------------
INSERT INTO users (username, email, password_hash, display_name) VALUES
('admin', 'admin@buildmaster.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '管理员'),
('testuser', 'test@buildmaster.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '测试用户'),
('demouser', 'demo@buildmaster.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '演示用户');

