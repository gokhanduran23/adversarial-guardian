package com.example.guardian.anthropic;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Anthropic Messages API'ına ince sarmalayıcı.
 *
 * Sözleşme (docs.claude.com'dan teyit edildi):
 *   POST https://api.anthropic.com/v1/messages
 *   header: x-api-key: <anahtar>, anthropic-version: 2023-06-01, content-type: application/json
 *   gövde : { model, max_tokens, system, messages }
 *   yanıt : { content: [ { type, text }, ... ] }
 *
 * BYOK: anahtar çağrı başına dışarıdan gelir. ASLA saklanmaz, ASLA loglanmaz.
 */
@Component
public class AnthropicClient {

    private final RestClient http;
    private final AnthropicProperties props;

    public AnthropicClient(AnthropicProperties props) {
        this.props = props;
        this.http = RestClient.builder()
                .baseUrl(props.baseUrl())
                .build();
    }

    /**
     * Tek bir tamamlanma çağrısı.
     *
     * @param apiKey       kullanıcının kendi anahtarı (BYOK) — sadece bu istekte kullanılır
     * @param model        çağrılacak model
     * @param maxTokens    çıktı üst sınırı
     * @param systemPrompt sistem prompt'u (SIR — sunucuda kalır, istemciye inmez)
     * @param messages     {role, content} listesi (konuşma geçmişi + yeni mesaj)
     * @return modelin ürettiği düz metin (content bloklarının birleşimi)
     */
    public String complete(String apiKey,
                           String model,
                           int maxTokens,
                           String systemPrompt,
                           List<Map<String, String>> messages) {

        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "system", systemPrompt,
                "messages", messages
        );

        JsonNode response = http.post()
                .uri("/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", props.version())
                .header("content-type", "application/json")
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        return extractText(response);
    }

    /** content[] bloklarındaki tüm text parçalarını birleştirir. */
    private String extractText(JsonNode response) {
        if (response == null || !response.has("content")) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (JsonNode block : response.get("content")) {
            if ("text".equals(block.path("type").asText())) {
                sb.append(block.path("text").asText());
            }
        }
        return sb.toString().trim();
    }
}
