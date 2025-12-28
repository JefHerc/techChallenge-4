package com.fiap.techchallenge.domain.model;

public record PdfData(
        String title,
        Long quantidadeTotalAvaliacoes,
        Double mediaAvaliacoes,
        Long quantidadeAvaliacoesCriticas
        ) {
}
