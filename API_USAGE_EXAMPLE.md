# 用户注册API使用示例

本文档介绍如何使用BuildMaster的用户注册功能。

## API接口概览

### 1. 发送验证码
**接口**: `POST /api/user/send-code`

**请求示例**:
```json
{
  "email": "user@example.com"
}
```

**响应示例**:
```json
{
  "success": true,
  "message": "验证码已发送，请查收邮箱",
  "data": null
}
```

### 2. 用户注册
**接口**: `POST /api/user/register`

**请求示例**:
```json
{
  "username": "testuser",
  "email": "user@example.com",
  "password": "password123",
  "verificationCode": "123456",
  "displayName": "测试用户"
}
```

**响应示例**:
```json
{
  "success": true,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "user@example.com",
    "displayName": "测试用户",
    "avatarUrl": null,
    "createdAt": "2025-10-21T10:30:00"
  }
}
```

## 完整注册流程示例

### 使用 cURL

```bash
# 步骤1: 发送验证码
curl -X POST http://localhost:8080/api/user/send-code \
  -H "Content-Type: application/json" \
  -d '{
    "email": "myemail@example.com"
  }'

# 步骤2: 注册用户（使用收到的验证码）
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "myusername",
    "email": "myemail@example.com",
    "password": "MySecurePassword123",
    "verificationCode": "123456",
    "displayName": "My Display Name"
  }'
```

### 使用 JavaScript (Fetch API)

```javascript
// 步骤1: 发送验证码
async function sendVerificationCode(email) {
  const response = await fetch('http://localhost:8080/api/user/send-code', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email }),
  });
  
  const result = await response.json();
  console.log(result);
  return result;
}

// 步骤2: 注册用户
async function registerUser(userData) {
  const response = await fetch('http://localhost:8080/api/user/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(userData),
  });
  
  const result = await response.json();
  console.log(result);
  return result;
}

// 使用示例
async function completeRegistration() {
  // 发送验证码
  await sendVerificationCode('user@example.com');
  
  // 等待用户输入验证码后注册
  const userData = {
    username: 'testuser',
    email: 'user@example.com',
    password: 'password123',
    verificationCode: '123456', // 用户从邮箱获取的验证码
    displayName: '测试用户'
  };
  
  const result = await registerUser(userData);
  
  if (result.success) {
    console.log('注册成功:', result.data);
  } else {
    console.error('注册失败:', result.message);
  }
}
```

### 使用 Axios

```javascript
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

// 步骤1: 发送验证码
export const sendVerificationCode = async (email) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/user/send-code`, {
      email
    });
    return response.data;
  } catch (error) {
    console.error('发送验证码失败:', error);
    throw error;
  }
};

// 步骤2: 注册用户
export const registerUser = async (userData) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/user/register`, userData);
    return response.data;
  } catch (error) {
    console.error('注册失败:', error);
    throw error;
  }
};

// 完整注册流程
export const completeRegistration = async (username, email, password, verificationCode, displayName) => {
  try {
    // 注册
    const result = await registerUser({
      username,
      email,
      password,
      verificationCode,
      displayName
    });
    
    if (result.success) {
      console.log('注册成功:', result.data);
      return result.data;
    } else {
      throw new Error(result.message);
    }
  } catch (error) {
    console.error('注册流程失败:', error);
    throw error;
  }
};
```

## 字段说明

### SendCodeRequest

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| email | String | 是 | 接收验证码的邮箱地址 |

### RegisterRequest

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名，3-20个字符 |
| email | String | 是 | 邮箱地址 |
| password | String | 是 | 密码，6-50个字符 |
| verificationCode | String | 是 | 6位数字验证码 |
| displayName | String | 否 | 显示名称，如不提供则使用username |

## 错误处理

### 常见错误响应

**验证码错误或已过期**:
```json
{
  "success": false,
  "message": "验证码错误或已过期",
  "data": null
}
```

**用户名已存在**:
```json
{
  "success": false,
  "message": "用户名已存在",
  "data": null
}
```

**邮箱已被注册**:
```json
{
  "success": false,
  "message": "邮箱已被注册",
  "data": null
}
```

**验证失败（字段格式错误）**:
```json
{
  "success": false,
  "message": "邮箱格式不正确",
  "data": null
}
```

## 注意事项

1. **验证码有效期**: 验证码有效期为5分钟，过期需要重新获取
2. **验证码格式**: 6位数字
3. **邮箱唯一性**: 每个邮箱只能注册一次
4. **用户名唯一性**: 用户名不能重复
5. **密码安全**: 密码会使用SHA-256加密存储
6. **验证码限制**: 建议实现发送频率限制（如1分钟内只能发送一次）

## Swagger文档

启动后端服务后，可以访问以下地址查看完整的API文档：

- **Swagger UI**: http://localhost:8080/swagger-ui
- **OpenAPI JSON**: http://localhost:8080/api-docs

在Swagger UI中可以直接测试所有API接口。

