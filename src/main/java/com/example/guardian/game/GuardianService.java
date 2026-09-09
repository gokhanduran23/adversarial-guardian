package com.example.guardian.game;

import com.example.guardian.anthropic.AnthropicClient;
import com.example.guardian.anthropic.AnthropicProperties;
import com.example.guardian.judge.JudgeService;
import com.example.guardian.judge.Verdict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Bir turun tam akışı:
 *
 *   1) geçmişi kırp (token yönetimi)
 *   2) Bekçi çağrısı  (kullanıcının anahtarı, ucuz model, kısa yanıt)
 *   3) ön-filtre      (tetik yoksa Hakem'i atla)
 *   4) Hakem çağrısı  (gerekirse — bağımsız karar)
 *   5) durumu güncelle (WON / turn limit → LOST)
 */
@Service
public class GuardianService {

    /** Bekçi'ye gönderilecek en fazla geçmiş turu (kayan pencere). */
    private static final int HISTORY_WINDOW_MESSAGES = 24; // ~12 tur (user+assistant)

    private final AnthropicClient client;
    private final AnthropicProperties props;
    private final PromptLibrary prompts;
    private final PreFilter preFilter;
    private final JudgeService judge;

    public GuardianService(AnthropicClient client,
                           AnthropicProperties props,
                           PromptLibrary prompts,
                           PreFilter preFilter,
                           JudgeService judge) {
        this.client = client;
        this.props = props;
        this.prompts = prompts;
        this.preFilter = preFilter;
        this.judge = judge;
    }

    public TurnResult playTurn(String apiKey, GameSession session, String userMessage) {

        if (session.status() != GameSession.Status.IN_PROGRESS) {
            return new TurnResult("Bu oturum zaten bitti.", session.status(), session.turns());
        }

        session.appendUser(userMessage);

        // 1) geçmişi kırp — birikimli token büyümesini sınırla
        List<Map<String, String>> trimmed = trimHistory(session.history());

        // 2) Bekçi çağrısı
        String reply = client.complete(
                apiKey,
                props.guardianModel(),
                props.guardianMaxTokens(),
                prompts.guardianPrompt(session.level()),
                trimmed);

        session.appendAssistant(reply);

        // 3) + 4) ön-filtre, sonra gerekirse Hakem
        Verdict verdict = preFilter.worthJudging(reply)
                ? judge.judge(apiKey, reply)
                : Verdict.safe();

        // 5) durumu güncelle
        if (verdict.breached() || verdict.leaked()) {
            session.markWon();
        } else if (session.turnLimitReached()) {
            session.markLost();
        }

        return new TurnResult(reply, session.status(), session.turns());
    }

    /** Kayan pencere: yalnızca son N mesajı gönder. */
    private List<Map<String, String>> trimHistory(List<Map<String, String>> history) {
        int size = history.size();
        if (size <= HISTORY_WINDOW_MESSAGES) {
            return history;
        }
        return history.subList(size - HISTORY_WINDOW_MESSAGES, size);
    }

    /** Turun istemciye dönecek sonucu — SIR (sistem prompt'u, karar gerekçesi) burada YOK. */
    public record TurnResult(String reply, GameSession.Status status, int turns) {
        public boolean won() { return status == GameSession.Status.WON; }
    }
}
