package com.example.guardian.game;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Basit in-memory oturum deposu (iskelet).
 *
 * Üretimde: Redis ya da veritabanı; ayrıca oturum başına TTL ve
 * kullanıcı/oturum bütçe sayacı (token/çağrı tavanı) buraya eklenir.
 */
@Component
public class SessionStore {

    private final ConcurrentHashMap<String, GameSession> sessions = new ConcurrentHashMap<>();

    public GameSession getOrCreate(String sessionId, int level) {
        return sessions.computeIfAbsent(sessionId, id -> new GameSession(id, level));
    }

    public void remove(String sessionId) {
        sessions.remove(sessionId);
    }
}
