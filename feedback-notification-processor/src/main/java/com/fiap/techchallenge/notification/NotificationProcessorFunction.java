package com.fiap.techchallenge.notification;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fiap.techchallenge.notification.dto.SqsFeedbackMessage;
import com.fiap.techchallenge.notification.model.Feedback;
import com.fiap.techchallenge.notification.repository.FeedbackRepository;
import com.fiap.techchallenge.notification.service.EmailService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@Named("notificationProcessor")
public class NotificationProcessorFunction implements RequestHandler<SQSEvent, Void> {

    private static final Logger LOG = Logger.getLogger(NotificationProcessorFunction.class);
    private static final int NOTA_CRITICA_LIMITE = 2;

    @Inject
    FeedbackRepository feedbackRepository;

    @Inject
    EmailService emailService;

    @ConfigProperty(name = "email.admin.address")
    String adminEmail;

    private final Jsonb jsonb = JsonbBuilder.create();

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        int count = event.getRecords() == null ? 0 : event.getRecords().size();
        LOG.infof("Iniciando processamento de Lote com %d mensagens.", count);

        if (event.getRecords() == null) {
            return null;
        }

        for (SQSEvent.SQSMessage msg : event.getRecords()) {
            String body = msg.getBody();
            String feedbackId = null;
            try {
                SqsFeedbackMessage parsed = jsonb.fromJson(body, SqsFeedbackMessage.class);
                feedbackId = parsed != null ? parsed.getFeedbackId() : null;
                if (feedbackId == null || feedbackId.isBlank()) {
                    LOG.error("Erro ao parsear mensagem SQS: campo feedbackId ausente ou vazio.");
                    // Mensagem considerada processada (evitar exceção para ir à DLQ após tentativas)
                    continue;
                }
            } catch (Exception e) {
                // Erro de parsing não deve lançar exceção para permitir que vá para DLQ.
                LOG.error("Erro de Parsing do corpo da mensagem SQS: " + e.getMessage());
                continue;
            }

            LOG.infof("Processando feedback ID: %s.", feedbackId);

            Feedback feedback;
            try {
                feedback = feedbackRepository.findById(feedbackId);
            } catch (RuntimeException awsCommsError) {
                // Falha ao contatar AWS (DynamoDB). Lançar para reprocessar.
                throw awsCommsError;
            }

            if (feedback == null) {
                LOG.errorf("Feedback com ID %s não encontrado na tabela DynamoDB.", feedbackId);
                // Considera processado; não lança exceção
                continue;
            }

            Integer nota = feedback.getNota();
            if (nota == null || nota > NOTA_CRITICA_LIMITE) {
                LOG.infof("Feedback %s com nota %s não é crítico. Ignorando.", feedbackId, String.valueOf(nota));
                continue;
            }

            LOG.infof("Feedback crítico detectado %s. Enviando notificação para %s.", feedbackId, adminEmail);
            try {
                emailService.enviarEmailDeAlerta(feedback);
            } catch (RuntimeException e) {
                LOG.errorf("Falha ao enviar e-mail para feedback %s: %s.", feedbackId, e.getMessage());
                // Lançar para reprocessamento
                throw e;
            }
        }
        return null;
    }
}