package com.fiap.techchallenge.infrastructure.dynamo;

import com.fiap.techchallenge.domain.model.Avaliacao;
import com.fiap.techchallenge.domain.model.PdfData;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DynamoPdfDataService implements com.fiap.techchallenge.domain.service.PdfDataService {

    private final DynamoDbClient dynamoDbClient;

    @ConfigProperty(name = "FEEDBACK_DYNAMODB_TABLE_NAME")
    String table;

    public DynamoPdfDataService(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public PdfData avaliacoesUltimaSemana() {
        QueryResponse response = dynamoDbClient.query(
                QueryRequest.builder()
                        .tableName(table)
                        .build()
        );

        List<Avaliacao> itensDatabase = response.items().stream()
                .map(this::toDomain)
                .toList();


        List<Avaliacao> itensSemana = itensDatabase.stream().filter(item -> {
            if (item.getDataEnvio() != null) {
                LocalDate date = LocalDate.parse(item.getDataEnvio());

                return date.isAfter(LocalDate.now().minus(Period.ofWeeks(1)));
            }

            return false;
        }).toList();

        Double mediaNotas = itensSemana.stream()
                .mapToInt(Avaliacao::getNota)
                .average()
                .orElse(0.0);

        Long qtdAvaliacoes = Long.valueOf(itensSemana.size());

        Long qtdAvaliacoesCriticas = itensSemana.stream()
                .filter(item -> item.getNota() < 2)
                .count();

        return new PdfData("Avaliacoes.pdf", qtdAvaliacoes, mediaNotas, qtdAvaliacoesCriticas);
    }

    private Avaliacao toDomain(Map<String, AttributeValue> item) {
        return new Avaliacao(
                item.get("FeedbackID").s(),
                item.get("descricao").s(),
                Integer.parseInt(item.get("nota").n()),
                item.get("status").s(),
                item.get("dataEnvio").s(),
                item.get("userId").s()
        );
    }

}
