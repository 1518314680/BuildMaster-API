# QQ邮箱SMTP授权码配置指南

本指南将帮助您配置QQ邮箱的SMTP服务，用于发送注册验证码。

## 步骤1：开启QQ邮箱SMTP服务

1. 登录QQ邮箱 (https://mail.qq.com/)
2. 点击顶部的 **"设置"** → **"账户"**
3. 找到 **"POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务"** 部分
4. 开启 **"POP3/SMTP服务"** 或 **"IMAP/SMTP服务"**

## 步骤2：生成授权码

1. 在开启服务时，QQ邮箱会要求您生成授权码
2. 按照提示发送短信验证
3. 验证成功后，QQ邮箱会显示一个 **16位的授权码**
4. **请妥善保存这个授权码**，它将用于配置应用

> ⚠️ **重要提示**：授权码只显示一次，如果忘记需要重新生成

## 步骤3：配置应用

### 方法1：环境变量配置（推荐）

在项目根目录创建 `.env` 文件或在系统环境变量中设置：

```bash
MAIL_PASSWORD=你的16位授权码
```

在Windows PowerShell中设置环境变量：
```powershell
$env:MAIL_PASSWORD="你的16位授权码"
```

在Linux/Mac中设置环境变量：
```bash
export MAIL_PASSWORD=你的16位授权码
```

### 方法2：直接修改配置文件（不推荐用于生产环境）

编辑 `BuildMaster-API/src/main/resources/application.yml`：

```yaml
spring:
  mail:
    password: yizyjiyrhfmpffei
```

> ⚠️ **安全提示**：不要将授权码提交到Git仓库！

## 步骤4：测试邮件发送

启动后端服务后，使用以下API测试邮件发送功能：

```bash
# 发送验证码
curl -X POST http://localhost:8080/api/user/send-code \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com"}'
```

## 常见问题

### Q1: 提示 "535 Login Fail" 错误
**A:** 请检查：
- 是否正确开启了SMTP服务
- 授权码是否正确（16位，不包含空格）
- 发送邮箱地址是否为 `1518314680@qq.com`

### Q2: 邮件发送超时
**A:** 请检查：
- 网络连接是否正常
- 防火墙是否允许587端口
- 尝试将端口从587改为465（SSL端口）

### Q3: 如何更换发送邮箱？
**A:** 修改 `application.yml` 中的：
```yaml
spring:
  mail:
    username: 新的邮箱地址@qq.com
    password: 新的授权码
```

## 配置说明

当前配置使用的是QQ邮箱SMTP服务器：

- **服务器地址**: smtp.qq.com
- **端口**: 587 (STARTTLS)
- **发送邮箱**: 1518314680@qq.com
- **验证方式**: SMTP认证

## 安全建议

1. ✅ 使用环境变量存储授权码
2. ✅ 将 `.env` 文件添加到 `.gitignore`
3. ✅ 定期更换授权码
4. ❌ 不要将授权码硬编码在代码中
5. ❌ 不要将授权码提交到版本控制系统

