package com.buildmaster.controller;

import com.buildmaster.model.ConversationHistory;
import com.buildmaster.service.AIService;
import com.buildmaster.service.VectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * AI智能助手控制器
 * 提供对话、推荐、知识管理等API
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI智能助手", description = "AI对话、配件推荐、知识管理接口")
public class AIController {

    private final AIService aiService;
    private final VectorService vectorService;

    /**
     * 普通聊天接口
     */
    @PostMapping("/chat")
    @Operation(summary = "AI对话", description = "与AI助手进行对话")
    public ResponseEntity<ChatResponseDTO> chat(@RequestBody ChatRequestDTO request) {
        try {
            String sessionId = request.getSessionId() != null ? 
                    request.getSessionId() : UUID.randomUUID().toString();
            
            ChatResponse response = aiService.chat(request.getMessage(), sessionId);
            
            ChatResponseDTO dto = new ChatResponseDTO();
            dto.setSessionId(sessionId);
            dto.setMessage(response.getResult().getOutput().getContent());
            dto.setUsedRAG(false);
            
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("Chat失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * RAG聊天接口
     */
    @PostMapping("/chat/rag")
    @Operation(summary = "RAG对话", description = "使用检索增强生成的AI对话")
    public ResponseEntity<ChatResponseDTO> chatWithRAG(@RequestBody RAGChatRequestDTO request) {
        try {
            String sessionId = request.getSessionId() != null ? 
                    request.getSessionId() : UUID.randomUUID().toString();
            
            int topK = request.getTopK() != null ? request.getTopK() : 5;
            
            ChatResponse response = aiService.chatWithRAG(request.getMessage(), sessionId, topK);
            
            ChatResponseDTO dto = new ChatResponseDTO();
            dto.setSessionId(sessionId);
            dto.setMessage(response.getResult().getOutput().getContent());
            dto.setUsedRAG(true);
            
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("RAG Chat失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 智能推荐接口
     */
    @PostMapping("/recommend")
    @Operation(summary = "智能推荐", description = "根据用户需求推荐配件组合")
    public ResponseEntity<RecommendationResponseDTO> recommend(@RequestBody RecommendationRequestDTO request) {
        try {
            String recommendation = aiService.recommendBuildConfig(
                    request.getRequirement(),
                    request.getBudget()
            );
            
            RecommendationResponseDTO dto = new RecommendationResponseDTO();
            dto.setRecommendation(recommendation);
            dto.setRequirement(request.getRequirement());
            dto.setBudget(request.getBudget());
            
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("推荐失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 添加知识到知识库
     */
    @PostMapping("/knowledge/add")
    @Operation(summary = "添加知识", description = "向知识库添加新的配件知识")
    public ResponseEntity<String> addKnowledge(@RequestBody KnowledgeAddRequestDTO request) {
        try {
            aiService.learnNewKnowledge(
                    request.getContent(),
                    request.getComponentId(),
                    request.getComponentType()
            );
            return ResponseEntity.ok("知识添加成功");
        } catch (Exception e) {
            log.error("添加知识失败", e);
            return ResponseEntity.internalServerError().body("添加知识失败: " + e.getMessage());
        }
    }

    /**
     * 批量向量化未处理的知识
     */
    @PostMapping("/knowledge/vectorize")
    @Operation(summary = "批量向量化", description = "批量向量化未处理的知识")
    public ResponseEntity<String> vectorizeKnowledge() {
        try {
            vectorService.vectorizeUnprocessedKnowledge();
            return ResponseEntity.ok("批量向量化完成");
        } catch (Exception e) {
            log.error("批量向量化失败", e);
            return ResponseEntity.internalServerError().body("批量向量化失败: " + e.getMessage());
        }
    }

    /**
     * 搜索相似知识
     */
    @PostMapping("/knowledge/search")
    @Operation(summary = "搜索知识", description = "在知识库中搜索相似内容")
    public ResponseEntity<List<VectorService.SearchResult>> searchKnowledge(@RequestBody KnowledgeSearchRequestDTO request) {
        try {
            int topK = request.getTopK() != null ? request.getTopK() : 10;
            List<VectorService.SearchResult> results = vectorService.searchSimilarVectors(
                    request.getQuery(),
                    topK
            );
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("搜索知识失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取用户对话历史
     */
    @GetMapping("/conversations/{userId}")
    @Operation(summary = "获取对话历史", description = "获取用户的所有对话历史")
    public ResponseEntity<List<ConversationHistory>> getConversations(@PathVariable Long userId) {
        try {
            List<ConversationHistory> conversations = aiService.getUserConversations(userId);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            log.error("获取对话历史失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 删除对话历史
     */
    @DeleteMapping("/conversations/{sessionId}")
    @Operation(summary = "删除对话", description = "删除指定的对话历史")
    public ResponseEntity<String> deleteConversation(@PathVariable String sessionId) {
        try {
            aiService.deleteConversation(sessionId);
            return ResponseEntity.ok("对话历史已删除");
        } catch (Exception e) {
            log.error("删除对话历史失败", e);
            return ResponseEntity.internalServerError().body("删除失败: " + e.getMessage());
        }
    }

    // ==================== DTO类 ====================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRequestDTO {
        private String message;
        private String sessionId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RAGChatRequestDTO {
        private String message;
        private String sessionId;
        private Integer topK;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatResponseDTO {
        private String sessionId;
        private String message;
        private Boolean usedRAG;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationRequestDTO {
        private String requirement;
        private Double budget;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationResponseDTO {
        private String requirement;
        private Double budget;
        private String recommendation;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KnowledgeAddRequestDTO {
        private String content;
        private Long componentId;
        private String componentType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KnowledgeSearchRequestDTO {
        private String query;
        private Integer topK;
    }
}

