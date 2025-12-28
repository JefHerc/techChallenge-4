package com.fiap.techchallenge;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fiap.techchallenge.application.usecase.GerarEnviarPdfUseCase;
import com.fiap.techchallenge.domain.model.JobInput;
import jakarta.inject.Inject;

import java.util.Map;

public class PdfJobHandler implements RequestHandler<Map<String, Object>, Void> {

    @Inject
    GerarEnviarPdfUseCase gerarEnviarPdfUseCase;

    @Override
    public Void handleRequest(Map<String, Object> stringObjectMap, Context context) {
        JobInput input = new JobInput(
                (String) stringObjectMap.get("jobId")
        );

        gerarEnviarPdfUseCase.execute(input);

        return null;
    }
}
