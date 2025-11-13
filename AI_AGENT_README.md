# BuildMaster AI 智能体使用说明

## 概述

BuildMaster AI智能体是一个基于Spring AI + Ollama + RAG + Milvus的智能装机助手系统，具有以下特性：

- 🤖 **本地LLM支持**：使用Ollama运行本地大语言模型（如llama3.1）
- 🔍 **RAG检索增强**：通过Milvus向量数据库实现知识库检索
- 💾 **上下文记忆**：使用MongoDB存储对话历史，支持连贯对话
- 📚 **持续学习**：支持动态添加和更新知识库
- 🎯 **智能推荐**：基于用户需求和预算智能推荐配件组合

## 技术架构

```
用户请求
    ↓
AIController (REST API)
    ↓
AIService (核心逻辑)
    ↓
    ├─→ VectorService (向量检索) → Milvus向量数据库
    ├─→ OllamaChatClient (LLM调用) → Ollama服务
    └─→ ConversationRepository (对话管理) → MongoDB
```

## 快速开始

### 1. 启动所有服务

使用Docker Compose一键启动：

```bash
docker-compose up -d
```

这将启动以下服务：
- MySQL (3306) - 主数据库
- Redis (6379) - 缓存
- MongoDB (27017) - 对话历史存储
- Milvus (19530) - 向量数据库
- Ollama (11434) - 本地LLM服务
- API服务 (8080)
- UI服务 (3000)

### 2. 下载Ollama模型

首次使用需要下载LLM模型：

```bash
# 进入Ollama容器
docker exec -it buildmaster-ollama bash

# 下载llama3.1模型（推荐）
ollama pull llama3.1

# 或下载其他模型
ollama pull llama2
ollama pull mistral
```

### 3. 验证服务状态

访问以下地址验证服务：

- Swagger文档：http://localhost:8080/swagger-ui
- Ollama健康检查：http://localhost:11434/api/tags
- 前端界面：http://localhost:3000

## API接口说明

### 1. 普通对话

```bash
POST http://localhost:8080/api/ai/chat
Content-Type: application/json

{
  "message": "你好，我想组装一台电脑",
  "sessionId": "session_123"  // 可选，不提供则自动生成
}
```

响应：
```json
{
  "sessionId": "session_123",
  "message": "您好！我是BuildMaster AI装机助手...",
  "usedRAG": false
}
```

### 2. RAG增强对话

```bash
POST http://localhost:8080/api/ai/chat/rag
Content-Type: application/json

{
  "message": "Intel i7-13700K和AMD 7800X3D哪个更适合游戏？",
  "sessionId": "session_123",
  "topK": 5  // 检索前5条相关知识，可选
}
```

响应：
```json
{
  "sessionId": "session_123",
  "message": "根据知识库信息，对于游戏场景...",
  "usedRAG": true
}
```

### 3. 智能推荐

```bash
POST http://localhost:8080/api/ai/recommend
Content-Type: application/json

{
  "requirement": "主要用于编程开发和偶尔玩游戏，需要多屏显示",
  "budget": 8000.00
}
```

响应：
```json
{
  "requirement": "主要用于编程开发和偶尔玩游戏，需要多屏显示",
  "budget": 8000.00,
  "recommendation": "【配置方案】\n1. CPU: Intel i5-13400F..."
}
```

### 4. 添加知识到知识库

```bash
POST http://localhost:8080/api/ai/knowledge/add
Content-Type: application/json

{
  "content": "Intel i7-13700K是13代酷睿处理器，采用16核24线程设计...",
  "componentId": 1001,
  "componentType": "CPU"
}
```

### 5. 搜索知识库

```bash
POST http://localhost:8080/api/ai/knowledge/search
Content-Type: application/json

{
  "query": "适合游戏的CPU",
  "topK": 10
}
```

### 6. 批量向量化

```bash
POST http://localhost:8080/api/ai/knowledge/vectorize
```

自动将所有未向量化的知识添加到向量数据库。

### 7. 获取对话历史

```bash
GET http://localhost:8080/api/ai/conversations/{userId}
```

### 8. 删除对话

```bash
DELETE http://localhost:8080/api/ai/conversations/{sessionId}
```

## 配置说明

### application.yml 配置

```yaml
spring:
  # MongoDB配置
  data:
    mongodb:
      uri: mongodb://localhost:27017/buildmaster_ai
      database: buildmaster_ai
  
  # Spring AI配置
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        model: llama3.1  # 使用的模型
        options:
          temperature: 0.7  # 温度参数，控制创造性
          top-p: 0.9
          num-predict: 2048  # 最大生成token数
    
    vectorstore:
      milvus:
        client:
          host: localhost
          port: 19530
        database-name: buildmaster
        collection-name: component_knowledge
        embedding-dimension: 768  # 向量维度
        index-type: IVF_FLAT
        metric-type: L2
```

### 环境变量覆盖（Docker部署）

在docker-compose.yml中设置：

```yaml
environment:
  SPRING_DATA_MONGODB_URI: mongodb://root:buildmaster@mongodb:27017/buildmaster_ai?authSource=admin
  SPRING_AI_OLLAMA_BASE_URL: http://ollama:11434
  SPRING_AI_VECTORSTORE_MILVUS_CLIENT_HOST: milvus
  SPRING_AI_VECTORSTORE_MILVUS_CLIENT_PORT: 19530
```

## 工作流程

### RAG工作流程

1. **用户提问** → 用户发送问题到 `/api/ai/chat/rag`
2. **向量检索** → VectorService将问题转换为向量，在Milvus中检索相似知识
3. **上下文构建** → 将检索到的知识与对话历史组合成上下文
4. **LLM生成** → OllamaChatClient基于上下文生成回答
5. **保存历史** → 对话内容存储到MongoDB

### 知识库更新流程

1. **添加知识** → 通过API添加配件相关知识
2. **文本向量化** → VectorService将文本转换为向量
3. **存储向量** → 向量存储到Milvus，元数据存储到MongoDB
4. **建立索引** → Milvus自动建立索引，加速检索

## 性能优化建议

### 1. 向量维度选择

- 768维：平衡精度和性能（推荐）
- 384维：更快的检索速度，略降精度
- 1024维：更高精度，需要更多资源

### 2. 索引配置

```yaml
# IVF_FLAT：快速检索，适中内存
index-type: IVF_FLAT

# HNSW：最快检索，高内存消耗
# index-type: HNSW

# FLAT：精确检索，慢速但最准确
# index-type: FLAT
```

### 3. 对话历史管理

- 默认保留最近5轮（10条消息）对话
- 定期清理过期对话历史
- 考虑实现对话摘要功能

### 4. 缓存策略

使用Redis缓存：
- 频繁查询的向量检索结果
- 热门推荐配置
- 常见问题回答

## 监控和维护

### 查看Milvus集合信息

```python
from pymilvus import connections, utility

connections.connect(host="localhost", port="19530")
print(utility.list_collections())

# 查看集合统计
collection = Collection("component_knowledge")
print(collection.num_entities)
```

### 查看MongoDB对话数据

```bash
docker exec -it buildmaster-mongodb mongosh

use buildmaster_ai
db.conversation_history.find().limit(5)
db.component_knowledge.countDocuments()
```

### 查看Ollama模型

```bash
docker exec buildmaster-ollama ollama list
```

## 常见问题

### Q1: Ollama响应很慢？

**A**: 首次调用会加载模型到内存，后续会快很多。可以考虑：
- 使用更小的模型（如tinyllama）
- 增加服务器内存
- 使用GPU加速

### Q2: Milvus连接失败？

**A**: 检查依赖服务：
```bash
docker ps | grep -E "etcd|minio|milvus"
```

确保etcd和minio正常运行。

### Q3: 向量检索结果不准确？

**A**: 当前使用简单的hash向量化方法，建议：
- 集成专业的embedding模型（如sentence-transformers）
- 使用OpenAI/HuggingFace的embedding API
- 调整检索的topK参数

### Q4: 如何更换LLM模型？

**A**: 修改配置：
```yaml
spring:
  ai:
    ollama:
      chat:
        model: mistral  # 改为其他模型
```

记得先下载对应模型：`ollama pull mistral`

## 扩展功能建议

- [ ] 集成专业embedding模型（sentence-transformers）
- [ ] 实现多轮对话意图识别
- [ ] 添加对话评分和反馈机制
- [ ] 实现知识图谱增强RAG
- [ ] 支持多模态（图片识别）
- [ ] 添加对话流程编排（langchain风格）
- [ ] 实现分布式向量检索
- [ ] 添加A/B测试框架

## 贡献指南

欢迎提交Issue和Pull Request！

## 许可证

MIT License

