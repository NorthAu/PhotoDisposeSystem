package com.photodisposesystem.service;

import com.photodisposesystem.config.AppProperties;
import java.net.URI;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class AiInferenceService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String aiServiceUrl;

    public AiInferenceService(AppProperties appProperties) {
        this.aiServiceUrl = appProperties.getAiServiceUrl();
    }

    public String requestEnhancement(Long imageId) {
        return callAiService("/enhance", imageId);
    }

    public String requestStyleTransfer(Long imageId, String style) {
        return callAiService("/style-transfer?style=" + style, imageId);
    }

    public String requestBackgroundReplace(Long imageId, String template) {
        return callAiService("/background-replace?template=" + template, imageId);
    }

    private String callAiService(String path, Long imageId) {
        try {
            URI uri = URI.create(aiServiceUrl + path);
            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    new HttpEntity<>("imageId=" + imageId),
                    String.class
            );
            return response.getBody();
        } catch (RestClientException ex) {
            return "AI service unavailable: " + ex.getMessage();
        }
    }
}
