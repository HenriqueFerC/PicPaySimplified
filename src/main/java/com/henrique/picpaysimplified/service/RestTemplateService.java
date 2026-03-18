package com.henrique.picpaysimplified.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RestTemplateService {

    private final RestTemplate restTemplate;

    @Value("${api.authorization.url}")
    private String AUTHORIZATION_URL = "";

    public boolean authorizeTransaction() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(AUTHORIZATION_URL, Map.class);
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("status")) {
                String message = (String) responseBody.get("status");
                return "success".equalsIgnoreCase(message);
            } else return false;
        } catch (HttpClientErrorException e) {
            return false;
        }
    }
}
