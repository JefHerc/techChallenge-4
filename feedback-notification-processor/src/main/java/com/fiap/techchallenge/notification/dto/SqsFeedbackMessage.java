package com.fiap.techchallenge.notification.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class SqsFeedbackMessage {
    private String feedbackId;

    public String getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(String feedbackId) {
        this.feedbackId = feedbackId;
    }
}