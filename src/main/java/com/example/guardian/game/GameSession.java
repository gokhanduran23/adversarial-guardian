package com.example.guardian.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tek bir oyun oturumunun durumu.
 *
 * NOT: LLM stateless'tır — her çağrıda tüm geçmişi yeniden göndeririz.
 * Geçmiş burada birikir; token yönetimi için GuardianService bunu kırpar.
 */
public class GameSession {

    /** Seviye başına sert tur limiti — hem maliyet tavanı hem oyun tasarımı. */
    public static final int TURN_LIMIT = 15;

    private final String sessionId;
    private final int level;
    private final List<Map<String, String>> history = new ArrayList<>();
    private int turns = 0;
    private Status status = Status.IN_PROGRESS;

    public enum Status { IN_PROGRESS, WON, LOST }

    public GameSession(String sessionId, int level) {
        this.sessionId = sessionId;
        this.level = level;
    }

    public String sessionId() { return sessionId; }
    public int level()        { return level; }
    public int turns()        { return turns; }
    public Status status()    { return status; }

    /** Bekçi'ye gönderilecek tam konuşma geçmişi (rol/içerik çiftleri). */
    public List<Map<String, String>> history() { return history; }

    public void appendUser(String message) {
        history.add(Map.of("role", "user", "content", message));
    }

    public void appendAssistant(String reply) {
        history.add(Map.of("role", "assistant", "content", reply));
        turns++;
    }

    public void markWon()  { this.status = Status.WON; }
    public void markLost() { this.status = Status.LOST; }

    public boolean turnLimitReached() { return turns >= TURN_LIMIT; }
}
