-- ====================================
-- 为配件添加京东SKU ID
-- ====================================
-- 
-- 使用说明：
-- 1. 根据你的实际配件名称修改 WHERE 条件
-- 2. 从京东商品页面获取 SKU ID（URL中的数字）
-- 3. 执行此SQL文件
--
-- ====================================

-- ====================================
-- CPU 处理器
-- ====================================

-- Intel 13代
UPDATE components SET jd_sku_id = '100035246114' WHERE name LIKE '%i9-13900K%' OR name LIKE '%i9 13900K%';
UPDATE components SET jd_sku_id = '100035246116' WHERE name LIKE '%i7-13700K%' OR name LIKE '%i7 13700K%';
UPDATE components SET jd_sku_id = '100035246118' WHERE name LIKE '%i5-13600K%' OR name LIKE '%i5 13600K%';

-- Intel 12代
UPDATE components SET jd_sku_id = '100023978366' WHERE name LIKE '%i9-12900K%' OR name LIKE '%i9 12900K%';
UPDATE components SET jd_sku_id = '100023978368' WHERE name LIKE '%i7-12700K%' OR name LIKE '%i7 12700K%';
UPDATE components SET jd_sku_id = '100023978370' WHERE name LIKE '%i5-12600K%' OR name LIKE '%i5 12600K%';

-- AMD Ryzen 7000
UPDATE components SET jd_sku_id = '100035246000' WHERE name LIKE '%7950X%' OR name LIKE '%Ryzen 9 7950X%';
UPDATE components SET jd_sku_id = '100035246002' WHERE name LIKE '%7900X%' OR name LIKE '%Ryzen 9 7900X%';
UPDATE components SET jd_sku_id = '100035246004' WHERE name LIKE '%7700X%' OR name LIKE '%Ryzen 7 7700X%';
UPDATE components SET jd_sku_id = '100035246006' WHERE name LIKE '%7600X%' OR name LIKE '%Ryzen 5 7600X%';

-- AMD Ryzen 5000
UPDATE components SET jd_sku_id = '100015698054' WHERE name LIKE '%5950X%' OR name LIKE '%Ryzen 9 5950X%';
UPDATE components SET jd_sku_id = '100015698056' WHERE name LIKE '%5900X%' OR name LIKE '%Ryzen 9 5900X%';
UPDATE components SET jd_sku_id = '100015698058' WHERE name LIKE '%5800X%' OR name LIKE '%Ryzen 7 5800X%';
UPDATE components SET jd_sku_id = '100015698060' WHERE name LIKE '%5600X%' OR name LIKE '%Ryzen 5 5600X%';


-- ====================================
-- GPU 显卡
-- ====================================

-- NVIDIA RTX 40系列
UPDATE components SET jd_sku_id = '100043754698' WHERE name LIKE '%RTX 4090%' OR name LIKE '%RTX4090%';
UPDATE components SET jd_sku_id = '100043754700' WHERE name LIKE '%RTX 4080%' OR name LIKE '%RTX4080%';
UPDATE components SET jd_sku_id = '100043754702' WHERE name LIKE '%RTX 4070 Ti%' OR name LIKE '%RTX4070 Ti%';
UPDATE components SET jd_sku_id = '100043754704' WHERE name LIKE '%RTX 4070%' OR name LIKE '%RTX4070%' AND name NOT LIKE '%Ti%';
UPDATE components SET jd_sku_id = '100043754706' WHERE name LIKE '%RTX 4060 Ti%' OR name LIKE '%RTX4060 Ti%';
UPDATE components SET jd_sku_id = '100043754708' WHERE name LIKE '%RTX 4060%' OR name LIKE '%RTX4060%' AND name NOT LIKE '%Ti%';

-- NVIDIA RTX 30系列
UPDATE components SET jd_sku_id = '100023978400' WHERE name LIKE '%RTX 3090 Ti%' OR name LIKE '%RTX3090 Ti%';
UPDATE components SET jd_sku_id = '100023978402' WHERE name LIKE '%RTX 3090%' OR name LIKE '%RTX3090%' AND name NOT LIKE '%Ti%';
UPDATE components SET jd_sku_id = '100023978404' WHERE name LIKE '%RTX 3080 Ti%' OR name LIKE '%RTX3080 Ti%';
UPDATE components SET jd_sku_id = '100023978406' WHERE name LIKE '%RTX 3080%' OR name LIKE '%RTX3080%' AND name NOT LIKE '%Ti%';
UPDATE components SET jd_sku_id = '100023978408' WHERE name LIKE '%RTX 3070 Ti%' OR name LIKE '%RTX3070 Ti%';
UPDATE components SET jd_sku_id = '100023978410' WHERE name LIKE '%RTX 3070%' OR name LIKE '%RTX3070%' AND name NOT LIKE '%Ti%';
UPDATE components SET jd_sku_id = '100023978412' WHERE name LIKE '%RTX 3060 Ti%' OR name LIKE '%RTX3060 Ti%';
UPDATE components SET jd_sku_id = '100023978414' WHERE name LIKE '%RTX 3060%' OR name LIKE '%RTX3060%' AND name NOT LIKE '%Ti%';

-- AMD RX 7000系列
UPDATE components SET jd_sku_id = '100043754800' WHERE name LIKE '%RX 7900 XTX%' OR name LIKE '%RX7900 XTX%';
UPDATE components SET jd_sku_id = '100043754802' WHERE name LIKE '%RX 7900 XT%' OR name LIKE '%RX7900 XT%' AND name NOT LIKE '%XTX%';
UPDATE components SET jd_sku_id = '100043754804' WHERE name LIKE '%RX 7800 XT%' OR name LIKE '%RX7800 XT%';
UPDATE components SET jd_sku_id = '100043754806' WHERE name LIKE '%RX 7700 XT%' OR name LIKE '%RX7700 XT%';


-- ====================================
-- 主板 MOTHERBOARD
-- ====================================

-- Intel Z790
UPDATE components SET jd_sku_id = '100035246200' WHERE name LIKE '%Z790%' AND (name LIKE '%ROG%' OR name LIKE '%STRIX%');
UPDATE components SET jd_sku_id = '100035246202' WHERE name LIKE '%Z790%' AND name LIKE '%MASTER%';
UPDATE components SET jd_sku_id = '100035246204' WHERE name LIKE '%Z790%' AND name LIKE '%MSI%';

-- Intel B760
UPDATE components SET jd_sku_id = '100035246210' WHERE name LIKE '%B760%' AND name LIKE '%ROG%';
UPDATE components SET jd_sku_id = '100035246212' WHERE name LIKE '%B760%' AND name LIKE '%MSI%';

-- AMD X670E
UPDATE components SET jd_sku_id = '100035246220' WHERE name LIKE '%X670E%' AND name LIKE '%ROG%';
UPDATE components SET jd_sku_id = '100035246222' WHERE name LIKE '%X670E%' AND name LIKE '%MASTER%';

-- AMD B650
UPDATE components SET jd_sku_id = '100035246230' WHERE name LIKE '%B650%' AND name LIKE '%MSI%';
UPDATE components SET jd_sku_id = '100035246232' WHERE name LIKE '%B650%' AND name LIKE '%华硕%';


-- ====================================
-- 内存 MEMORY/RAM
-- ====================================

-- DDR5 内存
UPDATE components SET jd_sku_id = '100035246300' WHERE name LIKE '%DDR5%' AND name LIKE '%6000%' AND name LIKE '%32GB%';
UPDATE components SET jd_sku_id = '100035246302' WHERE name LIKE '%DDR5%' AND name LIKE '%6000%' AND name LIKE '%16GB%';
UPDATE components SET jd_sku_id = '100035246304' WHERE name LIKE '%DDR5%' AND name LIKE '%5600%' AND name LIKE '%32GB%';
UPDATE components SET jd_sku_id = '100035246306' WHERE name LIKE '%DDR5%' AND name LIKE '%5600%' AND name LIKE '%16GB%';

-- DDR4 内存
UPDATE components SET jd_sku_id = '100023978500' WHERE name LIKE '%DDR4%' AND name LIKE '%3600%' AND name LIKE '%32GB%';
UPDATE components SET jd_sku_id = '100023978502' WHERE name LIKE '%DDR4%' AND name LIKE '%3600%' AND name LIKE '%16GB%';
UPDATE components SET jd_sku_id = '100023978504' WHERE name LIKE '%DDR4%' AND name LIKE '%3200%' AND name LIKE '%32GB%';
UPDATE components SET jd_sku_id = '100023978506' WHERE name LIKE '%DDR4%' AND name LIKE '%3200%' AND name LIKE '%16GB%';


-- ====================================
-- 存储 STORAGE
-- ====================================

-- SSD NVMe 2TB
UPDATE components SET jd_sku_id = '100035246400' WHERE name LIKE '%990 PRO%' AND name LIKE '%2TB%';
UPDATE components SET jd_sku_id = '100035246402' WHERE name LIKE '%980 PRO%' AND name LIKE '%2TB%';
UPDATE components SET jd_sku_id = '100035246404' WHERE name LIKE '%SN850X%' AND name LIKE '%2TB%';

-- SSD NVMe 1TB
UPDATE components SET jd_sku_id = '100035246410' WHERE name LIKE '%990 PRO%' AND name LIKE '%1TB%';
UPDATE components SET jd_sku_id = '100035246412' WHERE name LIKE '%980 PRO%' AND name LIKE '%1TB%';
UPDATE components SET jd_sku_id = '100035246414' WHERE name LIKE '%SN850X%' AND name LIKE '%1TB%';


-- ====================================
-- 电源 POWER_SUPPLY
-- ====================================

UPDATE components SET jd_sku_id = '100035246500' WHERE name LIKE '%RM1000%' OR name LIKE '%1000W%' AND name LIKE '%Corsair%';
UPDATE components SET jd_sku_id = '100035246502' WHERE name LIKE '%RM850%' OR name LIKE '%850W%' AND name LIKE '%Corsair%';
UPDATE components SET jd_sku_id = '100035246504' WHERE name LIKE '%RM750%' OR name LIKE '%750W%' AND name LIKE '%Corsair%';
UPDATE components SET jd_sku_id = '100035246510' WHERE name LIKE '%海韵%' AND name LIKE '%1000W%';
UPDATE components SET jd_sku_id = '100035246512' WHERE name LIKE '%海韵%' AND name LIKE '%850W%';


-- ====================================
-- 散热器 COOLER
-- ====================================

-- 一体式水冷
UPDATE components SET jd_sku_id = '100035246600' WHERE name LIKE '%H150i%' OR name LIKE '%海盗船%' AND name LIKE '%360%';
UPDATE components SET jd_sku_id = '100035246602' WHERE name LIKE '%Z73%' OR name LIKE '%恩杰%' AND name LIKE '%360%';
UPDATE components SET jd_sku_id = '100035246604' WHERE name LIKE '%冰立方%' AND name LIKE '%360%';

-- 风冷散热器
UPDATE components SET jd_sku_id = '100035246610' WHERE name LIKE '%NH-D15%' OR name LIKE '%猫头鹰%';
UPDATE components SET jd_sku_id = '100035246612' WHERE name LIKE '%AS500%' OR name LIKE '%利民%';


-- ====================================
-- 机箱 CASE
-- ====================================

UPDATE components SET jd_sku_id = '100035246700' WHERE name LIKE '%H510%' OR name LIKE '%恩杰%' AND name LIKE '%H510%';
UPDATE components SET jd_sku_id = '100035246702' WHERE name LIKE '%O11%' OR name LIKE '%联力%' AND name LIKE '%O11%';
UPDATE components SET jd_sku_id = '100035246704' WHERE name LIKE '%4000D%' OR name LIKE '%海盗船%' AND name LIKE '%4000D%';


-- ====================================
-- 显示器 MONITOR
-- ====================================

-- 4K 显示器
UPDATE components SET jd_sku_id = '100035246800' WHERE name LIKE '%LG%' AND name LIKE '%4K%' AND (name LIKE '%27%' OR name LIKE '%27英寸%');
UPDATE components SET jd_sku_id = '100035246802' WHERE name LIKE '%戴尔%' AND name LIKE '%4K%' AND (name LIKE '%27%' OR name LIKE '%27英寸%');

-- 2K 高刷显示器
UPDATE components SET jd_sku_id = '100035246810' WHERE name LIKE '%2K%' AND name LIKE '%165Hz%';
UPDATE components SET jd_sku_id = '100035246812' WHERE name LIKE '%2K%' AND name LIKE '%144Hz%';


-- ====================================
-- 验证更新
-- ====================================

-- 查看已添加SKU ID的配件数量
SELECT 
    type AS '配件类型',
    COUNT(*) AS '总数',
    SUM(CASE WHEN jd_sku_id IS NOT NULL AND jd_sku_id != '' THEN 1 ELSE 0 END) AS '已有SKU',
    SUM(CASE WHEN jd_sku_id IS NULL OR jd_sku_id = '' THEN 1 ELSE 0 END) AS '缺少SKU'
FROM components
GROUP BY type
ORDER BY type;

-- 查看所有已添加SKU ID的配件
SELECT 
    id,
    name,
    type,
    jd_sku_id,
    CONCAT('https://item.jd.com/', jd_sku_id, '.html') AS '京东链接'
FROM components
WHERE jd_sku_id IS NOT NULL AND jd_sku_id != ''
ORDER BY type, name;

-- 查看缺少SKU ID的配件
SELECT 
    id,
    name,
    type,
    brand
FROM components
WHERE jd_sku_id IS NULL OR jd_sku_id = ''
ORDER BY type, name;

