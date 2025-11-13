package com.buildmaster.repository;

import com.buildmaster.entity.ConversationHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 对话历史Repository
 */
@Repository
public interface ConversationHistoryRepository extends MongoRepository<ConversationHistory, String> {

    /**
     * 根据会话ID查找对话历史
     */
    Optional<ConversationHistory> findBySessionId(String sessionId);

    /**
     * 根据用户ID查找所有对话历史
     */
    List<ConversationHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 根据用户ID和时间范围查找对话历史
     */
    List<ConversationHistory> findByUserIdAndCreatedAtBetween(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * 根据会话ID删除对话历史
     */
    void deleteBySessionId(String sessionId);

    /**
     * 查找最近的N条对话
     */
    List<ConversationHistory> findTop10ByUserIdOrderByUpdatedAtDesc(Long userId);
}

