package com.co.reddit.service;

import com.co.reddit.exception.SpringRedditException;
import com.co.reddit.model.NotificationEmail;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
 class MailService {
    private final JavaMailSender mailSender;
    private final MailContentBuilder mailContentBuilder;

    @Async
    void sendMail(NotificationEmail notificationEmail){
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("erickjhorman@gmail.com");
            messageHelper.setTo(notificationEmail.getRecipient());
            messageHelper.setSubject(notificationEmail.getSubject());
            messageHelper.setText(mailContentBuilder.build(notificationEmail.getBody()));

        };

        log.info(String.valueOf(messagePreparator));
        try {
            mailSender.send(messagePreparator);
            log.info("Activation email sent");

        }catch (MailException e){
            log.info(String.valueOf(e));
            throw new SpringRedditException("Exception occurred when sending email to " + e + notificationEmail.getRecipient());
        }
    }
}
