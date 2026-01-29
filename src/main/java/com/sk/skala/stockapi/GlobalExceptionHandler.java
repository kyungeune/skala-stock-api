package com.sk.skala.stockapi;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sk.skala.stockapi.config.Error;
import com.sk.skala.stockapi.data.dto.Response;
import com.sk.skala.stockapi.exception.ParameterException;
import com.sk.skala.stockapi.exception.ResponseException;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ✅ @Valid 바인딩 오류 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }

        log.warn("Validation error: {}", errors);

        return Response.builder()
                .result(0)
                .code(Error.PARAMETER_MISSED.getCode())
                .message("요청 값이 올바르지 않습니다.")
                .error(errors)
                .build();
    }

    // ✅ JSON 파싱 실패 (빈 body, 타입 오류 등)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Response handleNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Request body parse error: {}", ex.getMessage());
        return Response.builder()
                .result(0)
                .code(Error.PARAMETER_MISSED.getCode())
                .message("요청 Body가 올바르지 않습니다.")
                .error(ex.getMessage())
                .build();
    }

    // 기존 커스텀 예외들
    @ExceptionHandler(ParameterException.class)
    public Response takeParameterException(ParameterException e) {
        return Response.builder()
                .result(0)
                .code(e.getCode())
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler(ResponseException.class)
    public Response takeResponseException(ResponseException e) {
        return Response.builder()
                .result(0)
                .code(e.getCode())
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler(SecurityException.class)
    public Response takeSecurityException(SecurityException e) {
        return Response.builder()
                .result(0)
                .code(Error.NOT_AUTHENTICATED.getCode())
                .message(e.getMessage())
                .build();
    }

    // 최후의 예외
    @ExceptionHandler(Exception.class)
    public Response takeException(Exception e) {
        log.error("Unhandled exception", e);
        return Response.builder()
                .result(0)
                .code(Error.SYSTEM_ERROR.getCode())
                .message(Error.SYSTEM_ERROR.getMessage())
                .error(e.getMessage())
                .build();
    }
}
