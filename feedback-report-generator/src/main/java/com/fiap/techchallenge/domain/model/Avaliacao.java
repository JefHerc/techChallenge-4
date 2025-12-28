package com.fiap.techchallenge.domain.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Avaliacao {

    private String feedbackId;
    private String descricao;
    private Integer nota;
    private String status;
    private String dataEnvio;
    private String userId;

    public Avaliacao(String feedbackId, String descricao, Integer nota, String status, String dataEnvio, String userId) {
        this.feedbackId = feedbackId;
        this.descricao = descricao;
        this.nota = nota;
        this.status = status;
        this.dataEnvio = dataEnvio;
        this.userId = userId;
    }

    public String getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(String feedbackId) {
        this.feedbackId = feedbackId;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(String dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
