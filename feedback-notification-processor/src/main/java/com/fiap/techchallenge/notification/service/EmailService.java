package com.fiap.techchallenge.notification.service;

import com.fiap.techchallenge.notification.model.Feedback;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;

@ApplicationScoped
public class EmailService {

    private static final Logger LOG = Logger.getLogger(EmailService.class);

    @Inject
    SesClient sesClient;

    @ConfigProperty(name = "email.admin.address")
    String adminEmail;

    @ConfigProperty(name = "email.source.address")
    String sourceEmail;

    public void enviarEmailDeAlerta(Feedback feedback) {
        String subjectText = "Alerta de Feedback Crítico Recebido!";
        String bodyText = "Olá, Administrador(a),\n\n" +
                "Um novo feedback crítico foi recebido e precisa de sua atenção.\n\n" +
                "- ID do Feedback: " + feedback.getFeedbackId() + "\n" +
                "- Nota: " + feedback.getNota() + "\n" +
                "- Descrição: \"" + safe(feedback.getDescricao()) + "\"\n" +
                "- Data de Envio: " + safe(feedback.getDataEnvio()) + "\n\n" +
                "Por favor, analise o mais rápido possível.\n\n" +
                "Atenciosamente,\n" +
                "Sistema de Feedback Automático";

        try {
            SendEmailRequest request = SendEmailRequest.builder()
                    .destination(Destination.builder().toAddresses(adminEmail).build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subjectText).charset("UTF-8").build())
                            .body(b -> b.text(Content.builder().data(bodyText).charset("UTF-8").build()))
                            .build())
                    .source(sourceEmail)
                    .build();

            SendEmailResponse response = sesClient.sendEmail(request);
            LOG.infof("Email enviado. MessageId=%s", response.messageId());
        } catch (SdkException e) {
            throw new RuntimeException("Falha ao enviar e-mail via SES", e);
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}