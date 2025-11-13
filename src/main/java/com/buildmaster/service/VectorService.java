package com.buildmaster.service;

import com.buildmaster.config.MilvusConfig;
import com.buildmaster.model.ComponentKnowledge;
import com.buildmaster.repository.ComponentKnowledgeRepository;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.SearchResults;
import io.milvus.param.MetricType;
import io.milvus.param.collection.*;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 向量嵌入服务
 * 负责文本向量化和向量检索
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VectorService {

    private final MilvusServiceClient milvusClient;
    private final MilvusConfig milvusConfig;
    private final ComponentKnowledgeRepository knowledgeRepository;
    private final OllamaChatClient ollamaChatClient;

    /**
     * 初始化Milvus collection
     */
    @PostConstruct
    public void initMilvusCollection() {
        String collectionName = milvusConfig.getCollectionName();
        
        try {
            // 检查collection是否存在
            boolean hasCollection = milvusClient.hasCollection(
                    HasCollectionParam.newBuilder()
                            .withCollectionName(collectionName)
                            .build()
            ).getData();

            if (!hasCollection) {
                log.info("创建Milvus collection: {}", collectionName);
                createCollection(collectionName);
            } else {
                log.info("Milvus collection已存在: {}", collectionName);
            }
        } catch (Exception e) {
            log.error("初始化Milvus collection失败", e);
        }
    }

    /**
     * 创建collection
     */
    private void createCollection(String collectionName) {
        try {
            // 定义字段
            FieldType idField = FieldType.newBuilder()
                    .withName("id")
                    .withDataType(DataType.Int64)
                    .withPrimaryKey(true)
                    .withAutoID(true)
                    .build();

            FieldType vectorField = FieldType.newBuilder()
                    .withName("embedding")
                    .withDataType(DataType.FloatVector)
                    .withDimension(milvusConfig.getEmbeddingDimension())
                    .build();

            FieldType contentField = FieldType.newBuilder()
                    .withName("content")
                    .withDataType(DataType.VarChar)
                    .withMaxLength(65535)
                    .build();

            // 创建collection schema
            CreateCollectionParam createCollectionReq = CreateCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withDescription("配件知识库向量存储")
                    .withShardsNum(2)
                    .addFieldType(idField)
                    .addFieldType(vectorField)
                    .addFieldType(contentField)
                    .build();

            milvusClient.createCollection(createCollectionReq);

            // 创建索引
            CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFieldName("embedding")
                    .withIndexType(io.milvus.param.IndexType.IVF_FLAT)
                    .withMetricType(MetricType.L2)
                    .withExtraParam("{\"nlist\":1024}")
                    .build();

            milvusClient.createIndex(indexParam);

            // 加载collection到内存
            milvusClient.loadCollection(LoadCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build());

            log.info("Collection创建成功: {}", collectionName);
        } catch (Exception e) {
            log.error("创建collection失败", e);
            throw new RuntimeException("创建collection失败", e);
        }
    }

    /**
     * 将文本转换为向量
     * 注意：这里使用简单的模拟方法，实际应该使用专门的embedding模型
     */
    public List<Float> textToEmbedding(String text) {
        try {
            // TODO: 实际应该使用专门的embedding模型，如sentence-transformers
            // 这里使用简单的hash方法生成固定维度的向量作为演示
            List<Float> embedding = new ArrayList<>();
            int dimension = milvusConfig.getEmbeddingDimension();
            
            // 使用文本的hash值作为种子
            Random random = new Random(text.hashCode());
            
            for (int i = 0; i < dimension; i++) {
                embedding.add(random.nextFloat());
            }
            
            // 归一化向量
            float norm = 0;
            for (float val : embedding) {
                norm += val * val;
            }
            norm = (float) Math.sqrt(norm);
            
            for (int i = 0; i < embedding.size(); i++) {
                embedding.set(i, embedding.get(i) / norm);
            }
            
            return embedding;
        } catch (Exception e) {
            log.error("文本向量化失败", e);
            throw new RuntimeException("文本向量化失败", e);
        }
    }

    /**
     * 向Milvus插入向量
     */
    public String insertVector(String content, Long componentId, String componentType) {
        try {
            List<Float> embedding = textToEmbedding(content);
            
            // 准备插入数据
            List<List<Float>> vectors = Collections.singletonList(embedding);
            List<String> contents = Collections.singletonList(content);

            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(milvusConfig.getCollectionName())
                    .withFields(Arrays.asList(
                            new InsertParam.Field("embedding", vectors),
                            new InsertParam.Field("content", contents)
                    ))
                    .build();

            var response = milvusClient.insert(insertParam);
            
            if (response.getData() == null || response.getData().getIDs() == null) {
                throw new RuntimeException("插入向量失败：没有返回ID");
            }
            
            String vectorId = String.valueOf(response.getData().getIDs().getIntId().getData(0));
            
            // 保存到MongoDB
            ComponentKnowledge knowledge = ComponentKnowledge.builder()
                    .componentId(componentId)
                    .componentType(componentType)
                    .content(content)
                    .source("manual")
                    .vectorId(vectorId)
                    .vectorized(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            knowledgeRepository.save(knowledge);
            
            log.info("向量插入成功，ID: {}", vectorId);
            return vectorId;
        } catch (Exception e) {
            log.error("插入向量失败", e);
            throw new RuntimeException("插入向量失败", e);
        }
    }

    /**
     * 搜索相似向量
     */
    public List<SearchResult> searchSimilarVectors(String queryText, int topK) {
        try {
            List<Float> queryVector = textToEmbedding(queryText);
            
            SearchParam searchParam = SearchParam.newBuilder()
                    .withCollectionName(milvusConfig.getCollectionName())
                    .withMetricType(MetricType.L2)
                    .withTopK(topK)
                    .withVectors(Collections.singletonList(queryVector))
                    .withVectorFieldName("embedding")
                    .withOutFields(Collections.singletonList("content"))
                    .withParams("{\"nprobe\":10}")
                    .build();

            var response = milvusClient.search(searchParam);
            SearchResults results = response.getData().getResults();
            
            List<SearchResult> searchResults = new ArrayList<>();
            
            if (results.getIdsCount() > 0) {
                for (int i = 0; i < results.getIdsCount(); i++) {
                    long id = results.getIds().getIntId().getData(i);
                    float score = results.getScores(i);
                    
                    // 获取content字段
                    String content = "";
                    if (results.getFieldsDataCount() > 0) {
                        content = results.getFieldsData(0).getScalars().getStringData().getData(i);
                    }
                    
                    searchResults.add(new SearchResult(String.valueOf(id), content, score));
                }
            }
            
            log.info("向量搜索完成，返回{}条结果", searchResults.size());
            return searchResults;
        } catch (Exception e) {
            log.error("向量搜索失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 批量向量化未处理的知识
     */
    public void vectorizeUnprocessedKnowledge() {
        List<ComponentKnowledge> unvectorized = knowledgeRepository.findByVectorizedFalse();
        
        log.info("开始批量向量化，共{}条未处理知识", unvectorized.size());
        
        for (ComponentKnowledge knowledge : unvectorized) {
            try {
                String vectorId = insertVector(
                        knowledge.getContent(),
                        knowledge.getComponentId(),
                        knowledge.getComponentType()
                );
                
                knowledge.setVectorId(vectorId);
                knowledge.setVectorized(true);
                knowledge.setUpdatedAt(LocalDateTime.now());
                knowledgeRepository.save(knowledge);
                
            } catch (Exception e) {
                log.error("向量化知识失败: {}", knowledge.getId(), e);
            }
        }
        
        log.info("批量向量化完成");
    }

    /**
     * 搜索结果内部类
     */
    public static class SearchResult {
        private String id;
        private String content;
        private float score;

        public SearchResult(String id, String content, float score) {
            this.id = id;
            this.content = content;
            this.score = score;
        }

        public String getId() {
            return id;
        }

        public String getContent() {
            return content;
        }

        public float getScore() {
            return score;
        }
    }
}

