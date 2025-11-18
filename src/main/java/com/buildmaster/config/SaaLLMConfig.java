package com.buildmaster.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring ai alibaba LLM 配置
 * @author valley
 * 2025年11月17日23:09:36
 */
@Configuration
public class SaaLLMConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

//    @Bean
//    public DashScopeApi dashScopeApi() {
//        return DashScopeApi.builder().apiKey(apiKey).build();
//    }


//    @Bean
//    public ChatClient chatClient(ChatModel chatModel) {
//        return ChatClient.builder(chatModel).build();
//    }

    private final String DEEPSEEK_MODEL = "deepseek-v3";

    private final String QWEN_MODEL = "qwen-max";

    @Bean(name = "deepseekModel")
    public ChatModel deepSeek() {
        return DashScopeChatModel.builder()
                .dashScopeApi(DashScopeApi.builder().apiKey(apiKey).build())
                .defaultOptions(DashScopeChatOptions.builder().withModel(DEEPSEEK_MODEL).build())
                .build();
    }

    @Bean(name = "qwenModel")
    public ChatModel qwen() {
        return DashScopeChatModel.builder()
                .dashScopeApi(DashScopeApi.builder().apiKey(apiKey).build())
                .defaultOptions(DashScopeChatOptions.builder().withModel(QWEN_MODEL).build())
                .build();
    }
}
