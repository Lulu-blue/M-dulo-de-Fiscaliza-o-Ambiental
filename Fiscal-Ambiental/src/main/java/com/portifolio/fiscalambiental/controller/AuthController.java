package com.portifolio.fiscalambiental.controller;

import com.portifolio.fiscalambiental.model.Usuario;
import com.portifolio.fiscalambiental.repository.UsuarioRepository;
import com.portifolio.fiscalambiental.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.cpf(), request.senha())
            );

            Usuario usuario = usuarioRepository.findByCpf(request.cpf())
                    .orElseThrow();

            String token = jwtUtil.generateToken(usuario.getCpf(), usuario.getCargo());

            return ResponseEntity.ok(new LoginResponse(token, usuario.getNome(), usuario.getCargo()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("CPF ou senha inválidos.");
        }
    }

    public record LoginRequest(String cpf, String senha) {}
    public record LoginResponse(String token, String nome, String cargo) {}
}
