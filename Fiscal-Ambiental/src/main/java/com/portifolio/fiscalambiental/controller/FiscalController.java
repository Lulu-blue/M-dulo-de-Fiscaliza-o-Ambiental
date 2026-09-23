package com.portifolio.fiscalambiental.controller;

import com.portifolio.fiscalambiental.model.AutoFiscalizacao;
import com.portifolio.fiscalambiental.model.Demanda;
import com.portifolio.fiscalambiental.model.Relatorio;
import com.portifolio.fiscalambiental.model.Usuario;
import com.portifolio.fiscalambiental.repository.DemandaRepository;
import com.portifolio.fiscalambiental.repository.UsuarioRepository;
import com.portifolio.fiscalambiental.service.AutoFiscalizacaoService;
import com.portifolio.fiscalambiental.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fiscal")
@RequiredArgsConstructor
public class FiscalController {

    private final DemandaRepository demandaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AutoFiscalizacaoService autoFiscalizacaoService;
    private final RelatorioService relatorioService;

    @GetMapping("/demandas")
    public ResponseEntity<List<Demanda>> listarDemandasDoFiscal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            Optional<Usuario> fiscalOpt = usuarioRepository.findByCpf(auth.getName());
            if (fiscalOpt.isPresent()) {
                List<Demanda> demandas = demandaRepository.findByFiscalAtribuidoId(fiscalOpt.get().getId());
                return ResponseEntity.ok(demandas);
            }
        }
        return ResponseEntity.ok(demandaRepository.findAll());
    }

    @PostMapping("/demandas/{demandaId}/finalizar")
    public ResponseEntity<?> finalizarDemanda(@PathVariable UUID demandaId) {
        Usuario fiscalLogado = usuarioAutenticado();
        Demanda demanda = demandaRepository.findById(demandaId).orElse(null);
        if (demanda == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Demanda não encontrada."));
        }

        Usuario fiscalAtribuido = demanda.getFiscalAtribuido();
        if (fiscalLogado == null || fiscalAtribuido == null || !fiscalAtribuido.getId().equals(fiscalLogado.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Esta demanda não está atribuída a você."));
        }

        if ("CONCLUIDO".equals(demanda.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Esta demanda já está concluída."));
        }

        List<AutoFiscalizacao> autos = autoFiscalizacaoService.listarPorDemanda(demandaId);
        if (autos.isEmpty() || autos.get(0).getDocumentoAssinadoNome() == null) {
            return ResponseEntity.badRequest().body(Map.of("message",
                    "É necessário emitir o Auto de Fiscalização e anexar o documento assinado antes de finalizar a demanda."));
        }

        List<Relatorio> relatorios = relatorioService.listarPorDemanda(demandaId);
        if (relatorios.isEmpty() || relatorios.get(0).getDocumentoAssinadoNome() == null) {
            return ResponseEntity.badRequest().body(Map.of("message",
                    "É necessário emitir o Relatório de Vistoria e anexar o documento assinado antes de finalizar a demanda."));
        }

        demanda.setStatus("CONCLUIDO");
        demanda.setDataConclusao(LocalDateTime.now());
        Demanda salva = demandaRepository.save(demanda);
        return ResponseEntity.ok(salva);
    }

    @GetMapping("/minha-pontuacao")
    public ResponseEntity<?> minhaPontuacao() {
        Usuario fiscalLogado = usuarioAutenticado();
        if (fiscalLogado == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Não autenticado."));
        }

        List<PontuacaoDocumento> documentos = new ArrayList<>();

        for (AutoFiscalizacao auto : autoFiscalizacaoService.listarPorCriador(fiscalLogado.getId())) {
            if (auto.getDocumentoAssinadoEnviadoEm() != null) {
                int pontos = auto.getPontosProdutividade() != null ? auto.getPontosProdutividade() : 0;
                documentos.add(new PontuacaoDocumento(
                        "AUTO", auto.getId(), auto.getNumeroSequencial(), auto.getAno(), pontos,
                        auto.getDemanda() != null ? auto.getDemanda().getTitulo() : null,
                        auto.getDocumentoAssinadoEnviadoEm()));
            }
        }

        for (Relatorio relatorio : relatorioService.listarPorCriador(fiscalLogado.getId())) {
            if (relatorio.getDocumentoAssinadoEnviadoEm() != null) {
                int pontos = relatorio.getPontosProdutividadeBase() != null ? relatorio.getPontosProdutividadeBase() : 0;
                documentos.add(new PontuacaoDocumento(
                        "RELATORIO", relatorio.getId(), relatorio.getNumeroSequencial(), relatorio.getAno(), pontos,
                        relatorio.getDemanda() != null ? relatorio.getDemanda().getTitulo() : null,
                        relatorio.getDocumentoAssinadoEnviadoEm()));
            }
        }

        documentos.sort(Comparator.comparing(PontuacaoDocumento::dataAssinatura).reversed());

        Map<LocalDate, Integer> porDiaMap = new TreeMap<>();
        for (PontuacaoDocumento d : documentos) {
            porDiaMap.merge(d.dataAssinatura().toLocalDate(), d.pontos(), Integer::sum);
        }
        List<PontuacaoDiaria> porDia = porDiaMap.entrySet().stream()
                .map(e -> new PontuacaoDiaria(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        int totalPontos = documentos.stream().mapToInt(PontuacaoDocumento::pontos).sum();

        return ResponseEntity.ok(new MinhaPontuacaoResposta(totalPontos, porDia, documentos));
    }

    public record PontuacaoDocumento(
            String tipo, UUID id, Long numeroSequencial, Integer ano, int pontos,
            String demandaTitulo, LocalDateTime dataAssinatura
    ) {}

    public record PontuacaoDiaria(LocalDate dia, int pontos) {}

    public record MinhaPontuacaoResposta(int totalPontos, List<PontuacaoDiaria> porDia, List<PontuacaoDocumento> documentos) {}

    @PostMapping("/autos")
    public ResponseEntity<?> emitirAuto(@RequestBody AutoFiscalizacao auto) {
        Usuario fiscalLogado = usuarioAutenticado();
        if (fiscalLogado != null) {
            auto.setCriador(fiscalLogado);
        }

        ResponseEntity<?> erroPosse = validarPosseDaDemanda(auto.getDemanda(), fiscalLogado);
        if (erroPosse != null) {
            return erroPosse;
        }
        auto.setDemanda(demandaRepository.findById(auto.getDemanda().getId()).orElseThrow());

        try {
            AutoFiscalizacao autoSalvo = autoFiscalizacaoService.gerarAuto(auto);
            return ResponseEntity.ok(autoSalvo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/relatorios")
    public ResponseEntity<?> emitirRelatorio(@RequestBody Relatorio relatorio) {
        Usuario fiscalLogado = usuarioAutenticado();
        if (fiscalLogado != null) {
            relatorio.setCriador(fiscalLogado);
        }

        ResponseEntity<?> erroPosse = validarPosseDaDemanda(relatorio.getDemanda(), fiscalLogado);
        if (erroPosse != null) {
            return erroPosse;
        }
        relatorio.setDemanda(demandaRepository.findById(relatorio.getDemanda().getId()).orElseThrow());

        try {
            Relatorio relatorioSalvo = relatorioService.gerarRelatorio(relatorio);
            return ResponseEntity.ok(relatorioSalvo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/autos/por-demanda/{demandaId}")
    public ResponseEntity<?> listarAutosPorDemanda(@PathVariable UUID demandaId) {
        ResponseEntity<?> erroPosse = validarPosseDaDemandaPorId(demandaId, usuarioAutenticado());
        if (erroPosse != null) {
            return erroPosse;
        }
        return ResponseEntity.ok(autoFiscalizacaoService.listarPorDemanda(demandaId));
    }

    @PostMapping("/autos/{id}/documento-assinado")
    public ResponseEntity<?> anexarDocumentoAssinadoAuto(@PathVariable UUID id, @RequestBody DocumentoAssinadoRequest req) {
        try {
            byte[] conteudo = Base64.getDecoder().decode(req.conteudoBase64());
            AutoFiscalizacao auto = autoFiscalizacaoService.anexarDocumentoAssinado(id, conteudo, req.nomeArquivo(), req.contentType(), usuarioAutenticado());
            return ResponseEntity.ok(auto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/autos/{id}/documento-assinado")
    public ResponseEntity<?> removerDocumentoAssinadoAuto(@PathVariable UUID id) {
        try {
            AutoFiscalizacao auto = autoFiscalizacaoService.removerDocumentoAssinado(id, usuarioAutenticado());
            return ResponseEntity.ok(auto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/autos/{id}")
    public ResponseEntity<?> atualizarAuto(@PathVariable UUID id, @RequestBody AutoFiscalizacao dados) {
        try {
            AutoFiscalizacao autoAtualizado = autoFiscalizacaoService.atualizarAuto(id, dados, usuarioAutenticado());
            return ResponseEntity.ok(autoAtualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/autos/{id}")
    public ResponseEntity<?> excluirAuto(@PathVariable UUID id) {
        try {
            autoFiscalizacaoService.excluirAuto(id, usuarioAutenticado());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/relatorios/por-demanda/{demandaId}")
    public ResponseEntity<?> listarRelatoriosPorDemanda(@PathVariable UUID demandaId) {
        ResponseEntity<?> erroPosse = validarPosseDaDemandaPorId(demandaId, usuarioAutenticado());
        if (erroPosse != null) {
            return erroPosse;
        }
        return ResponseEntity.ok(relatorioService.listarPorDemanda(demandaId));
    }

    @PostMapping("/relatorios/{id}/documento-assinado")
    public ResponseEntity<?> anexarDocumentoAssinadoRelatorio(@PathVariable UUID id, @RequestBody DocumentoAssinadoRequest req) {
        try {
            byte[] conteudo = Base64.getDecoder().decode(req.conteudoBase64());
            Relatorio relatorio = relatorioService.anexarDocumentoAssinado(id, conteudo, req.nomeArquivo(), req.contentType(), usuarioAutenticado());
            return ResponseEntity.ok(relatorio);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/relatorios/{id}/documento-assinado")
    public ResponseEntity<?> removerDocumentoAssinadoRelatorio(@PathVariable UUID id) {
        try {
            Relatorio relatorio = relatorioService.removerDocumentoAssinado(id, usuarioAutenticado());
            return ResponseEntity.ok(relatorio);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/relatorios/{id}")
    public ResponseEntity<?> atualizarRelatorio(@PathVariable UUID id, @RequestBody Relatorio dados) {
        try {
            Relatorio relatorioAtualizado = relatorioService.atualizarRelatorio(id, dados, usuarioAutenticado());
            return ResponseEntity.ok(relatorioAtualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/relatorios/{id}")
    public ResponseEntity<?> excluirRelatorio(@PathVariable UUID id) {
        try {
            relatorioService.excluirRelatorio(id, usuarioAutenticado());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    public record DocumentoAssinadoRequest(String nomeArquivo, String contentType, String conteudoBase64) {}

    private Usuario usuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return null;
        }
        return usuarioRepository.findByCpf(auth.getName()).orElse(null);
    }

    private ResponseEntity<?> validarPosseDaDemanda(Demanda demandaInformada, Usuario fiscalLogado) {
        if (demandaInformada == null || demandaInformada.getId() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Demanda não informada."));
        }
        return validarPosseDaDemandaPorId(demandaInformada.getId(), fiscalLogado);
    }

    private ResponseEntity<?> validarPosseDaDemandaPorId(UUID demandaId, Usuario fiscalLogado) {
        Demanda demanda = demandaRepository.findById(demandaId).orElse(null);
        if (demanda == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Demanda não encontrada: " + demandaId));
        }

        Usuario fiscalAtribuido = demanda.getFiscalAtribuido();
        if (fiscalLogado == null || fiscalAtribuido == null || !fiscalAtribuido.getId().equals(fiscalLogado.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Esta demanda não está atribuída a você."));
        }

        return null;
    }
}
