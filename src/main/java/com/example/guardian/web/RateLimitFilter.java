package com.example.guardian.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basit IP başına rate limiting.
 *
 * Amaç: tek bir kaynağın backend'i istek yağmuruna tutmasını engellemek
 * (spam / DoS). BYOK olduğu için bu, API faturasını değil, SUNUCU kaynağını
 * (Render CPU/bellek) korur.
 *
 * Yöntem: IP başına sabit zaman penceresi. Pencere içinde MAX_REQUESTS'i
 * aşan istekler 429 (Too Many Requests) ile reddedilir.
 *
 * Not: in-memory ve tek instance içindir (Render free tier tek instance).
 * Restart'ta sıfırlanır. Çok instance'lı üretimde Redis tabanlı bir çözüm gerekir.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 20;     // pencere başına izinli istek
    private static final long WINDOW_MS = 60_000;   // 1 dakikalık pencere

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        // Yalnızca API uçlarına uygula (Swagger, statik vs. serbest).
        if (!request.getRequestURI().startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }

        String ip = clientIp(request);
        if (isOverLimit(ip)) {
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"error\":\"Çok fazla istek gönderildi. Lütfen biraz bekleyip tekrar dene.\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isOverLimit(String ip) {
        long now = System.currentTimeMillis();
        Window w = windows.compute(ip, (key, existing) -> {
            if (existing == null || now - existing.start > WINDOW_MS) {
                return new Window(now); // yeni pencere
            }
            existing.count++;
            return existing;
        });
        return w.count > MAX_REQUESTS;
    }

    /**
     * Gerçek istemci IP'si. Render gibi proxy'ler arkasında gerçek IP,
     * X-Forwarded-For başlığının ilk değeridir; yoksa doğrudan bağlantı IP'si.
     */
    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class Window {
        final long start;
        int count;
        Window(long start) {
            this.start = start;
            this.count = 1;
        }
    }
}