package com.unicksbyte.smart_email_generator.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicksbyte.smart_email_generator.entity.EmailRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.Map;
import java.util.function.Function;

@Service
public class EmailGeneratorService {


    private final WebClient webClient;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;




    public EmailGeneratorService(WebClient.Builder webClientbuilder) {
        this.webClient = webClientbuilder.build();
    }

    public String generateEmailReply(EmailRequest emailRequest)
    {
        //build a prompt

        String prompt = buildPrompt(emailRequest);

        //craft the request

        Map<String, Object> requestBody = Map.of(
                "contents", java.util.List.of(
                        Map.of("parts", java.util.List.of(
                                Map.of("text", prompt)
                        ))
                )
        );


        String geminiFinalUrl = geminiApiUrl + "?key=" + geminiApiKey;

        //do request and get response

        String response = webClient.post()
                .uri(geminiFinalUrl)
                .header("Content-Type","application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        //return response

        return extractResponseContent(response);

    }

    private String extractResponseContent(String response) {
        try{
            ObjectMapper mapper =new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);
            return rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        }
        catch (Exception e)
        {
            return "Error processing request"+ e.getMessage();
        }
    }

    private String buildPrompt(EmailRequest emailRequest) {

        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a professional email reply for the following email content .please don't generate a subject line");
        if(emailRequest.getTone() != null && !emailRequest.getTone().isEmpty())
        {
            prompt.append("use a").append(emailRequest.getTone()).append("tone");
        }

        prompt.append("\noriginal email :").append(emailRequest.getEmailContent());
        return  prompt.toString();
    }
}
