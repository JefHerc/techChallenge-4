package com.fiap.techchallenge.infrastructure.storage;

import com.fiap.techchallenge.domain.service.StorageService;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@ApplicationScoped
public class S3StorageService implements StorageService {

    private final S3Client s3;
    private final S3Presigner presigner;

    @ConfigProperty(name = "PDF_BUCKET")
    String bucket;

    @ConfigProperty(name = "PDF_DIAS_URL_VALIDA", defaultValue = "20")
    String daysStr;

    Long days = 20L;

    public S3StorageService(S3Client s3, S3Presigner presigner) {
        this.s3 = s3;
        this.presigner = presigner;

        if(daysStr != null){
            try {
                days = Long.parseLong(daysStr);
            }catch (Exception e){
                System.out.println("Erro ao converter o parametro do email");
            }
        }
    }

    @Override
    public String store(byte[] arquivo, String nomeArquivo) {
        String key = "relatorios/" + UUID.randomUUID() + nomeArquivo;

        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType("application/pdf")
                        .build(),
                RequestBody.fromBytes(arquivo)
        );

        GetObjectRequest getRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();

        return presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofDays(days))
                        .getObjectRequest(getRequest)
                        .build()
        ).url().toString();
    }
}
