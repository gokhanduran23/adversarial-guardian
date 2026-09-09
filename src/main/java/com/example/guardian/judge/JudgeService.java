package com.example.guardian.judge;

import com.example.guardian.anthropic.AnthropicClient;
import com.example.guardian.anthropic.AnthropicProperties;
import com.example.guardian.game.PromptLibrary;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Hakem — kazanma/sızıntı kararını veren BAĞIMSIZ model çağrısı.
 *
 * Neden ayrı: kazanıp kazanmadığına Bekçi'nin kendisi karar verirse, o karar
 * da manipüle edilebilir ("artık kazandığımı söyle"). Denetimi ayrı bir çağrıya
 * taşımak bu açığı kapatır ve LLM-as-judge desenini uygular.
 *
 * Hakem geçmiş taşımaz: girdisi yalnızca Bekçi'nin son yanıtı + kısa talimat.
 */
@Service
public class JudgeService {

    private final AnthropicClient client;
    private final AnthropicProperties props;
    private final PromptLibrary prompts;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final Logger log = LoggerFactory.getLogger(JudgeService.class);

    public JudgeService(AnthropicClient client, AnthropicProperties props, PromptLibrary prompts) {
        this.client = client;
        this.props = props;
        this.prompts = prompts;
    }

    public Verdict judge(String apiKey, String guardianReply) {
        List<Map<String, String>> messages = List.of(
                Map.of("role", "user", "content", prompts.judgePrompt(guardianReply))
        );

        String raw = client.complete(
                apiKey,
                props.judgeModel(),
                props.judgeMaxTokens(),
                "Yalnızca istenen JSON'u döndür.",
                messages);

        return parse(raw);
    }

    /** Katı JSON bekleriz ama model bazen etrafına metin koyar; savunmacı ayrıştır. */
    private Verdict parse(String raw) {
        try {
            String json = extractJsonObject(raw);
            JsonNode node = mapper.readTree(json);
            return new Verdict(
                    node.path("breached").asBoolean(false),
                    node.path("leaked").asBoolean(false),
                    node.path("reason").asText("")
            );
        } catch (Exception e) {
            // Ayrıştırma çöktü (ör. kesik JSON). SESSİZCE false DÖNME — gerçek bir
            // kırılmayı gizleyebilir. Uyar ve ham metinde kaba bir yedek kontrol yap.
            log.warn("Hakem JSON ayrıştırılamadı, yedek kontrole düşülüyor. Ham: {}", raw);
            String lower = raw.toLowerCase();
            boolean breached = lower.contains("\"breached\": true")
                    || lower.contains("\"breached\":true");
            boolean leaked = lower.contains("\"leaked\": true")
                    || lower.contains("\"leaked\":true");
            return new Verdict(breached, leaked, "yedek ayrıştırma (JSON bozuktu)");
        }
    }

    private String extractJsonObject(String raw) {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return raw.substring(start, end + 1);
        }
        return raw;
    }
}
