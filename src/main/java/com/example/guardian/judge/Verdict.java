package com.example.guardian.judge;

/** Hakem'in kararı: kapı kırıldı mı, talimat sızdı mı, kısa gerekçe. */
public record Verdict(boolean breached, boolean leaked, String reason) {

    public static Verdict safe() {
        return new Verdict(false, false, "ön-filtre: tetik yok");
    }
}
