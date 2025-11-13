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
 * 对话历史记录模型
 * 用于在MongoDB中存储用户与AI的对话上下文
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "conversation_history")
public class ConversationHistory {

    @Id
    private String id;

    /**
     * 会话ID（用于关联同一次对话）
     */
    private String sessionId;

    /**
     * 用户ID（可选，用于关联用户）
     */
    private Long userId;

    /**
     * 对话消息列表
     */
    private List<Message> messages;

    /**
     * 对话主题/摘要
     */
    private String topic;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最后更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 对话元数据（如使用的模型、tokens等）
     */
    private Metadata metadata;

    /**
     * 消息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        /**
         * 角色：user/assistant/system
         */
        private String role;

        /**
         * 消息内容
         */
        private String content;

        /**
         * 消息时间戳
         */
        private LocalDateTime timestamp;

        /**
         * 是否使用了RAG检索
         */
        private Boolean usedRAG;

        /**
         * 检索到的相关文档（如果使用了RAG）
         */
        private List<String> retrievedDocuments;
    }

    /**
     * 元数据内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metadata {
        /**
         * 使用的模型名称
         */
        private String model;

        /**
         * 总tokens消耗
         */
        private Integer totalTokens;

        /**
         * 对话轮次
         */
        private Integer turnCount;

        /**
         * 对话类型：recommendation/question/general
         */
        private String conversationType;
    }
}

