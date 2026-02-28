package com.lakroune.backend.service;

import java.util.Map;


public interface OllamaAiService {

    Map<String, String> analyzeCinWithOllama(byte[] imageBytes);

    String analyzeImageWithPrompt(byte[] imageBytes, String prompt);
    boolean isOllamaAvailable();
    String getCurrentModel();
}
