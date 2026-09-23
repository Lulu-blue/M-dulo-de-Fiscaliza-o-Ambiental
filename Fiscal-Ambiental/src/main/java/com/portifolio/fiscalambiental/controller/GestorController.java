package com.portifolio.fiscalambiental.controller;

import com.portifolio.fiscalambiental.model.AutoFiscalizacao;
import com.portifolio.fiscalambiental.model.Demanda;
import com.portifolio.fiscalambiental.model.Relatorio;
import com.portifolio.fiscalambiental.model.Usuario;
import com.portifolio.fiscalambiental.repository.AnexoRepository;
import com.portifolio.fiscalambiental.repository.AutoFiscalizacaoRepository;
import com.portifolio.fiscalambiental.repository.DemandaRepository;
import com.portifolio.fiscalambiental.repository.RelatorioRepository;
import com.portifolio.fiscalambiental.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gestor")
@RequiredArgsConstructor
public class GestorController {

    private final DemandaRepository demandaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AutoFiscalizacaoRepository autoFiscalizacaoRepository;
    private final RelatorioRepository relatorioRepository;
    private final AnexoRepository anexoRepository;

    @GetMapping("/demandas")
    public ResponseEntity<List<Demanda>> listarDemandas() {
        List<Demanda> demandas = demandaRepository.findAll();
        demandas.forEach(d -> {
            marcarPodeEditar(d);
            d.setAnexos(anexoRepository.findByDemandaId(d.getId()));
        });
        return ResponseEntity.ok(demandas);
    }

    @PostMapping("/demandas")
    public ResponseEntity<Demanda> criarDemanda(@RequestBody Demanda demanda) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            usuarioRepository.findByCpf(auth.getName()).ifPresent(demanda::setGestorCriador);
        }
        demanda.setStatus("PENDENTE");
        demanda.setDataCriacao(LocalDateTime.now());
        Demanda salva = demandaRepository.save(demanda);
        marcarPodeEditar(salva);
        return ResponseEntity.ok(salva);
    }

    private void marcarPodeEditar(Demanda demanda) {
        boolean semAuto = !autoFiscalizacaoRepository.existsByDemandaId(demanda.getId());
        boolean semRelatorio = !relatorioRepository.existsByDemandaId(demanda.getId());
        demanda.setPodeEditar(semAuto && semRelatorio);
    }

    @PutMapping("/demandas/{demandaId}")
    public ResponseEntity<?> editarDemanda(@PathVariable UUID demandaId, @RequestBody Demanda dadosEditados) {
        Demanda demanda = demandaRepository.findById(demandaId)
                .orElseThrow(() -> new RuntimeException("Demanda não encontrada"));

        boolean jaTemAuto = autoFiscalizacaoRepository.existsByDemandaId(demandaId);
        boolean jaTemRelatorio = relatorioRepository.existsByDemandaId(demandaId);
        if (jaTemAuto || jaTemRelatorio) {
            return ResponseEntity.badRequest().body("Não é possível editar: o fiscal já gerou " +
                    (jaTemAuto ? "um Auto de Fiscalização" : "um Relatório") + " para esta demanda.");
        }

        if (dadosEditados.getTitulo() == null || dadosEditados.getTitulo().isBlank()) {
            return ResponseEntity.badRequest().body("O título é obrigatório.");
        }
        if (dadosEditados.getDescricao() == null || dadosEditados.getDescricao().isBlank()) {
            return ResponseEntity.badRequest().body("A descrição é obrigatória.");
        }

        demanda.setTitulo(dadosEditados.getTitulo());
        demanda.setDescricao(dadosEditados.getDescricao());
        demanda.setLocalizacao(dadosEditados.getLocalizacao());
        demanda.setUrgencia(dadosEditados.getUrgencia());
        Demanda salva = demandaRepository.save(demanda);
        marcarPodeEditar(salva);
        return ResponseEntity.ok(salva);
    }

    @DeleteMapping("/demandas/{demandaId}")
    public ResponseEntity<?> excluirDemanda(@PathVariable UUID demandaId) {
        Demanda demanda = demandaRepository.findById(demandaId)
                .orElseThrow(() -> new RuntimeException("Demanda não encontrada"));

        boolean jaTemAuto = autoFiscalizacaoRepository.existsByDemandaId(demandaId);
        boolean jaTemRelatorio = relatorioRepository.existsByDemandaId(demandaId);
        if (jaTemAuto || jaTemRelatorio) {
            return ResponseEntity.badRequest().body("Não é possível excluir: o fiscal já gerou " +
                    (jaTemAuto ? "um Auto de Fiscalização" : "um Relatório") + " para esta demanda.");
        }

        if (!"PENDENTE".equals(demanda.getStatus())) {
            return ResponseEntity.badRequest().body("Só é possível excluir demandas com status PENDENTE. Esta demanda já está " + demanda.getStatus() + ".");
        }

        anexoRepository.deleteAll(anexoRepository.findByDemandaId(demandaId));
        demandaRepository.delete(demanda);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/demandas/{demandaId}/delegar/{fiscalId}")
    public ResponseEntity<?> delegarDemanda(@PathVariable UUID demandaId, @PathVariable UUID fiscalId) {
        Demanda demanda = demandaRepository.findById(demandaId)
                .orElseThrow(() -> new RuntimeException("Demanda não encontrada"));
        Usuario fiscal = usuarioRepository.findById(fiscalId)
                .orElseThrow(() -> new RuntimeException("Fiscal não encontrado"));

        if (!"PENDENTE".equals(demanda.getStatus())) {
            return ResponseEntity.badRequest().body("Só é possível delegar demandas com status PENDENTE. Esta demanda já está " + demanda.getStatus() + ".");
        }
        if (!"FISCAL".equalsIgnoreCase(fiscal.getCargo())) {
            return ResponseEntity.badRequest().body("Só é possível delegar demandas a usuários com cargo FISCAL.");
        }
        if (Boolean.FALSE.equals(fiscal.getAtivo())) {
            return ResponseEntity.badRequest().body("O fiscal informado está inativo.");
        }

        demanda.setFiscalAtribuido(fiscal);
        demanda.setStatus("EM_ANDAMENTO");
        Demanda salva = demandaRepository.save(demanda);
        marcarPodeEditar(salva);
        return ResponseEntity.ok(salva);
    }

    @GetMapping("/fiscais")
    public ResponseEntity<List<Usuario>> listarFiscais() {
        return ResponseEntity.ok(usuarioRepository.findByCargo("FISCAL"));
    }

    @GetMapping("/ranking-fiscais")
    public ResponseEntity<List<RankingFiscal>> rankingFiscais() {
        List<Usuario> fiscais = usuarioRepository.findByCargo("FISCAL");

        List<RankingFiscal> ranking = new ArrayList<>();
        for (Usuario fiscal : fiscais) {
            List<DocumentoPontuacao> documentos = new ArrayList<>();

            for (AutoFiscalizacao auto : autoFiscalizacaoRepository.findByCriadorId(fiscal.getId())) {
                int pontos = auto.getPontosProdutividade() != null ? auto.getPontosProdutividade() : 0;
                documentos.add(new DocumentoPontuacao(
                        "AUTO", auto.getId(), auto.getNumeroSequencial(), auto.getAno(),
                        pontos, auto.getDemanda() != null ? auto.getDemanda().getTitulo() : null,
                        auto.getDataGeracao()));
            }

            for (Relatorio relatorio : relatorioRepository.findByCriadorId(fiscal.getId())) {
                int pontos = relatorio.getPontosProdutividadeBase() != null ? relatorio.getPontosProdutividadeBase() : 0;
                documentos.add(new DocumentoPontuacao(
                        "RELATORIO", relatorio.getId(), relatorio.getNumeroSequencial(), relatorio.getAno(),
                        pontos, relatorio.getDemanda() != null ? relatorio.getDemanda().getTitulo() : null,
                        relatorio.getDataGeracao()));
            }

            documentos.sort(Comparator.comparing(DocumentoPontuacao::dataGeracao).reversed());
            int totalPontos = documentos.stream().mapToInt(DocumentoPontuacao::pontos).sum();
            ranking.add(new RankingFiscal(fiscal, totalPontos, documentos));
        }

        ranking.sort(Comparator.comparingInt(RankingFiscal::totalPontos).reversed());
        return ResponseEntity.ok(ranking);
    }

    public record DocumentoPontuacao(
            String tipo,
            UUID id,
            Long numeroSequencial,
            Integer ano,
            int pontos,
            String demandaTitulo,
            LocalDateTime dataGeracao
    ) {}

    public record RankingFiscal(Usuario fiscal, int totalPontos, List<DocumentoPontuacao> documentos) {}
}
