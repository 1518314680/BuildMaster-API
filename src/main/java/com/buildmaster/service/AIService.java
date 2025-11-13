package com.buildmaster.service;

import com.buildmaster.config.OllamaConfig;
import com.buildmaster.model.ConversationHistory;
import com.buildmaster.repository.ConversationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AI服务
 * 实现RAG（检索增强生成）、对话管理、持续学习等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final OllamaChatClient ollamaChatClient;
    private final VectorService vectorService;
    private final ConversationHistoryRepository conversationRepository;
    private final OllamaConfig ollamaConfig;

    /**
     * 系统提示词 - 定义AI助手的角色和行为
     */
    private static final String SYSTEM_PROMPT = """
            你是BuildMaster的AI装机助手，专门帮助用户选择和配置电脑硬件。
            你的职责包括：
            1. 根据用户预算和需求推荐合适的配件组合
            2. 解答用户关于硬件兼容性、性能的问题
            3. 提供专业的装机建议和优化方案
            4. 帮助用户理解不同配件之间的性能差异
            
            请注意：
            - 始终以用户需求为中心
            - 提供客观、准确的信息
            - 考虑性价比和兼容性
            - 使用简洁易懂的语言
            """;

    /**
     * 聊天接口（不使用RAG）
     */
    public ChatResponse chat(String userMessage, String sessionId) {
        try {
            // 获取或创建会话历史
            ConversationHistory history = getOrCreateConversation(sessionId);

            // 构建消息列表
            List<Message> messages = buildMessages(history, userMessage, null);

            // 调用LLM
            Prompt prompt = new Prompt(messages);
            ChatResponse response = ollamaChatClient.call(prompt);

            // 保存对话历史
            saveConversationTurn(history, userMessage, response.getResult().getOutput().getContent(), false, null);

            log.info("Chat完成，会话ID: {}", sessionId);
            return response;
        } catch (Exception e) {
            log.error("Chat失败", e);
            throw new RuntimeException("Chat失败: " + e.getMessage(), e);
        }
    }

    /**
     * RAG聊天接口（使用检索增强生成）
     */
    public ChatResponse chatWithRAG(String userMessage, String sessionId, int topK) {
        try {
            // 获取或创建会话历史
            ConversationHistory history = getOrCreateConversation(sessionId);

            // 1. 检索相关知识
            List<VectorService.SearchResult> searchResults = vectorService.searchSimilarVectors(userMessage, topK);
            
            // 2. 构建上下文
            String retrievedContext = buildRAGContext(searchResults);
            
            // 3. 构建消息列表（包含检索到的上下文）
            List<Message> messages = buildMessages(history, userMessage, retrievedContext);

            // 4. 调用LLM
            Prompt prompt = new Prompt(messages);
            ChatResponse response = ollamaChatClient.call(prompt);

            // 5. 保存对话历史
            List<String> retrievedDocs = searchResults.stream()
                    .map(VectorService.SearchResult::getContent)
                    .collect(Collectors.toList());
            saveConversationTurn(history, userMessage, response.getResult().getOutput().getContent(), true, retrievedDocs);

            log.info("RAG Chat完成，会话ID: {}, 检索到{}条相关知识", sessionId, searchResults.size());
            return response;
        } catch (Exception e) {
            log.error("RAG Chat失败", e);
            throw new RuntimeException("RAG Chat失败: " + e.getMessage(), e);
        }
    }

    /**
     * 智能推荐配件组合
     */
    public String recommendBuildConfig(String userRequirement, Double budget) {
        try {
            // 构建推荐提示词
            String prompt = String.format("""
                    用户需求：%s
                    预算：%.2f 元
                    
                    请根据以上信息，推荐一套完整的电脑配置方案。
                    要求：
                    1. 列出CPU、主板、内存、显卡、存储、电源、机箱等主要配件
                    2. 说明推荐理由和性能特点
                    3. 确保配件之间的兼容性
                    4. 控制在预算范围内
                    5. 提供性价比分析
                    """, userRequirement, budget);

            // 使用RAG检索相关配件信息
            List<VectorService.SearchResult> searchResults = vectorService.searchSimilarVectors(userRequirement, 10);
            String context = buildRAGContext(searchResults);

            // 构建完整提示
            String fullPrompt = context + "\n\n" + prompt;
            
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(SYSTEM_PROMPT));
            messages.add(new UserMessage(fullPrompt));

            Prompt chatPrompt = new Prompt(messages);
            ChatResponse response = ollamaChatClient.call(chatPrompt);

            String recommendation = response.getResult().getOutput().getContent();
            
            // 创建推荐会话历史
            String sessionId = "recommend_" + UUID.randomUUID().toString();
            ConversationHistory history = ConversationHistory.builder()
                    .sessionId(sessionId)
                    .topic("配件推荐")
                    .messages(new ArrayList<>())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .metadata(ConversationHistory.Metadata.builder()
                            .model(ollamaConfig.getModel())
                            .turnCount(1)
                            .conversationType("recommendation")
                            .build())
                    .build();

            saveConversationTurn(history, prompt, recommendation, true, 
                    searchResults.stream().map(VectorService.SearchResult::getContent).collect(Collectors.toList()));

            log.info("推荐完成，会话ID: {}", sessionId);
            return recommendation;
        } catch (Exception e) {
            log.error("推荐失败", e);
            throw new RuntimeException("推荐失败: " + e.getMessage(), e);
        }
    }

    /**
     * 学习新知识（将新的配件信息加入知识库）
     */
    public void learnNewKnowledge(String content, Long componentId, String componentType) {
        try {
            vectorService.insertVector(content, componentId, componentType);
            log.info("新知识已学习：配件ID={}, 类型={}", componentId, componentType);
        } catch (Exception e) {
            log.error("学习新知识失败", e);
            throw new RuntimeException("学习新知识失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取或创建对话历史
     */
    private ConversationHistory getOrCreateConversation(String sessionId) {
        return conversationRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    ConversationHistory newHistory = ConversationHistory.builder()
                            .sessionId(sessionId)
                            .messages(new ArrayList<>())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .metadata(ConversationHistory.Metadata.builder()
                                    .model(ollamaConfig.getModel())
                                    .turnCount(0)
                                    .conversationType("general")
                                    .build())
                            .build();
                    return conversationRepository.save(newHistory);
                });
    }

    /**
     * 构建消息列表
     */
    private List<Message> buildMessages(ConversationHistory history, String userMessage, String ragContext) {
        List<Message> messages = new ArrayList<>();
        
        // 添加系统消息
        String systemPrompt = SYSTEM_PROMPT;
        if (ragContext != null && !ragContext.isEmpty()) {
            systemPrompt += "\n\n相关知识库信息：\n" + ragContext;
        }
        messages.add(new SystemMessage(systemPrompt));

        // 添加历史消息（最近5轮对话）
        List<ConversationHistory.Message> recentMessages = history.getMessages();
        int startIndex = Math.max(0, recentMessages.size() - 10); // 最多包含最近5轮对话（10条消息）
        for (int i = startIndex; i < recentMessages.size(); i++) {
            ConversationHistory.Message msg = recentMessages.get(i);
            if ("user".equals(msg.getRole())) {
                messages.add(new UserMessage(msg.getContent()));
            } else if ("assistant".equals(msg.getRole())) {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }

        // 添加当前用户消息
        messages.add(new UserMessage(userMessage));

        return messages;
    }

    /**
     * 构建RAG上下文
     */
    private String buildRAGContext(List<VectorService.SearchResult> searchResults) {
        if (searchResults.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder("【相关知识库信息】\n");
        for (int i = 0; i < searchResults.size(); i++) {
            VectorService.SearchResult result = searchResults.get(i);
            context.append(String.format("\n%d. %s (相关度: %.2f)\n", 
                    i + 1, result.getContent(), result.getScore()));
        }
        return context.toString();
    }

    /**
     * 保存对话轮次
     */
    private void saveConversationTurn(ConversationHistory history, String userMessage, 
                                      String assistantMessage, boolean usedRAG, List<String> retrievedDocs) {
        LocalDateTime now = LocalDateTime.now();

        // 添加用户消息
        ConversationHistory.Message userMsg = ConversationHistory.Message.builder()
                .role("user")
                .content(userMessage)
                .timestamp(now)
                .usedRAG(false)
                .build();
        history.getMessages().add(userMsg);

        // 添加助手消息
        ConversationHistory.Message assistantMsg = ConversationHistory.Message.builder()
                .role("assistant")
                .content(assistantMessage)
                .timestamp(now)
                .usedRAG(usedRAG)
                .retrievedDocuments(retrievedDocs)
                .build();
        history.getMessages().add(assistantMsg);

        // 更新元数据
        history.setUpdatedAt(now);
        if (history.getMetadata() != null) {
            history.getMetadata().setTurnCount(history.getMetadata().getTurnCount() + 1);
        }

        // 保存到MongoDB
        conversationRepository.save(history);
    }

    /**
     * 获取用户的对话历史
     */
    public List<ConversationHistory> getUserConversations(Long userId) {
        return conversationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 删除对话历史
     */
    public void deleteConversation(String sessionId) {
        conversationRepository.deleteBySessionId(sessionId);
        log.info("对话历史已删除：{}", sessionId);
    }
}

