package com.buildmaster.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ollama配置
 * 用于本地LLM模型调用
 */
@Slf4j
@Configuration
public class OllamaConfig {

    @Value("${spring.ai.ollama.base-url:http://localhost:11434}")
    private String baseUrl;

    @Value("${spring.ai.ollama.chat.model:llama3.1}")
    private String model;

    @Value("${spring.ai.ollama.chat.options.temperature:0.7}")
    private Double temperature;

    @Value("${spring.ai.ollama.chat.options.top-p:0.9}")
    private Double topP;

    @Value("${spring.ai.ollama.chat.options.num-predict:2048}")
    private Integer numPredict;

    /**
     * 创建Ollama API客户端
     */
    @Bean
    public OllamaApi ollamaApi() {
        log.info("初始化Ollama API客户端: {}", baseUrl);
        return new OllamaApi(baseUrl);
    }

    /**
     * 创建Ollama Chat客户端
     */
    @Bean
    public OllamaChatClient ollamaChatClient(OllamaApi ollamaApi) {
        log.info("初始化Ollama Chat客户端，模型: {}", model);
        
        OllamaOptions options = OllamaOptions.create()
                .withModel(model)
                .withTemperature(temperature.floatValue())
                .withTopP(topP.floatValue())
                .withNumPredict(numPredict);

        return new OllamaChatClient(ollamaApi, options);
    }

    public String getModel() {
        return model;
    }
}

