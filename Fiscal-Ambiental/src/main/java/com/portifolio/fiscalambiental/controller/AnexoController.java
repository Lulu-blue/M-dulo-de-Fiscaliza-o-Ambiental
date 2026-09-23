package com.portifolio.fiscalambiental.controller;

import com.portifolio.fiscalambiental.model.Anexo;
import com.portifolio.fiscalambiental.model.Usuario;
import com.portifolio.fiscalambiental.repository.UsuarioRepository;
import com.portifolio.fiscalambiental.service.AnexoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/anexos")
@RequiredArgsConstructor
public class AnexoController {

    private final AnexoService anexoService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('FISCAL', 'GESTOR')")
    public ResponseEntity<?> registrarAnexo(@RequestBody RegistroAnexoRequest request) {
        try {
            byte[] conteudo = (request.conteudoBase64() != null && !request.conteudoBase64().isBlank())
                    ? Base64.getDecoder().decode(request.conteudoBase64())
                    : null;

            Anexo anexo = anexoService.registrarAnexo(
                    request.demandaId(),
                    request.nomeArquivo(),
                    request.hashSha256(),
                    request.tamanhoBytes(),
                    conteudo,
                    request.contentType(),
                    usuarioAutenticado()
            );
            return ResponseEntity.ok(anexo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/demanda/{demandaId}")
    public ResponseEntity<List<Anexo>> listarPorDemanda(@PathVariable UUID demandaId) {
        return ResponseEntity.ok(anexoService.listarPorDemanda(demandaId));
    }

    @GetMapping("/verificar-hash/{hash}")
    public ResponseEntity<Map<String, Boolean>> verificarHash(@PathVariable String hash) {
        return ResponseEntity.ok(Map.of("existe", anexoService.verificarSeExisteHash(hash)));
    }

    @GetMapping("/{id}/arquivo")
    public ResponseEntity<byte[]> baixarArquivo(@PathVariable UUID id) {
        Anexo anexo = anexoService.buscarPorId(id);
        if (anexo.getConteudo() == null) {
            return ResponseEntity.notFound().build();
        }

        MediaType tipo = anexo.getContentType() != null
                ? MediaType.parseMediaType(anexo.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + anexo.getNomeArquivo() + "\"")
                .body(anexo.getConteudo());
    }

    private Usuario usuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return null;
        }
        return usuarioRepository.findByCpf(auth.getName()).orElse(null);
    }

    public record RegistroAnexoRequest(
            UUID demandaId,
            String nomeArquivo,
            String hashSha256,
            Long tamanhoBytes,
            String contentType,
            String conteudoBase64
    ) {}
}
