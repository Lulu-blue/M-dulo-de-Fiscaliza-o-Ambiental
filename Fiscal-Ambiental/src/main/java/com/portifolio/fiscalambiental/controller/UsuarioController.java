package com.portifolio.fiscalambiental.controller;

import com.portifolio.fiscalambiental.model.Usuario;
import com.portifolio.fiscalambiental.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<Usuario>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @GetMapping("/matricula/{matricula}")
    public ResponseEntity<Usuario> buscarPorMatricula(@PathVariable String matricula) {
        return ResponseEntity.ok(usuarioService.buscarPorMatricula(matricula));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('GESTOR')")
    public ResponseEntity<Usuario> salvar(@Valid @RequestBody Usuario usuario) {
        return ResponseEntity.ok(usuarioService.salvar(usuario));
    }
}
