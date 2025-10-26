package com.energyfactory.energy_factory.service;

import com.energyfactory.energy_factory.exception.BusinessException;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 이메일 발송 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    /**
     * 인증 코드 이메일 발송
     * @param toEmail 수신자 이메일
     * @param code 인증 코드
     */
    public void sendVerificationCode(String toEmail, String code) {
        String subject = "[Energy Factory] 비밀번호 재설정 인증 코드";
        String content = buildVerificationEmailContent(code);

        sendEmail(toEmail, subject, content);
    }

    /**
     * 이메일 발송 (HTML)
     */
    private void sendEmail(String toEmail, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true); // HTML 사용

            mailSender.send(message);
            log.info("이메일 발송 성공: {}", toEmail);

        } catch (MessagingException e) {
            log.error("이메일 발송 실패: {}", toEmail, e);
            throw new BusinessException(ResultCode.EMAIL_SEND_FAILED);
        } catch (Exception e) {
            log.error("이메일 발송 중 예외 발생: {}", toEmail, e);
            throw new BusinessException(ResultCode.EMAIL_SEND_FAILED);
        }
    }

    /**
     * 인증 코드 이메일 HTML 컨텐츠 생성
     */
    private String buildVerificationEmailContent(String code) {
        return String.format("""
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
                    }
                    .header {
                        background-color: #4CAF50;
                        color: white;
                        padding: 20px;
                        text-align: center;
                        border-radius: 5px 5px 0 0;
                    }
                    .content {
                        background-color: #f9f9f9;
                        padding: 30px;
                        border-radius: 0 0 5px 5px;
                    }
                    .code {
                        background-color: #fff;
                        border: 2px dashed #4CAF50;
                        padding: 20px;
                        text-align: center;
                        font-size: 32px;
                        font-weight: bold;
                        letter-spacing: 5px;
                        margin: 20px 0;
                        color: #4CAF50;
                    }
                    .footer {
                        margin-top: 20px;
                        padding-top: 20px;
                        border-top: 1px solid #ddd;
                        font-size: 12px;
                        color: #666;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>비밀번호 재설정</h1>
                    </div>
                    <div class="content">
                        <p>안녕하세요,</p>
                        <p>Energy Factory 비밀번호 재설정을 위한 인증 코드입니다.</p>
                        <p>아래의 6자리 코드를 입력해주세요:</p>

                        <div class="code">%s</div>

                        <p><strong>이 코드는 5분간 유효합니다.</strong></p>
                        <p>본인이 요청하지 않았다면 이 이메일을 무시하셔도 됩니다.</p>

                        <div class="footer">
                            <p>이 메일은 발신 전용입니다. 문의사항은 고객센터를 이용해주세요.</p>
                            <p>&copy; 2025 Energy Factory. All rights reserved.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, code);
    }
}
