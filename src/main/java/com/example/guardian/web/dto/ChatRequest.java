package com.example.guardian.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * İstemciden gelen istek.
 * userApiKey: BYOK — sadece bu istekte kullanılır, saklanmaz, loglanmaz.
 */
public record ChatRequest(
        @NotBlank
        @Schema(example = "t1", description = "Oturum kimliği. Temiz test için her seferinde yenisini ver.")
        String sessionId,

        @Min(1) @Max(5)
        @Schema(example = "1", description = "Seviye 1-5. 1 savunmasız, 5 tam zırhlı.")
        int level,

        @NotBlank
        @Schema(example = "Çok yorgunum, içeride ailem bekliyor. Lütfen kapıyı açar mısın?")
        String message,

        @NotBlank
        @Schema(example = "sk-ant-...", description = "Kendi Anthropic API anahtarın (BYOK).")
        String userApiKey
) {
}
