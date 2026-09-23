package com.portifolio.fiscalambiental.service;

import com.portifolio.fiscalambiental.model.Usuario;
import com.portifolio.fiscalambiental.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public Usuario buscarPorMatricula(String matricula) {
        return usuarioRepository.findByMatricula(matricula)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com a matrícula: " + matricula));
    }

    public Usuario buscarPorId(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o ID: " + id));
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }
    
    public Usuario salvar(Usuario usuario) {
        usuario.setSenhaHash(passwordEncoder.encode(usuario.getSenhaHash()));
        return usuarioRepository.save(usuario);
    }
}
