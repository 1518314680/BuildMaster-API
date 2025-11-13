package com.buildmaster.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String VERIFICATION_CODE_PREFIX = "email:verification:";
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRY_MINUTES = 5;

    /**
     * 发送验证码到指定邮箱
     */
    public void sendVerificationCode(String toEmail) throws MessagingException {
        // 生成6位数字验证码
        String code = generateVerificationCode();
        
        // 将验证码存储到Redis，设置5分钟过期
        String key = VERIFICATION_CODE_PREFIX + toEmail;
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRY_MINUTES, TimeUnit.MINUTES);
        
        // 发送邮件
        sendHtmlEmail(toEmail, "BuildMaster - 注册验证码", buildVerificationEmailContent(code));
        
        log.info("Verification code sent to {}", toEmail);
    }

    /**
     * 验证验证码是否正确
     */
    public boolean verifyCode(String email, String code) {
        String key = VERIFICATION_CODE_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(key);
        
        if (storedCode != null && storedCode.equals(code)) {
            // 验证成功后删除验证码
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    /**
     * 生成6位数字验证码
     */
    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 构建验证码邮件内容（HTML格式）
     */
    private String buildVerificationEmailContent(String code) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f9f9f9;
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white;
                        padding: 30px;
                        text-align: center;
                        border-radius: 10px 10px 0 0;
                    }
                    .content {
                        background: white;
                        padding: 30px;
                        border-radius: 0 0 10px 10px;
                    }
                    .code-box {
                        background: #f0f0f0;
                        border-left: 4px solid #667eea;
                        padding: 15px;
                        margin: 20px 0;
                        font-size: 24px;
                        font-weight: bold;
                        text-align: center;
                        letter-spacing: 5px;
                    }
                    .footer {
                        text-align: center;
                        margin-top: 20px;
                        color: #999;
                        font-size: 12px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>BuildMaster</h1>
                        <p>装机配置助手</p>
                    </div>
                    <div class="content">
                        <h2>欢迎注册 BuildMaster！</h2>
                        <p>您好，</p>
                        <p>感谢您选择 BuildMaster 装机配置助手。请使用以下验证码完成注册：</p>
                        <div class="code-box">
                            %s
                        </div>
                        <p><strong>验证码有效期为 5 分钟</strong>，请尽快完成注册。</p>
                        <p>如果这不是您的操作，请忽略此邮件。</p>
                        <hr>
                        <p>祝您使用愉快！</p>
                        <p>BuildMaster 团队</p>
                    </div>
                    <div class="footer">
                        <p>这是一封自动发送的邮件，请勿直接回复。</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(code);
    }

    /**
     * 发送HTML格式邮件
     */
    private void sendHtmlEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true); // true表示HTML格式
        
        mailSender.send(message);
    }
}

