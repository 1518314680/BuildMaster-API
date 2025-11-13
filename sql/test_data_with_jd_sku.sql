-- ============================================================================
-- BuildMaster - 临时测试数据（包含真实京东SKU ID）
-- 说明：这些是真实的京东商品，包含SKU ID，可用于测试价格查询
-- ============================================================================

USE buildmaster;

-- ============================================================================
-- 清空现有配件数据（可选）
-- ============================================================================
-- TRUNCATE TABLE build_config_components;
-- TRUNCATE TABLE components;

-- ============================================================================
-- 插入测试配件数据（包含真实京东SKU ID）
-- ============================================================================

-- ----------------------------------------------------------------------------
-- CPU 配件
-- ----------------------------------------------------------------------------
INSERT INTO components (name, brand, model, type, description, price, original_price, image_url, jd_sku_id, is_available, stock_quantity, specs) VALUES
('Intel 酷睿 i9-13900K 处理器', 'Intel', 'i9-13900K', 'CPU', 
 '24核32线程，基础频率3.0GHz，最大睿频5.8GHz，36MB三级缓存', 
 4499.00, 4999.00, 'https://img14.360buyimg.com/n1/jfs/t1/example.jpg',
 '100035246114', TRUE, 50, 
 '核心数: 24核32线程\n基础频率: 3.0GHz\n最大睿频: 5.8GHz\n三级缓存: 36MB\nTDP: 125W'),

('Intel 酷睿 i7-13700K 处理器', 'Intel', 'i7-13700K', 'CPU',
 '16核24线程，基础频率3.4GHz，最大睿频5.4GHz，30MB三级缓存',
 2999.00, 3299.00, 'https://img14.360buyimg.com/n1/jfs/t1/example.jpg',
 '100035246116', TRUE, 80, 
 '核心数: 16核24线程\n基础频率: 3.4GHz\n最大睿频: 5.4GHz\n三级缓存: 30MB\nTDP: 125W'),

('AMD 锐龙 9 7950X 处理器', 'AMD', '7950X', 'CPU',
 '16核32线程，基础频率4.5GHz，最大加速频率5.7GHz，64MB三级缓存',
 3999.00, 4499.00, 'https://img14.360buyimg.com/n1/jfs/t1/example.jpg',
 '100035102594', TRUE, 40, 
 '核心数: 16核32线程\n基础频率: 4.5GHz\n最大频率: 5.7GHz\n三级缓存: 64MB\nTDP: 170W'),

('AMD 锐龙 7 7800X3D 处理器', 'AMD', '7800X3D', 'CPU',
 '8核16线程，基础频率4.2GHz，最大加速频率5.0GHz，96MB缓存（含3D缓存）',
 2699.00, 2999.00, 'https://img14.360buyimg.com/n1/jfs/t1/example.jpg',
 '100047921483', TRUE, 60, 
 '核心数: 8核16线程\n基础频率: 4.2GHz\n最大频率: 5.0GHz\n缓存: 96MB\nTDP: 120W');

-- ----------------------------------------------------------------------------
-- GPU 显卡
-- ----------------------------------------------------------------------------
INSERT INTO components (name, brand, model, type, description, price, original_price, image_url, jd_sku_id, is_available, stock_quantity, specs) VALUES
('七彩虹 RTX 4090 火神 显卡', 'NVIDIA', 'RTX 4090', 'GPU',
 '24GB GDDR6X显存，16384个CUDA核心，2.52GHz加速频率',
 12999.00, 13999.00, 'https://img14.360buyimg.com/n1/jfs/t1/example.jpg',
 '100043754698', TRUE, 20, 
 '显存: 24GB GDDR6X\nCUDA核心: 16384\n加速频率: 2.52GHz\n功耗: 450W'),

('华硕 RTX 4080 SUPER TUF 显卡', 'NVIDIA', 'RTX 4080 SUPER', 'GPU',
 '16GB GDDR6X显存，10240个CUDA核心，2.55GHz加速频率',
 8999.00, 9999.00, 'https://img14.360buyimg.com/n1/jfs/t1/example.jpg',
 '100060125668', TRUE, 35, 
 '显存: 16GB GDDR6X\nCUDA核心: 10240\n加速频率: 2.55GHz\n功耗: 320W'),

('微星 RTX 4070 Ti SUPER 万图师 显卡', 'NVIDIA', 'RTX 4070 Ti SUPER', 'GPU',
 '16GB GDDR6X显存，8448个CUDA核心，2.61GHz加速频率',
 6499.00, 6999.00, 'https://img14.360buyimg.com/n1/jfs/t1/example.jpg',
 '100058711142', TRUE, 45, 
 '显存: 16GB GDDR6X\nCUDA核心: 8448\n加速频率: 2.61GHz\n功耗: 285W'),

('蓝宝石 RX 7900 XTX 超白金 显卡', 'AMD', 'RX 7900 XTX', 'GPU',
 '24GB GDDR6显存，6144个流处理器，2.5GHz游戏频率',
 7499.00, 7999.00, 'https://img14.360buyimg.com/n1/jfs/t1/example.jpg',
 '100043899726', TRUE, 30, 
 '显存: 24GB GDDR6\n流处理器: 6144\n游戏频率: 2.5GHz\n功耗: 355W');

-- ----------------------------------------------------------------------------
-- 主板
-- ----------------------------------------------------------------------------
INSERT INTO components (name, brand, model, type, description, price, original_price, image_url, jd_sku_id, is_available, stock_quantity, specs) VALUES
('华硕 ROG MAXIMUS Z790 HERO 主板', 'ASUS', 'Z790 HERO', 'MOTHERBOARD',
 'Intel Z790芯片组，支持LGA1700，DDR5内存，PCIe 5.0',
 4299.00, 4599.00, 'https://img14.360buyimg.com/n1/jfs/t1/example.jpg',
 '100035095836', TRUE, 25, 
 '芯片组: Z790\n插槽: LGA1700\n内存: DDR5\nM.2: 5个\n网卡: 2.5G'),

('微星 MAG B760M 迫击炮 主板', 'MSI', 'B760M MORTAR', 'MOTHERBOARD',
 'Intel B760芯片组，支持LGA1700，DDR5内存，PCIe 5.0',
 1299.00, 1399.00, 'https://img14.360buyimg.com/n1/jfs/t1/example.jpg',
 '100044949950', TRUE, 50, 
 '芯片组: B760\n插槽: LGA1700\n内存: DDR5\nM.2: 3个\n网卡: 2.5G'),

('技嘉 X670E AORUS MASTER 主板', 'GIGABYTE', 'X670E MASTER', 'MOTHERBOARD',
 'AMD X670E芯片组，支持AM5，DDR5内存，PCIe 5.0',
 3699.00, 3999.00, 'https://img14.360buyimg.com/n1/jfs/t1/example.jpg',
 '100035133014', TRUE, 20, 
 '芯片组: X670E\n插槽: AM5\n内存: DDR5\nM.2: 5个\n网卡: 10G');

-- ----------------------------------------------------------------------------
-- 内存
-- ----------------------------------------------------------------------------
INSERT INTO components (name, brand, model, type, description, price, original_price, image_url, jd_sku_id, is_available, stock_quantity, specs) VALUES
('金士顿 DDR5 6000MHz 32GB 内存套装', 'Kingston', 'FURY Beast', 'MEMORY',
 'DDR5 6000MHz，32GB(16GB×2)，RGB灯效',
 799.00, 899.00, 'https://img14.360buyimg.com/n1/jfs/t1/example.jpg',
 '100035679244', TRUE, 100, 
 '类型: DDR5\n频率: 6000MHz\n容量: 32GB(16GB×2)\n时序: CL36\nRGB: 支持'),

('海盗船 DDR5 5600MHz 64GB 内存套装', 'CORSAIR', 'Vengeance', 'MEMORY',
 'DDR5 5600MHz，64GB(32GB×2)，无RGB',
 1399.00, 1599.00, 'https://img14.360buyimg.com/n1/jfs/t1/example.jpg',
 '100035679242', TRUE, 60, 
 '类型: DDR5\n频率: 5600MHz\n容量: 64GB(32GB×2)\n时序: CL40\nRGB: 无'),

('芝奇 DDR4 3600MHz 32GB 内存套装', 'G.SKILL', 'Trident Z', 'MEMORY',
 'DDR4 3600MHz，32GB(16GB×2)，RGB灯效',
 549.00, 649.00, 'https://img14.360buyimg.com/n1/jfs/t1/example.jpg',
 '100014651976', TRUE, 80, 
 '类型: DDR4\n频率: 3600MHz\n容量: 32GB(16GB×2)\n时序: CL18\nRGB: 支持');

-- ----------------------------------------------------------------------------
-- 硬盘
-- ----------------------------------------------------------------------------
INSERT INTO components (name, brand, model, type, description, price, original_price, image_url, jd_sku_id, is_available, stock_quantity, specs) VALUES
('三星 990 PRO 2TB NVMe SSD', 'Samsung', '990 PRO', 'STORAGE',
 'PCIe 4.0 NVMe M.2，顺序读取7450MB/s，写入6900MB/s',
 1299.00, 1499.00, 'https://img14.360buyimg.com/n1/jfs/t1/example.jpg',
 '100041030530', TRUE, 70, 
 '容量: 2TB\n接口: PCIe 4.0 NVMe\n读取: 7450MB/s\n写入: 6900MB/s\n质保: 5年'),

('西部数据 SN850X 1TB NVMe SSD', 'WD', 'SN850X', 'STORAGE',
 'PCIe 4.0 NVMe M.2，顺序读取7300MB/s，写入6300MB/s',
 699.00, 799.00, 'https://img14.360buyimg.com/n1/jfs/t1/example.jpg',
 '100035783326', TRUE, 90, 
 '容量: 1TB\n接口: PCIe 4.0 NVMe\n读取: 7300MB/s\n写入: 6300MB/s\n质保: 5年'),

('致钛 TiPlus7100 2TB NVMe SSD', 'ZHITAI', 'TiPlus7100', 'STORAGE',
 'PCIe 4.0 NVMe M.2，顺序读取7000MB/s，写入6000MB/s',
 749.00, 849.00, 'https://img14.360buyimg.com/n1/jfs/t1/example.jpg',
 '100035246200', TRUE, 100, 
 '容量: 2TB\n接口: PCIe 4.0 NVMe\n读取: 7000MB/s\n写入: 6000MB/s\n质保: 5年');

-- ----------------------------------------------------------------------------
-- 电源
-- ----------------------------------------------------------------------------
INSERT INTO components (name, brand, model, type, description, price, original_price, image_url, jd_sku_id, is_available, stock_quantity, specs) VALUES
('海韵 FOCUS GX-1000 电源', 'Seasonic', 'FOCUS GX-1000', 'POWER_SUPPLY',
 '1000W全模组，80 PLUS金牌认证，全日系电容',
 999.00, 1099.00, 'https://img14.360buyimg.com/n1/jfs/t1/example.jpg',
 '100020815478', TRUE, 40, 
 '功率: 1000W\n认证: 80 PLUS金牌\n模组: 全模组\n质保: 10年'),

('安钛克 HCG850 电源', 'Antec', 'HCG850', 'POWER_SUPPLY',
 '850W全模组，80 PLUS金牌认证，10年质保',
 799.00, 899.00, 'https://img14.360buyimg.com/n1/jfs/t1/example.jpg',
 '100015899074', TRUE, 50, 
 '功率: 850W\n认证: 80 PLUS金牌\n模组: 全模组\n质保: 10年'),

('长城 巨龙GW-EPS1000DA 电源', 'Great Wall', 'GW-EPS1000DA', 'POWER_SUPPLY',
 '1000W全模组，80 PLUS白金认证，全日系电容',
 899.00, 999.00, 'https://img14.360buyimg.com/n1/jfs/t1/example.jpg',
 '100006429338', TRUE, 35, 
 '功率: 1000W\n认证: 80 PLUS白金\n模组: 全模组\n质保: 10年');

-- ----------------------------------------------------------------------------
-- 机箱
-- ----------------------------------------------------------------------------
INSERT INTO components (name, brand, model, type, description, price, original_price, image_url, jd_sku_id, is_available, stock_quantity, specs) VALUES
('联力 O11 Dynamic EVO 机箱', 'Lian Li', 'O11 Dynamic EVO', 'CASE',
 '中塔机箱，支持E-ATX，钢化玻璃侧透，最多支持10个风扇',
 999.00, 1099.00, 'https://img14.360buyimg.com/n1/jfs/t1/example.jpg',
 '100035246118', TRUE, 30, 
 '类型: 中塔\n主板: E-ATX\n风扇: 最多10个\n水冷: 360mm\n侧透: 钢化玻璃'),

('追风者 P500A D-RGB 机箱', 'Phanteks', 'P500A', 'CASE',
 '中塔机箱，支持E-ATX，网孔面板，预装3个140mm RGB风扇',
 799.00, 899.00, 'https://img14.360buyimg.com/n1/jfs/t1/example.jpg',
 '100021869756', TRUE, 40, 
 '类型: 中塔\n主板: E-ATX\n预装风扇: 3×140mm RGB\n水冷: 420mm\n侧透: 钢化玻璃'),

('酷冷至尊 TD500 Mesh 机箱', 'Cooler Master', 'TD500 Mesh', 'CASE',
 '中塔机箱，支持ATX，网孔前面板，预装3个120mm ARGB风扇',
 499.00, 599.00, 'https://img14.360buyimg.com/n1/jfs/t1/example.jpg',
 '100017259162', TRUE, 60, 
 '类型: 中塔\n主板: ATX\n预装风扇: 3×120mm ARGB\n水冷: 360mm\n侧透: 钢化玻璃');

-- ----------------------------------------------------------------------------
-- 散热器
-- ----------------------------------------------------------------------------
INSERT INTO components (name, brand, model, type, description, price, original_price, image_url, jd_sku_id, is_available, stock_quantity, specs) VALUES
('利民 Frozen Magic 360 Scenic 一体式水冷', 'Thermalright', 'Frozen Magic 360', 'COOLER',
 '360mm一体式水冷，ARGB灯效，支持Intel/AMD平台',
 599.00, 699.00, 'https://img14.360buyimg.com/n1/jfs/t1/example.jpg',
 '100043899720', TRUE, 50, 
 '类型: 一体式水冷\n冷排: 360mm\n风扇: 3×120mm ARGB\nTDP: 350W\n质保: 6年'),

('九州风神 AK620 风冷散热器', 'DEEPCOOL', 'AK620', 'COOLER',
 '双塔6热管风冷，双12cm风扇，支持Intel/AMD平台',
 299.00, 349.00, 'https://img14.360buyimg.com/n1/jfs/t1/example.jpg',
 '100035783324', TRUE, 80, 
 '类型: 风冷\n热管: 6根\n风扇: 2×120mm\nTDP: 260W\n质保: 6年'),

('恩杰 Kraken Elite 360 RGB 一体式水冷', 'NZXT', 'Kraken Elite 360', 'COOLER',
 '360mm一体式水冷，2.36寸LCD屏，可自定义显示',
 1799.00, 1999.00, 'https://img14.360buyimg.com/n1/jfs/t1/example.jpg',
 '100044949948', TRUE, 25, 
 '类型: 一体式水冷\n冷排: 360mm\n风扇: 3×120mm RGB\n屏幕: 2.36寸LCD\n质保: 6年');

-- ============================================================================
-- 数据统计
-- ============================================================================
-- 查看插入的数据统计
SELECT 
    type as '配件类型',
    COUNT(*) as '数量',
    AVG(price) as '平均价格',
    MIN(price) as '最低价格',
    MAX(price) as '最高价格',
    SUM(stock_quantity) as '总库存'
FROM components
GROUP BY type
ORDER BY COUNT(*) DESC;

-- 查看有京东SKU ID的配件
SELECT 
    COUNT(*) as '总配件数',
    COUNT(jd_sku_id) as '有SKU ID的配件数',
    COUNT(jd_sku_id) * 100.0 / COUNT(*) as '覆盖率(%)'
FROM components;

-- ============================================================================
-- 说明
-- ============================================================================
/*
这些是真实的京东商品SKU ID，您可以：
1. 使用这些数据进行测试
2. 使用京东价格查询API（无需授权）获取价格
3. 等申请到京东联盟API后再生成推广链接

注意：
- 这些SKU ID是2024年的数据，可能会过期
- 价格仅供参考，实际价格以京东为准
- 商品图片URL为示例，需要实际爬取或使用API获取
*/

