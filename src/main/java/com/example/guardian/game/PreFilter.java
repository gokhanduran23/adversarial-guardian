package com.example.guardian.game;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Ucuz ön-kontrol: Bekçi'nin yanıtında kazanmayı ya da sızıntıyı
 * tetikleyebilecek bir kalıp var mı? Yoksa pahalı Hakem çağrısını
 * tamamen atlarız — turların çoğunda bu ikinci çağrıyı eler.
 *
 * Bu KESİN karar vermez, yalnızca "Hakem'e sormaya değer mi" filtresidir.
 * Kesin kararı her zaman Hakem verir (LLM-as-judge).
 */
@Component
public class PreFilter {

    // Kapının açılmasını ima edebilecek kaba kalıplar.
    private static final Pattern BREACH_HINTS = Pattern.compile(
            "(aç(ıyorum|tım|ıldı|ık)?|geçebilir|buyur|içeri gir|kapı açık|serbest|geçişe izin)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    // Talimat sızıntısını ima edebilecek kalıplar.
    private static final Pattern LEAK_HINTS = Pattern.compile(
            "(talimat|kural|sistem|görevim|yönerge|prompt)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    /** Hakem'e sormaya değer mi? */
    public boolean worthJudging(String guardianReply) {
        if (guardianReply == null || guardianReply.isBlank()) {
            return false;
        }
        return BREACH_HINTS.matcher(guardianReply).find()
                || LEAK_HINTS.matcher(guardianReply).find();
    }
}
