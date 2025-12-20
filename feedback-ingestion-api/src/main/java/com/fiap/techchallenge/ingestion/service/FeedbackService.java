package com.fiap.techchallenge.ingestion.service;

import com.fiap.techchallenge.ingestion.dto.FeedbackRequest;
import com.fiap.techchallenge.ingestion.model.Feedback;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class FeedbackService {

    private static final Logger LOG = Logger.getLogger(FeedbackService.class);

    @Inject
    DynamoDbClient dynamoDbClient;

    @Inject
    SqsClient sqsClient;

    @ConfigProperty(name = "feedback.dynamodb.table-name")
    String tableName;

    @ConfigProperty(name = "feedback.sqs.queue-url")
    String queueUrl;

    public Feedback processarFeedback(FeedbackRequest request, String userId) {
        // Geração de dados
        String id = UUID.randomUUID().toString();
        String dataEnvio = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

        Feedback feedback = new Feedback();
        feedback.setFeedbackId(id);
        feedback.setDescricao(request.getDescricao());
        feedback.setNota(request.getNota());
        feedback.setStatus("PENDENTE");
        feedback.setDataEnvio(dataEnvio);
        feedback.setUserId(userId);

        // Persistir no DynamoDB
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("FeedbackID", AttributeValue.builder().s(id).build());
        if (request.getDescricao() != null) {
            item.put("descricao", AttributeValue.builder().s(request.getDescricao()).build());
        }
        if (request.getNota() != null) {
            item.put("nota", AttributeValue.builder().n(Integer.toString(request.getNota())).build());
        }
        item.put("status", AttributeValue.builder().s("PENDENTE").build());
        item.put("dataEnvio", AttributeValue.builder().s(dataEnvio).build());
        if (userId != null) {
            item.put("userId", AttributeValue.builder().s(userId).build());
        }

        PutItemRequest put = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();
        dynamoDbClient.putItem(put);

        // Enviar evento minimalista para SQS
        String payload = "{\"feedbackId\":\"" + id + "\"}";
        if (queueUrl != null && !queueUrl.isBlank()) {
            SendMessageRequest send = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(payload)
                    .build();
            sqsClient.sendMessage(send);
        } else {
            LOG.warn("FEEDBACK_SUBMITTED_QUEUE_URL não configurada; pulando envio para SQS.");
        }

        return feedback;
    }
}
