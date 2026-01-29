package com.sk.skala.stockapi.service;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.sk.skala.stockapi.config.Constant;
import com.sk.skala.stockapi.config.Error;
import com.sk.skala.stockapi.data.dto.PlayerSession;
import com.sk.skala.stockapi.exception.ResponseException;
import com.sk.skala.stockapi.tools.JsonTool;
import com.sk.skala.stockapi.tools.JwtTool;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class SessionHandler {

    /**
     * 현재 요청의 Cookie(JWT_ACCESS_COOKIE)에서 PlayerSession을 복원합니다.
     */
    public PlayerSession getPlayerSession() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            throw new ResponseException(Error.SESSION_NOT_FOUND, "request context not found");
        }

        HttpServletRequest request = attributes.getRequest();
        if (request == null) {
            throw new ResponseException(Error.SESSION_NOT_FOUND, "http request not found");
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new ResponseException(Error.SESSION_NOT_FOUND);
        }

        for (Cookie cookie : cookies) {
            if (cookie != null && Constant.JWT_ACCESS_COOKIE.equals(cookie.getName())) {
                String payload = JwtTool.getValidPayload(cookie.getValue());
                if (payload == null || payload.isBlank()) {
                    throw new ResponseException(Error.SESSION_NOT_FOUND, "invalid token payload");
                }
                return JsonTool.toObject(payload, PlayerSession.class);
            }
        }

        throw new ResponseException(Error.SESSION_NOT_FOUND);
    }

    /**
     * 현재 세션의 playerId를 꺼냅니다.
     */
    public String getPlayerId() {
        PlayerSession playerSession = getPlayerSession();
        return (playerSession != null) ? playerSession.getPlayerId() : null;
    }

    /**
     * ✅ PlayerService에서 필요로 하는 메서드
     * access token(JWT)을 생성하고 Cookie에 저장한 뒤,
     * "토큰 문자열"을 반환합니다.
     */
    public String createAccessToken(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new ResponseException(Error.PARAMETER_MISSED, "playerId");
        }

        PlayerSession playerSession = PlayerSession.builder()
                .playerId(playerId)
                .playerPassword("") // password는 절대 토큰에 넣지 않음
                .build();

        // cookie에 저장 (기존 로직 재사용)
        storeAccessToken(playerSession);

        // 토큰 문자열을 반환 (응답 body로 내려주고 싶을 때 사용)
        return JwtTool.generateToken(playerId, playerSession);
    }

    /**
     * access token(JWT)을 생성하고 Cookie에 저장합니다.
     * (password는 반드시 비웁니다)
     */
    public PlayerSession storeAccessToken(PlayerSession playerSession) {
        if (playerSession == null
                || playerSession.getPlayerId() == null
                || playerSession.getPlayerId().isBlank()) {
            throw new ResponseException(Error.PARAMETER_MISSED, "playerId");
        }

        // hide password
        playerSession.setPlayerPassword("");

        String token = JwtTool.generateToken(playerSession.getPlayerId(), playerSession);

        Cookie cookie = new Cookie(Constant.JWT_ACCESS_COOKIE, token);
        cookie.setMaxAge(Constant.JWT_ACCESS_TTL);
        cookie.setPath("/");
        cookie.setSecure(false);
        // 필요하면 아래도 고려 가능 (과제 요구사항에 따라)
        // cookie.setHttpOnly(true);

        ServletRequestAttributes attr =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();

        if (attr.getResponse() == null) {
            throw new ResponseException(Error.SYSTEM_ERROR, "http response not found");
        }

        attr.getResponse().addCookie(cookie);

        return playerSession;
    }
}
