package com.example.guardian.web;

import com.example.guardian.game.GameSession;
import com.example.guardian.game.GuardianService;
import com.example.guardian.game.SessionStore;
import com.example.guardian.web.dto.ChatRequest;
import com.example.guardian.web.dto.ChatResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * İnce proxy katmanı. İstemci yalnızca mesaj gönderir; sistem prompt'u
 * ve kazanma kararı tamamen sunucuda kalır.
 */
@RestController
@RequestMapping("/api")
public class ChatController {

    private final SessionStore sessions;
    private final GuardianService guardian;

    public ChatController(SessionStore sessions, GuardianService guardian) {
        this.sessions = sessions;
        this.guardian = guardian;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest req) {
        GameSession session = sessions.getOrCreate(req.sessionId(), req.level());

        GuardianService.TurnResult result =
                guardian.playTurn(req.userApiKey(), session, req.message());

        return new ChatResponse(
                result.reply(),
                result.won(),
                result.turns(),
                result.status().name());
    }
}
