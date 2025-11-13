package com.buildmaster.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus向量数据库配置
 */
@Slf4j
@Configuration
public class MilvusConfig {

    @Value("${spring.ai.vectorstore.milvus.client.host:localhost}")
    private String milvusHost;

    @Value("${spring.ai.vectorstore.milvus.client.port:19530}")
    private Integer milvusPort;

    @Value("${spring.ai.vectorstore.milvus.database-name:buildmaster}")
    private String databaseName;

    @Value("${spring.ai.vectorstore.milvus.collection-name:component_knowledge}")
    private String collectionName;

    @Value("${spring.ai.vectorstore.milvus.embedding-dimension:768}")
    private Integer embeddingDimension;

    /**
     * 创建Milvus客户端Bean
     */
    @Bean
    public MilvusServiceClient milvusServiceClient() {
        log.info("初始化Milvus客户端: {}:{}", milvusHost, milvusPort);
        
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost(milvusHost)
                .withPort(milvusPort)
                .withDatabaseName(databaseName)
                .build();

        MilvusServiceClient milvusClient = new MilvusServiceClient(connectParam);
        
        log.info("Milvus客户端初始化成功");
        return milvusClient;
    }

    /**
     * 获取collection名称
     */
    public String getCollectionName() {
        return collectionName;
    }

    /**
     * 获取embedding维度
     */
    public Integer getEmbeddingDimension() {
        return embeddingDimension;
    }
}

