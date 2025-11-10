package com.hermes.service;

import com.hermes.repository.MailLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class MailServiceTest {

    @Autowired
    private MailService mailService;

    @Autowired
    private MailLogRepository mailLogRepository;

//    @Test
//    void sendMail_shouldCreateMailLog() {
//        // Given
//        MailRequest request = new MailRequest(
//            "test@example.com",
//            "테스트 제목",
//            "테스트 내용",
//            false,
//            "default"
//        );
//
//        // When
//        MailResponse response = mailService.sendMail(request);
//
//        // Then
//        assertThat(response).isNotNull();
//        assertThat(response.recipient()).isEqualTo("test@example.com");
//        assertThat(response.subject()).isEqualTo("테스트 제목");
//        assertThat(response.status()).isEqualTo(MailLog.MailStatus.PENDING);
//
//        // Verify database
//        MailLog savedLog = mailLogRepository.findById(response.id()).orElse(null);
//        assertThat(savedLog).isNotNull();
//        assertThat(savedLog.getRecipient()).isEqualTo("test@example.com");
//    }
}