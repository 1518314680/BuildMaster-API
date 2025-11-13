package com.buildmaster.service;

import com.buildmaster.model.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 京东开放平台API服务
 * 文档: https://union.jd.com/openplatform/
 * 
 * 主要功能：
 * 1. 商品搜索
 * 2. 商品详情查询
 * 3. 价格查询
 * 4. 推广链接生成
 */
@Slf4j
@Service
public class JDApiService {

    @Value("${jd.api.app-key:}")
    private String appKey;

    @Value("${jd.api.app-secret:}")
    private String appSecret;

    @Value("${jd.api.site-id:}")
    private String siteId;  // 推广位ID

    @Value("${jd.api.enabled:false}")
    private boolean apiEnabled;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String JD_API_URL = "https://router.jd.com/api";
    private static final String JD_PRICE_API = "https://p.3.cn/prices/mgets";

    /**
     * 检查API是否已配置
     */
    public boolean isApiConfigured() {
        return apiEnabled && appKey != null && !appKey.isEmpty() 
               && appSecret != null && !appSecret.isEmpty();
    }

    /**
     * 根据关键词搜索商品
     * 
     * @param keyword 关键词（如: "Intel i7-13700K"）
     * @param pageSize 返回数量
     * @return 商品列表
     */
    @Cacheable(value = "jd-search", key = "#keyword + '-' + #pageSize")
    public List<Component> searchProducts(String keyword, int pageSize) {
        if (!isApiConfigured()) {
            log.warn("京东API未配置，返回空列表");
            return Collections.emptyList();
        }

        try {
            // 构建API请求参数
            Map<String, String> params = new TreeMap<>();
            params.put("method", "jd.union.open.goods.query");
            params.put("app_key", appKey);
            params.put("timestamp", String.valueOf(System.currentTimeMillis()));
            params.put("format", "json");
            params.put("v", "1.0");
            params.put("sign_method", "md5");
            
            // 业务参数
            Map<String, Object> bizParams = new HashMap<>();
            bizParams.put("keyword", keyword);
            bizParams.put("pageSize", pageSize);
            bizParams.put("pageIndex", 1);
            
            String paramJson = objectMapper.writeValueAsString(bizParams);
            params.put("360buy_param_json", paramJson);
            
            // 生成签名
            String sign = generateSign(params);
            params.put("sign", sign);
            
            // 构建URL
            String url = buildUrl(JD_API_URL, params);
            
            log.info("调用京东API搜索: {}", keyword);
            
            // 发送请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return parseSearchResponse(response.body());
            } else {
                log.error("京东API调用失败，状态码: {}", response.statusCode());
                return Collections.emptyList();
            }
            
        } catch (Exception e) {
            log.error("京东API搜索失败: {}", keyword, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取商品详情
     * 
     * @param skuId 京东商品ID
     * @return 配件信息
     */
    @Cacheable(value = "jd-product", key = "#skuId")
    public Component getProductDetail(String skuId) {
        if (!isApiConfigured()) {
            log.warn("京东API未配置");
            return null;
        }

        try {
            Map<String, String> params = new TreeMap<>();
            params.put("method", "jd.union.open.goods.promotiongoodsinfo.query");
            params.put("app_key", appKey);
            params.put("timestamp", String.valueOf(System.currentTimeMillis()));
            params.put("format", "json");
            params.put("v", "1.0");
            params.put("sign_method", "md5");
            
            Map<String, Object> bizParams = new HashMap<>();
            bizParams.put("skuIds", Collections.singletonList(skuId));
            
            String paramJson = objectMapper.writeValueAsString(bizParams);
            params.put("360buy_param_json", paramJson);
            
            String sign = generateSign(params);
            params.put("sign", sign);
            
            String url = buildUrl(JD_API_URL, params);
            
            log.info("获取京东商品详情: {}", skuId);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return parseProductDetail(response.body());
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("获取商品详情失败: {}", skuId, e);
            return null;
        }
    }

    /**
     * 获取商品价格
     * 
     * @param skuId 京东商品ID
     * @return 价格
     */
    @Cacheable(value = "jd-price", key = "#skuId", unless = "#result == null")
    public BigDecimal getPrice(String skuId) {
        if (!isApiConfigured()) {
            return null;
        }

        try {
            // 价格查询API（不需要签名）
            String url = String.format("%s?skuIds=J_%s", JD_PRICE_API, skuId);
            
            log.info("查询京东价格: {}", skuId);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return parsePriceResponse(response.body());
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("获取价格失败: {}", skuId, e);
            return null;
        }
    }

    /**
     * 批量更新价格
     * 
     * @param skuIds SKU ID列表
     * @return SKU ID -> 价格映射
     */
    public Map<String, BigDecimal> batchGetPrices(List<String> skuIds) {
        Map<String, BigDecimal> prices = new HashMap<>();
        
        if (!isApiConfigured() || skuIds == null || skuIds.isEmpty()) {
            return prices;
        }

        try {
            // 每次最多查询100个
            int batchSize = 100;
            for (int i = 0; i < skuIds.size(); i += batchSize) {
                int end = Math.min(i + batchSize, skuIds.size());
                List<String> batch = skuIds.subList(i, end);
                
                String skuIdsParam = batch.stream()
                    .map(id -> "J_" + id)
                    .collect(Collectors.joining(","));
                
                String url = String.format("%s?skuIds=%s", JD_PRICE_API, skuIdsParam);
                
                log.info("批量查询价格: {} 个商品", batch.size());
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    prices.putAll(parseBatchPriceResponse(response.body()));
                }
                
                // 避免频率限制
                if (i + batchSize < skuIds.size()) {
                    Thread.sleep(500);
                }
            }
        } catch (Exception e) {
            log.error("批量获取价格失败", e);
        }
        
        return prices;
    }

    /**
     * 生成推广链接
     * 
     * @param skuId 商品ID
     * @return 推广链接
     */
    public String generatePromotionUrl(String skuId) {
        if (!isApiConfigured()) {
            return null;
        }

        try {
            Map<String, String> params = new TreeMap<>();
            params.put("method", "jd.union.open.promotion.common.get");
            params.put("app_key", appKey);
            params.put("timestamp", String.valueOf(System.currentTimeMillis()));
            params.put("format", "json");
            params.put("v", "1.0");
            params.put("sign_method", "md5");
            
            Map<String, Object> bizParams = new HashMap<>();
            bizParams.put("materialId", "https://item.jd.com/" + skuId + ".html");
            bizParams.put("siteId", siteId);
            
            String paramJson = objectMapper.writeValueAsString(bizParams);
            params.put("360buy_param_json", paramJson);
            
            String sign = generateSign(params);
            params.put("sign", sign);
            
            String url = buildUrl(JD_API_URL, params);
            
            log.info("生成推广链接: {}", skuId);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return parsePromotionUrl(response.body());
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("生成推广链接失败: {}", skuId, e);
            return null;
        }
    }

    /**
     * 生成API签名
     */
    private String generateSign(Map<String, String> params) {
        try {
            // 1. 参数排序
            String sortedParams = params.entrySet().stream()
                .filter(e -> !"sign".equals(e.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + e.getValue())
                .collect(Collectors.joining());
            
            // 2. 拼接secret
            String signStr = appSecret + sortedParams + appSecret;
            
            // 3. MD5加密
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(signStr.getBytes(StandardCharsets.UTF_8));
            
            // 4. 转大写hex
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
            }
            
            return sb.toString();
            
        } catch (Exception e) {
            log.error("生成签名失败", e);
            return "";
        }
    }

    /**
     * 构建URL
     */
    private String buildUrl(String baseUrl, Map<String, String> params) {
        String queryString = params.entrySet().stream()
            .map(e -> {
                try {
                    return e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8);
                } catch (Exception ex) {
                    return e.getKey() + "=" + e.getValue();
                }
            })
            .collect(Collectors.joining("&"));
        
        return baseUrl + "?" + queryString;
    }

    /**
     * 解析搜索响应
     */
    private List<Component> parseSearchResponse(String responseBody) {
        List<Component> components = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode resultNode = root.path("jd_union_open_goods_query_response")
                                      .path("result");
            
            if (resultNode.isMissingNode()) {
                log.warn("响应中没有result字段");
                return components;
            }
            
            String resultStr = resultNode.asText();
            JsonNode resultJson = objectMapper.readTree(resultStr);
            JsonNode dataList = resultJson.path("data");
            
            if (dataList.isArray()) {
                for (JsonNode item : dataList) {
                    Component component = parseProductItem(item);
                    if (component != null) {
                        components.add(component);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("解析搜索响应失败", e);
        }
        
        return components;
    }

    /**
     * 解析商品详情
     */
    private Component parseProductDetail(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode resultNode = root.path("jd_union_open_goods_promotiongoodsinfo_query_response")
                                      .path("result");
            
            if (!resultNode.isMissingNode()) {
                String resultStr = resultNode.asText();
                JsonNode resultJson = objectMapper.readTree(resultStr);
                JsonNode dataList = resultJson.path("data");
                
                if (dataList.isArray() && dataList.size() > 0) {
                    return parseProductItem(dataList.get(0));
                }
            }
            
        } catch (Exception e) {
            log.error("解析商品详情失败", e);
        }
        
        return null;
    }

    /**
     * 解析单个商品项
     */
    private Component parseProductItem(JsonNode item) {
        try {
            Component component = new Component();
            
            JsonNode skuInfo = item.path("skuInfo");
            JsonNode priceInfo = item.path("priceInfo");
            JsonNode imageInfo = item.path("imageInfo");
            
            // 基本信息
            component.setJdSkuId(skuInfo.path("skuId").asText());
            component.setName(skuInfo.path("skuName").asText());
            component.setBrand(skuInfo.path("brandName").asText());
            
            // 价格信息
            if (!priceInfo.isMissingNode()) {
                BigDecimal price = priceInfo.path("price").decimalValue();
                BigDecimal lowestPrice = priceInfo.path("lowestPrice").decimalValue();
                
                component.setPrice(lowestPrice != null ? lowestPrice : price);
                component.setOriginalPrice(price);
                component.setPriceUpdatedAt(LocalDateTime.now());
                
                // 佣金信息
                JsonNode commissionInfo = item.path("commissionInfo");
                if (!commissionInfo.isMissingNode()) {
                    component.setCommissionRate(commissionInfo.path("commissionShare").decimalValue());
                }
            }
            
            // 图片
            if (!imageInfo.isMissingNode()) {
                JsonNode imageList = imageInfo.path("imageList");
                if (imageList.isArray() && imageList.size() > 0) {
                    component.setImageUrl(imageList.get(0).path("url").asText());
                }
            }
            
            // 库存
            component.setIsAvailable(true);
            component.setStockQuantity(999);
            
            return component;
            
        } catch (Exception e) {
            log.error("解析商品项失败", e);
            return null;
        }
    }

    /**
     * 解析价格响应
     */
    private BigDecimal parsePriceResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            if (root.isArray() && root.size() > 0) {
                JsonNode firstItem = root.get(0);
                String priceStr = firstItem.path("p").asText();
                if (priceStr != null && !priceStr.isEmpty()) {
                    return new BigDecimal(priceStr);
                }
            }
        } catch (Exception e) {
            log.error("解析价格响应失败", e);
        }
        
        return null;
    }

    /**
     * 解析批量价格响应
     */
    private Map<String, BigDecimal> parseBatchPriceResponse(String responseBody) {
        Map<String, BigDecimal> prices = new HashMap<>();
        
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            if (root.isArray()) {
                for (JsonNode item : root) {
                    String id = item.path("id").asText();
                    String priceStr = item.path("p").asText();
                    
                    if (id != null && priceStr != null && !priceStr.isEmpty()) {
                        // 移除 "J_" 前缀
                        String skuId = id.replace("J_", "");
                        prices.put(skuId, new BigDecimal(priceStr));
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析批量价格响应失败", e);
        }
        
        return prices;
    }

    /**
     * 解析推广链接
     */
    private String parsePromotionUrl(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode resultNode = root.path("jd_union_open_promotion_common_get_response")
                                      .path("result");
            
            if (!resultNode.isMissingNode()) {
                String resultStr = resultNode.asText();
                JsonNode resultJson = objectMapper.readTree(resultStr);
                JsonNode data = resultJson.path("data");
                
                return data.path("clickURL").asText();
            }
            
        } catch (Exception e) {
            log.error("解析推广链接失败", e);
        }
        
        return null;
    }
}
