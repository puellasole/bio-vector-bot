package edu.diploma.biovectorbot.client;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import okhttp3.OkHttpClient;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Component
public class BioVectorBotClient {
	
	@Value("${deepseek.url}")
    private String URL;
    
    @Value("${deepseek.api.key}")
    private String apiKey;
    
    @Autowired
    private OkHttpClient client;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    /**
     * Отправляет запрос к DeepSeek API и возвращает только текст ответа
     */
    public String getChatResponse(String fullprompt) throws IOException {
        // Создаем JSON запрос
        String jsonRequest = buildJsonRequest(fullprompt);
        
        RequestBody requestBody = RequestBody.create(jsonRequest, JSON);
        
        Request request = new Request.Builder()
            .url(URL)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiKey)
            .post(requestBody)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Ошибка запроса к DeepSeek API: " + response.code() + " " + response.message());
            }
            
            String responseBody = response.body().string();
            return extractMessageFromResponse(responseBody);
        }
    }
    
    private String buildJsonRequest(String userMessage) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", "deepseek-chat");
        root.put("stream", false);
        //token and temp limits
        root.put("max_tokens", 1200);       
        root.put("temperature", 0.3);
        
        ArrayNode messages = root.putArray("messages");
        
        // Только одно user сообщение
        ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        
        return root.toString();
    }
    
    /**
     * Извлекает только текст ответа из JSON ответа DeepSeek
     */
    private String extractMessageFromResponse(String jsonResponse) throws IOException {
        JsonNode root = objectMapper.readTree(jsonResponse);
        
        // Путь к тексту ответа: choices[0].message.content
        JsonNode choices = root.path("choices");
        if (choices.isArray() && choices.size() > 0) {
            JsonNode message = choices.get(0).path("message");
            return message.path("content").asText();
        }
        
        throw new IOException("Не удалось извлечь ответ из JSON: " + jsonResponse);
    }
}
