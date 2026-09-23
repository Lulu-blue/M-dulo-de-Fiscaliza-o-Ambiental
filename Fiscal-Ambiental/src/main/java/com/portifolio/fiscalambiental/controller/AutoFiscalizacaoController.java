package com.portifolio.fiscalambiental.controller;

import com.portifolio.fiscalambiental.model.AutoFiscalizacao;
import com.portifolio.fiscalambiental.model.Usuario;
import com.portifolio.fiscalambiental.repository.UsuarioRepository;
import com.portifolio.fiscalambiental.service.AutoFiscalizacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/autos")
@RequiredArgsConstructor
public class AutoFiscalizacaoController {

    private final AutoFiscalizacaoService autoFiscalizacaoService;
    private final UsuarioRepository usuarioRepository;

    // Só consulta — a emissão fica em FiscalController (POST /api/fiscal/autos).

    @GetMapping
    public ResponseEntity<?> listarTodos() {
        if (!isGestor()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acesso restrito ao Gestor."));
        }
        return ResponseEntity.ok(autoFiscalizacaoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable UUID id) {
        AutoFiscalizacao auto = autoFiscalizacaoService.buscarPorId(id);
        ResponseEntity<?> erroAcesso = validarAcesso(auto);
        if (erroAcesso != null) {
            return erroAcesso;
        }
        return ResponseEntity.ok(auto);
    }

    @GetMapping("/{id}/documento-assinado")
    public ResponseEntity<?> baixarDocumentoAssinado(@PathVariable UUID id) {
        AutoFiscalizacao auto = autoFiscalizacaoService.buscarPorId(id);
        ResponseEntity<?> erroAcesso = validarAcesso(auto);
        if (erroAcesso != null) {
            return erroAcesso;
        }
        if (auto.getDocumentoAssinado() == null) {
            return ResponseEntity.notFound().build();
        }

        MediaType tipo = auto.getDocumentoAssinadoContentType() != null
                ? MediaType.parseMediaType(auto.getDocumentoAssinadoContentType())
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + auto.getDocumentoAssinadoNome() + "\"")
                .body(auto.getDocumentoAssinado());
    }

    private ResponseEntity<?> validarAcesso(AutoFiscalizacao auto) {
        if (isGestor()) {
            return null;
        }
        Usuario usuarioLogado = usuarioAutenticado();
        if (usuarioLogado == null || auto.getCriador() == null || !auto.getCriador().getId().equals(usuarioLogado.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Você só pode acessar Autos emitidos por você."));
        }
        return null;
    }

    private boolean isGestor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("GESTOR") || a.getAuthority().equals("ROLE_GESTOR"));
    }

    private Usuario usuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return null;
        }
        return usuarioRepository.findByCpf(auth.getName()).orElse(null);
    }
}
