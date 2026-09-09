package com.example.guardian.web.dto;

/**
 * İstemciye dönen yanıt.
 * DİKKAT: sistem prompt'u, Hakem gerekçesi gibi SIRLAR burada ASLA yer almaz.
 */
public record ChatResponse(
        String reply,
        boolean won,
        int turns,
        String status
) {
}
