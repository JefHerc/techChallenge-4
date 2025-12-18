package com.fiap.techchallenge.notification.repository;

import com.fiap.techchallenge.notification.model.Feedback;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;

import java.util.Map;

@ApplicationScoped
public class FeedbackRepository {

    private static final Logger LOG = Logger.getLogger(FeedbackRepository.class);

    @Inject
    DynamoDbClient dynamoDbClient;

    @ConfigProperty(name = "feedback.dynamodb.table-name")
    String tableName;

    public Feedback findById(String feedbackId) {
        try {
            GetItemRequest request = GetItemRequest.builder()
                    .tableName(tableName)
                    .key(Map.of("FeedbackID", AttributeValue.builder().s(feedbackId).build()))
                    .build();

            GetItemResponse response = dynamoDbClient.getItem(request);
            Map<String, AttributeValue> item = response.item();
            if (item == null || item.isEmpty()) {
                return null;
            }

            Feedback f = new Feedback();
            f.setFeedbackId(feedbackId);
            if (item.containsKey("nota") && item.get("nota").n() != null) {
                try {
                    f.setNota(Integer.parseInt(item.get("nota").n()));
                } catch (NumberFormatException e) {
                    LOG.warnf("Valor de nota inválido no item do DynamoDB para feedback %s: %s", feedbackId, item.get("nota").n());
                }
            }
            if (item.containsKey("descricao") && item.get("descricao").s() != null) {
                f.setDescricao(item.get("descricao").s());
            }
            if (item.containsKey("dataEnvio") && item.get("dataEnvio").s() != null) {
                f.setDataEnvio(item.get("dataEnvio").s());
            }
            return f;
        } catch (SdkException e) {
            // Deixar claro que é uma falha de comunicação com a AWS
            throw new RuntimeException("Falha ao consultar o DynamoDB", e);
        }
    }
}