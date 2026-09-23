package com.portifolio.fiscalambiental.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(erro ->
            erros.put(erro.getField(), erro.getDefaultMessage())
        );

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mensagem", "Dados inválidos.");
        body.put("erros", erros);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
