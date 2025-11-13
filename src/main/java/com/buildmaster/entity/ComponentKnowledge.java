package com.buildmaster.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 配件知识库模型
 * 用于存储配件相关的知识文档，供RAG检索使用
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "component_knowledge")
public class ComponentKnowledge {

    @Id
    private String id;

    /**
     * 配件ID（关联到Component表）
     */
    private Long componentId;

    /**
     * 配件类型
     */
    private String componentType;

    /**
     * 知识文档内容
     */
    private String content;

    /**
     * 知识来源：manual/crawled/generated
     */
    private String source;

    /**
     * 知识标签
     */
    private List<String> tags;

    /**
     * 向量ID（在Milvus中的ID）
     */
    private String vectorId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 知识评分（用于质量评估）
     */
    private Double score;

    /**
     * 是否已向量化
     */
    private Boolean vectorized;
}

