# 邮箱验证码注册功能

## 功能概述

本功能实现了完整的邮箱验证码注册流程：

1. 用户填写注册信息（用户名、邮箱、密码等）
2. 系统向用户邮箱发送6位数字验证码
3. 用户输入验证码完成注册
4. 验证码有效期为5分钟，存储在Redis中

## 技术架构

### 后端技术栈

- **Spring Boot 3.3.3** - 应用框架
- **Spring Mail** - 邮件发送服务
- **Redis** - 验证码存储（带过期时间）
- **MySQL** - 用户数据持久化
- **QQ邮箱SMTP** - 邮件服务器

### 前端技术栈

- **Next.js 14** - React框架
- **Tailwind CSS** - 样式
- **Framer Motion** - 动画效果

## 文件结构

### 后端文件

```
BuildMaster-API/
├── src/main/java/com/buildmaster/
│   ├── controller/
│   │   └── UserController.java          # 用户注册API接口
│   ├── service/
│   │   ├── EmailService.java            # 邮件发送服务
│   │   └── UserService.java             # 用户业务逻辑
│   ├── dto/
│   │   ├── SendCodeRequest.java         # 发送验证码请求DTO
│   │   ├── RegisterRequest.java         # 注册请求DTO
│   │   ├── ApiResponse.java             # 统一响应DTO
│   │   └── UserDTO.java                 # 用户信息DTO
│   ├── model/
│   │   └── User.java                    # 用户实体
│   └── repository/
│       └── UserRepository.java          # 用户数据访问
└── src/main/resources/
    └── application.yml                  # 配置文件（包含邮件配置）
```

### 前端文件

```
BuildMaster-UI/
└── src/app/
    └── register/
        └── page.tsx                     # 注册页面
```

### 文档文件

```
BuildMaster-API/
├── EMAIL_SETUP_GUIDE.md                 # QQ邮箱SMTP配置指南
├── API_USAGE_EXAMPLE.md                 # API使用示例
└── EMAIL_REGISTRATION_README.md         # 本文档
```

## 快速开始

### 1. 配置QQ邮箱授权码

参考 `EMAIL_SETUP_GUIDE.md` 获取QQ邮箱的SMTP授权码。

### 2. 设置环境变量

在PowerShell中设置：
```powershell
$env:MAIL_PASSWORD="你的QQ邮箱授权码"
```

或者直接修改 `application.yml`：
```yaml
spring:
  mail:
    password: 你的QQ邮箱授权码
```

### 3. 启动Redis

```bash
# Windows (使用Docker)
docker run -d -p 6379:6379 redis

# 或使用已安装的Redis
redis-server
```

### 4. 启动后端服务

```bash
cd BuildMaster-API
mvn spring-boot:run
```

### 5. 启动前端服务

```bash
cd BuildMaster-UI
npm install
npm run dev
```

### 6. 访问注册页面

打开浏览器访问：http://localhost:3000/register

## API接口说明

### 1. 发送验证码

**请求**:
```http
POST /api/user/send-code
Content-Type: application/json

{
  "email": "user@example.com"
}
```

**响应**:
```json
{
  "success": true,
  "message": "验证码已发送，请查收邮箱",
  "data": null
}
```

### 2. 用户注册

**请求**:
```http
POST /api/user/register
Content-Type: application/json

{
  "username": "testuser",
  "email": "user@example.com",
  "password": "password123",
  "verificationCode": "123456",
  "displayName": "测试用户"
}
```

**响应**:
```json
{
  "success": true,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "user@example.com",
    "displayName": "测试用户",
    "createdAt": "2025-10-21T10:30:00"
  }
}
```

## 核心功能说明

### 验证码生成与存储

- **生成**: 使用 `Random` 生成6位数字验证码
- **存储**: 使用Redis存储，键名格式为 `email:verification:{邮箱}`
- **过期**: 自动设置5分钟过期时间

```java
String key = VERIFICATION_CODE_PREFIX + toEmail;
redisTemplate.opsForValue().set(key, code, CODE_EXPIRY_MINUTES, TimeUnit.MINUTES);
```

### 邮件发送

使用 `JavaMailSender` 发送HTML格式邮件：

```java
MimeMessage message = mailSender.createMimeMessage();
MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
helper.setFrom(fromEmail);
helper.setTo(to);
helper.setSubject(subject);
helper.setText(content, true); // HTML格式
mailSender.send(message);
```

### 密码加密

使用SHA-256算法加密密码：

```java
MessageDigest digest = MessageDigest.getInstance("SHA-256");
byte[] hash = digest.digest(password.getBytes());
return Base64.getEncoder().encodeToString(hash);
```

### 验证码校验

```java
public boolean verifyCode(String email, String code) {
    String key = VERIFICATION_CODE_PREFIX + email;
    String storedCode = redisTemplate.opsForValue().get(key);
    
    if (storedCode != null && storedCode.equals(code)) {
        redisTemplate.delete(key); // 验证成功后删除
        return true;
    }
    return false;
}
```

## 安全考虑

1. ✅ **验证码过期**: 5分钟自动过期
2. ✅ **一次性使用**: 验证成功后立即删除
3. ✅ **密码加密**: SHA-256加密存储
4. ✅ **参数验证**: 使用 `@Valid` 进行参数校验
5. ✅ **唯一性检查**: 用户名和邮箱唯一性验证

### 建议改进

- [ ] 添加发送频率限制（防止恶意发送）
- [ ] 添加验证码错误次数限制
- [ ] 使用更强的密码加密算法（如BCrypt）
- [ ] 添加图形验证码防止机器人
- [ ] 添加邮箱格式黑名单

## 测试

### 使用Swagger测试

访问 http://localhost:8080/swagger-ui 可以直接测试API。

### 使用cURL测试

```bash
# 发送验证码
curl -X POST http://localhost:8080/api/user/send-code \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com"}'

# 注册（使用收到的验证码）
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username":"testuser",
    "email":"test@example.com",
    "password":"password123",
    "verificationCode":"123456",
    "displayName":"Test User"
  }'
```

## 常见问题

### Q: 收不到验证码邮件？

1. 检查QQ邮箱授权码是否正确
2. 检查SMTP服务是否开启
3. 检查邮箱是否在垃圾邮件中
4. 检查Redis是否正常运行

### Q: 验证码总是提示错误或过期？

1. 检查Redis服务是否运行
2. 检查系统时间是否正确
3. 确认在5分钟内使用验证码

### Q: 如何更换发送邮箱？

修改 `application.yml` 中的配置：
```yaml
spring:
  mail:
    username: 新的邮箱@qq.com
    password: 新的授权码
```

## 下一步计划

- [ ] 添加登录功能
- [ ] 添加密码重置功能
- [ ] 添加JWT认证
- [ ] 添加用户个人中心
- [ ] 添加邮箱验证功能（验证邮箱是否真实）

## 贡献

如有问题或建议，欢迎提Issue或PR。

## 许可证

MIT License

