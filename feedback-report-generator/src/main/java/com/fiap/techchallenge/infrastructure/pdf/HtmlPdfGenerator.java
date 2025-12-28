package com.fiap.techchallenge.infrastructure.pdf;

import com.fiap.techchallenge.domain.model.PdfData;
import com.fiap.techchallenge.domain.service.PdfGenerator;
import com.fiap.techchallenge.infrastructure.dynamo.DynamoPdfDataService;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.io.ByteArrayOutputStream;

@ApplicationScoped
public class HtmlPdfGenerator implements PdfGenerator {

    private static final Logger LOG = Logger.getLogger(HtmlPdfGenerator.class);

    private final DynamoPdfDataService dynamoPdfDataService;

    public HtmlPdfGenerator(DynamoPdfDataService dynamoPdfDataService) {
        this.dynamoPdfDataService = dynamoPdfDataService;
    }

    @Override
    public byte[] gerarPdf() {
        PdfData pdfData = dynamoPdfDataService.avaliacoesUltimaSemana();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Title
            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD);
            Paragraph title = new Paragraph(pdfData.title(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));

            // Body content
            Font bodyFont = new Font(Font.HELVETICA, 12, Font.NORMAL);
            document.add(new Paragraph(new Phrase("Media de avaliações da ultima semana: " + pdfData.mediaAvaliacoes(), bodyFont)));
            document.add(new Paragraph(new Phrase("Quatidade de avaliações da ultima semana: " + pdfData.quantidadeTotalAvaliacoes(), bodyFont)));
            document.add(new Paragraph(new Phrase("Quatidade de avaliações criticas: " + pdfData.quantidadeAvaliacoesCriticas(), bodyFont)));

            document.close();

            LOG.debug("PDF Gerado com sucesso");
            return baos.toByteArray();
        } catch (Exception e) {
            LOG.error("Ocorreu um erro ao gerar o PDF [" + e.getMessage() + "]");
            throw new RuntimeException("Ocorreu um erro ao gerar o PDF", e);
        }
    }
}
