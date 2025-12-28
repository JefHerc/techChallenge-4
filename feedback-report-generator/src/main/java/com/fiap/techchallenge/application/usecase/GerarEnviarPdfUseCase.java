package com.fiap.techchallenge.application.usecase;

import com.fiap.techchallenge.domain.model.JobInput;
import com.fiap.techchallenge.domain.service.EmailSender;
import com.fiap.techchallenge.domain.service.PdfGenerator;
import com.fiap.techchallenge.domain.service.StorageService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GerarEnviarPdfUseCase {

    private final PdfGenerator pdfGenerator;
    private final StorageService storageService;
    private final EmailSender emailSender;

    public GerarEnviarPdfUseCase(
            PdfGenerator pdfGenerator,
            StorageService storageService,
            EmailSender emailSender
    ) {
        this.pdfGenerator = pdfGenerator;
        this.storageService = storageService;
        this.emailSender = emailSender;
    }

    public void execute(JobInput input) {
        byte[] pdf = pdfGenerator.gerarPdf();

        String url = storageService.store(pdf, "relatorio.pdf");

        emailSender.send(
                "Relatório disponível",
                "Seu relatório foi gerado: " + url
        );
    }
}
