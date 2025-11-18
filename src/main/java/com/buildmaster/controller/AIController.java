package com.buildmaster.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

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

    //    @Resource(name = "ollamaChatModel") //可以通过name选择使用哪个chat model
    //    @Resource + @Qualifier("ollamaChatModel") = @Resource(name = "ollamaChatModel")
    @Resource //对话模型
    private ChatModel chatModel;

//    private final ChatClient dashScopeChatClient;

    /**
     * ChatClient 依赖 ChatModel
     */
//    public AIController(ChatModel dashScopeChatModel) {
//        this.dashScopeChatClient = ChatClient.builder(dashScopeChatModel).build();
//    }

    @GetMapping("/hello/doChat")
    public String doChat(@RequestParam(name = "msg", defaultValue = "你是谁") String msg) {
        String result = chatModel.call(msg);
        return result;
    }

    @GetMapping(value = "hello/doSteam")
    public Flux<String> doSteam(@RequestParam(name = "msg", defaultValue = "你是谁") String msg) {
        return chatModel.stream(msg);
    }
}

