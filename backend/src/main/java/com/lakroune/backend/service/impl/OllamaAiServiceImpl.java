package com.lakroune.backend.service.impl;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lakroune.backend.service.OllamaAiService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OllamaAiServiceImpl implements OllamaAiService {

    @Value("${ollama.api.url:http://localhost:11434}")
    private String ollamaApiUrl;

    @Value("${ollama.model:llava:7b}")
    private String ollamaModel;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper  = new ObjectMapper();


    @Override
    public Map<String, String> analyzeCinWithOllama(byte[] imageBytes) {
        log.info("Starting CIN analysis with Ollama AI (model: {})", ollamaModel);

        String prompt = """
                IMPORTANT: You must ONLY read and transcribe the EXACT text visible on this
                Moroccan ID card image. DO NOT make up, guess, or invent any information.
                If you cannot see the text clearly, write "Non visible".

                Read these fields from the card:
                - Nom (Last name) - usually in capital letters
                - Prenom (First name) - usually in capital letters
                - NumeroCIN (ID number) - format: 2 letters + 6 digits  (e.g. HH190300)
                - DateNaissance (Birth date) - format: DD.MM.YYYY or DD/MM/YYYY
                - LieuNaissance (Birth place) - city name
                - DateExpiration (Expiry date) - format: DD.MM.YYYY or DD/MM/YYYY
                - Nationalite (Nationality) - usually "MAROC" or "MOROCCO"
                - Sexe (Gender) - M or F

                Output format (use EXACT text from the image):
                Nom: [exact text from card]
                Prenom: [exact text from card]
                NomFamille: [same as Nom]
                NumeroCIN: [exact number from card]
                DateNaissance: [exact date from card]
                LieuNaissance: [exact place from card]
                DateExpiration: [exact date from card]
                DateEmission: [if visible, otherwise "Non visible"]
                Nationalite: [MAROC or MOROCCO]
                Sexe: [M or F]

                CRITICAL: Only transcribe what you actually see. Do not invent data.
                """;

        try {
            String aiResponse = analyzeImageWithPrompt(imageBytes, prompt);
            Map<String, String> extractedData = parseOllamaResponse(aiResponse);
            log.info(" CIN analysis completed. Extracted {} fields", extractedData.size());
            return extractedData;

        } catch (Exception e) {
            log.error(" Failed to analyze CIN with Ollama: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error",   "Failed to analyze image with Ollama AI");
            error.put("details", e.getMessage());
            error.put("status",  "error");
            return error;
        }
    }

    @Override
    public String analyzeImageWithPrompt(byte[] imageBytes, String prompt) {
        log.info(" Sending image to Ollama API: {}/api/generate", ollamaApiUrl);

        try {
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model",  ollamaModel);
            requestBody.put("prompt", prompt);
            requestBody.put("images", new String[]{base64Image});
            requestBody.put("stream", false);

            Map<String, Object> options = new HashMap<>();
            options.put("temperature", 0.1);
            options.put("top_p",       0.5);
            options.put("top_k",       10);
            requestBody.put("options", options);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = ollamaApiUrl + "/api/generate";
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode json     = objectMapper.readTree(response.getBody());
                String aiResponse = json.get("response").asText();
                log.info(" Ollama response received ({} chars)", aiResponse.length());
                log.debug("Raw AI Response: {}", aiResponse);
                return aiResponse;
            } else {
                throw new RuntimeException("Ollama API error: " + response.getStatusCode());
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Ollama JSON response", e);
        } catch (Exception e) {
            log.error(" Failed to call Ollama API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to communicate with Ollama: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isOllamaAvailable() {
        try {
            ResponseEntity<String> response =
                restTemplate.getForEntity(ollamaApiUrl + "/api/tags", String.class);
            boolean ok = response.getStatusCode() == HttpStatus.OK;
            if (ok) log.info(" Ollama server available at {}", ollamaApiUrl);
            else    log.warn("⚠ Ollama returned status: {}", response.getStatusCode());
            return ok;
        } catch (Exception e) {
            log.error(" Ollama not reachable at {}: {}", ollamaApiUrl, e.getMessage());
            return false;
        }
    }

    @Override
    public String getCurrentModel() {
        return ollamaModel;
    }


    private Map<String, String> parseOllamaResponse(String aiResponse) {
        Map<String, String> result = new HashMap<>();

        Map<String, String> fieldPatterns = new HashMap<>();
        fieldPatterns.put("nom",            "Nom:\\s*(.+?)(?=\\n|$)");
        fieldPatterns.put("prenom",         "Prenom:\\s*(.+?)(?=\\n|$)");
        fieldPatterns.put("nomFamille",     "NomFamille:\\s*(.+?)(?=\\n|$)");
        fieldPatterns.put("numeroCIN",      "NumeroCIN:\\s*(.+?)(?=\\n|$)");
        fieldPatterns.put("dateNaissance",  "DateNaissance:\\s*(.+?)(?=\\n|$)");
        fieldPatterns.put("lieuNaissance",  "LieuNaissance:\\s*(.+?)(?=\\n|$)");
        fieldPatterns.put("dateExpiration", "DateExpiration:\\s*(.+?)(?=\\n|$)");
        fieldPatterns.put("dateEmission",   "DateEmission:\\s*(.+?)(?=\\n|$)");
        fieldPatterns.put("nationalite",    "Nationalite:\\s*(.+?)(?=\\n|$)");
        fieldPatterns.put("sexe",           "Sexe:\\s*(.+?)(?=\\n|$)");

        for (Map.Entry<String, String> entry : fieldPatterns.entrySet()) {
            Matcher m = Pattern.compile(entry.getValue(), Pattern.CASE_INSENSITIVE)
                               .matcher(aiResponse);
            if (m.find()) {
                String value = m.group(1).trim();
                if (!value.equalsIgnoreCase("Non visible") && !value.isEmpty()) {
                    result.put(entry.getKey(), value);
                }
            }
        }

        result.put("rawResponse", aiResponse);
        result.put("model",       ollamaModel);
        result.put("source",      "Ollama AI");
        result.put("status",      "success");

        double confidence = (double) (result.size() - 4) / 10.0;
        result.put("confidence", String.format("%.2f", confidence));

        return result;
    }
}
