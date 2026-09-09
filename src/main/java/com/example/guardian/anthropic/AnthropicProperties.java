package com.example.guardian.anthropic;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Model adları ve token limitleri application.yml'den okunur.
 * Model string'leri zamanla değişir — güncel değerleri docs.claude.com'dan teyit et.
 */
@ConfigurationProperties(prefix = "anthropic")
public record AnthropicProperties(
        String baseUrl,
        String version,
        String guardianModel,   // ucuz model — Bekçi rolü
        String judgeModel,      // ucuz model — Hakem denetimi
        int guardianMaxTokens,  // Bekçi yanıtını kısa tut (maliyet + oyun hissi)
        int judgeMaxTokens      // Hakem yalnızca minik JSON döndürür
) {
}
