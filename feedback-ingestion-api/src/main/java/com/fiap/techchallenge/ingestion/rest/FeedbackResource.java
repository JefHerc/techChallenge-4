package com.fiap.techchallenge.ingestion.rest;

import com.fiap.techchallenge.ingestion.dto.FeedbackRequest;
import com.fiap.techchallenge.ingestion.model.Feedback;
import com.fiap.techchallenge.ingestion.service.FeedbackService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/avaliacao")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FeedbackResource {

    @Inject
    FeedbackService service;

    @POST
    public Response criarAvaliacao(@Valid FeedbackRequest request, @Context SecurityContext securityContext) {
        String userId = null;
        if (securityContext != null && securityContext.getUserPrincipal() != null) {
            userId = securityContext.getUserPrincipal().getName();
        }
        Feedback feedback = service.processarFeedback(request, userId);
        return Response.status(Response.Status.CREATED).entity(feedback).build();
    }
}
