package com.fiap.techchallenge.ingestion.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class FeedbackRequest {

    @NotBlank(message = "O campo descricao é obrigatória")
    private String descricao;

    @Min(value = 0, message = "Nota mínima é 0")
    @Max(value = 10, message = "Nota máxima é 10")
    @NotNull(message = "O campo nota é obrigatória")
    private Integer nota;

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getNota() {
        return nota;
    }

    public void setNota(Integer nota) {
        this.nota = nota;
    }
}
