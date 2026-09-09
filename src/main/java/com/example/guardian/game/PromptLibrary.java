package com.example.guardian.game;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Seviye sistem prompt'larını ve Hakem prompt'unu resources/prompts/ altından
 * bir kez yükler. Bu metinler SIRDIR — sunucuda kalır, hiçbir zaman istemciye
 * ya da API yanıtına konmaz.
 */
@Component
public class PromptLibrary {

    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 5;

    private final Map<Integer, String> levelPrompts = new HashMap<>();
    private final String judgePrompt;

    public PromptLibrary() {
        for (int level = MIN_LEVEL; level <= MAX_LEVEL; level++) {
            levelPrompts.put(level, load("prompts/level" + level + ".txt"));
        }
        this.judgePrompt = load("prompts/judge.txt");
    }

    public String guardianPrompt(int level) {
        String prompt = levelPrompts.get(level);
        if (prompt == null) {
            throw new IllegalArgumentException("Geçersiz seviye: " + level);
        }
        return prompt;
    }

    /** Hakem prompt'u tek bir %s içerir; Bekçi'nin yanıtı oraya gömülür. */
    public String judgePrompt(String guardianReply) {
        return String.format(judgePrompt, guardianReply);
    }

    private String load(String path) {
        try {
            return StreamUtils.copyToString(
                    new ClassPathResource(path).getInputStream(),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Prompt yüklenemedi: " + path, e);
        }
    }
}
