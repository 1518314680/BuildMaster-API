-- ============================================================================
-- BuildMaster - 性能优化 SQL 脚本
-- 说明：为配件表添加全文索引和优化查询性能
-- ============================================================================

USE buildmaster;

-- ============================================================================
-- 1. 添加全文索引（Full-Text Index）
-- ============================================================================

-- 检查是否已存在全文索引
-- DROP INDEX idx_fulltext_search ON components;

-- 为 name, brand, description 添加全文索引
-- 注意：MySQL 全文索引支持中文，但效果有限，建议配合 ngram 解析器
ALTER TABLE components 
ADD FULLTEXT INDEX idx_fulltext_search (name, brand, description) 
WITH PARSER ngram;

-- ============================================================================
-- 2. 添加组合索引（提升多条件查询性能）
-- ============================================================================

-- 类型 + 价格 组合索引（常用于筛选）
CREATE INDEX idx_type_price ON components(type, price, is_available);

-- 品牌 + 类型 组合索引
CREATE INDEX idx_brand_type ON components(brand, type, is_available);

-- 价格区间查询优化索引
CREATE INDEX idx_available_price ON components(is_available, price);

-- ============================================================================
-- 3. 查看索引使用情况
-- ============================================================================

-- 查看所有索引
SHOW INDEX FROM components;

-- 查看表状态
SHOW TABLE STATUS LIKE 'components';

-- ============================================================================
-- 4. 查询性能测试 SQL
-- ============================================================================

-- 测试全文搜索性能
EXPLAIN SELECT * FROM components 
WHERE MATCH(name, brand, description) AGAINST('Intel 酷睿 i9' IN BOOLEAN MODE)
AND is_available = TRUE;

-- 测试价格区间查询
EXPLAIN SELECT * FROM components 
WHERE is_available = TRUE 
AND price BETWEEN 1000 AND 5000 
ORDER BY price ASC;

-- 测试类型筛选
EXPLAIN SELECT * FROM components 
WHERE type = 'CPU' 
AND is_available = TRUE 
ORDER BY price ASC;

-- ============================================================================
-- 5. 性能监控查询
-- ============================================================================

-- 查看慢查询日志（需要开启慢查询）
-- SHOW VARIABLES LIKE 'slow_query%';
-- SET GLOBAL slow_query_log = 'ON';
-- SET GLOBAL long_query_time = 1;  -- 1秒以上的查询记录为慢查询

-- 查看表统计信息
SELECT 
    COUNT(*) as total_components,
    COUNT(CASE WHEN is_available = TRUE THEN 1 END) as available_count,
    COUNT(DISTINCT type) as type_count,
    COUNT(DISTINCT brand) as brand_count,
    AVG(price) as avg_price,
    MIN(price) as min_price,
    MAX(price) as max_price
FROM components;

-- ============================================================================
-- 6. 数据清理建议
-- ============================================================================

-- 清理无效数据（可选）
-- UPDATE components SET is_available = FALSE WHERE stock_quantity = 0;

-- 定期更新统计信息（优化查询计划）
ANALYZE TABLE components;

-- ============================================================================
-- 说明
-- ============================================================================
-- 
-- 全文索引使用示例：
-- 
-- 1. 自然语言模式（相关性排序）
--    SELECT * FROM components 
--    WHERE MATCH(name, description) AGAINST('Intel 处理器' IN NATURAL LANGUAGE MODE);
--
-- 2. 布尔模式（精确匹配）
--    SELECT * FROM components 
--    WHERE MATCH(name, description) AGAINST('+Intel +i9 -AMD' IN BOOLEAN MODE);
--    
-- 3. 查询扩展模式（智能联想）
--    SELECT * FROM components 
--    WHERE MATCH(name, description) AGAINST('游戏' WITH QUERY EXPANSION);
--
-- 性能提升预期：
-- - 简单关键词搜索：10-50倍提升
-- - 复杂多条件查询：3-10倍提升
-- - 分页查询：5-20倍提升
--
-- ============================================================================

