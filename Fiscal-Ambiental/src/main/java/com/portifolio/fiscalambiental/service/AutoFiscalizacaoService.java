package com.portifolio.fiscalambiental.service;

import com.portifolio.fiscalambiental.model.AutoFiscalizacao;
import com.portifolio.fiscalambiental.model.Contribuinte;
import com.portifolio.fiscalambiental.model.Demanda;
import com.portifolio.fiscalambiental.model.Imovel;
import com.portifolio.fiscalambiental.model.Relatorio;
import com.portifolio.fiscalambiental.model.Usuario;
import com.portifolio.fiscalambiental.repository.AutoFiscalizacaoRepository;
import com.portifolio.fiscalambiental.repository.RelatorioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AutoFiscalizacaoService {

    private final AutoFiscalizacaoRepository autoFiscalizacaoRepository;
    private final RelatorioRepository relatorioRepository;
    private final NumeracaoService numeracaoService;
    private final ContribuinteService contribuinteService;
    private final ImovelService imovelService;

    public static final String TIPO_DOCUMENTO = "AUTO_FISCALIZACAO";
    public static final int PONTOS_PRODUTIVIDADE = 25;

    @Transactional
    public AutoFiscalizacao gerarAuto(AutoFiscalizacao auto) {
        validarDemandaNaoConcluida(auto.getDemanda());

        UUID demandaId = auto.getDemanda().getId();
        if (autoFiscalizacaoRepository.existsByDemandaId(demandaId)) {
            throw new IllegalArgumentException("Esta demanda já possui um Auto de Fiscalização emitido. Edite o Auto existente em vez de criar outro.");
        }

        Contribuinte contribuinte = contribuinteService.buscarOuCriar(auto.getContribuinte());
        Imovel imovel = resolverImovelCompartilhado(demandaId, auto.getImovel(), contribuinte);
        auto.setContribuinte(contribuinte);
        auto.setImovel(imovel);

        int anoAtual = LocalDate.now().getYear();
        Long proximoNumero = numeracaoService.gerarProximoNumero(TIPO_DOCUMENTO, anoAtual);

        auto.setNumeroSequencial(proximoNumero);
        auto.setAno(anoAtual);
        auto.setDataGeracao(LocalDateTime.now());
        auto.setPontosProdutividade(0);

        return autoFiscalizacaoRepository.save(auto);
    }

    public List<AutoFiscalizacao> listarTodos() {
        return autoFiscalizacaoRepository.findAll();
    }

    public List<AutoFiscalizacao> listarPorDemanda(UUID demandaId) {
        return autoFiscalizacaoRepository.findByDemandaId(demandaId);
    }

    public List<AutoFiscalizacao> listarPorCriador(UUID criadorId) {
        return autoFiscalizacaoRepository.findByCriadorId(criadorId);
    }

    public AutoFiscalizacao buscarPorId(UUID id) {
        return autoFiscalizacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Auto de Fiscalização não encontrado com ID: " + id));
    }

    @Transactional
    public AutoFiscalizacao anexarDocumentoAssinado(UUID autoId, byte[] conteudo, String nomeArquivo, String contentType, Usuario fiscalLogado) {
        AutoFiscalizacao auto = buscarPorId(autoId);
        validarPosseDoAuto(auto, fiscalLogado);
        validarDemandaNaoConcluida(auto.getDemanda());

        auto.setDocumentoAssinado(conteudo);
        auto.setDocumentoAssinadoNome(nomeArquivo);
        auto.setDocumentoAssinadoContentType(contentType);
        auto.setDocumentoAssinadoEnviadoEm(LocalDateTime.now());
        auto.setPontosProdutividade(PONTOS_PRODUTIVIDADE);

        return autoFiscalizacaoRepository.save(auto);
    }

    @Transactional
    public AutoFiscalizacao removerDocumentoAssinado(UUID autoId, Usuario fiscalLogado) {
        AutoFiscalizacao auto = buscarPorId(autoId);
        validarPosseDoAuto(auto, fiscalLogado);
        validarDemandaNaoConcluida(auto.getDemanda());

        if (auto.getDocumentoAssinadoEnviadoEm() == null) {
            throw new IllegalArgumentException("Nenhum documento assinado anexado a este Auto.");
        }
        if (auto.getDocumentoAssinadoEnviadoEm().plusHours(24).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("O prazo de 24 horas para remover o documento assinado já expirou.");
        }

        auto.setDocumentoAssinado(null);
        auto.setDocumentoAssinadoNome(null);
        auto.setDocumentoAssinadoContentType(null);
        auto.setDocumentoAssinadoEnviadoEm(null);
        auto.setPontosProdutividade(0);

        return autoFiscalizacaoRepository.save(auto);
    }

    // Auto e Relatório da mesma demanda apontam pro mesmo Imóvel.
    private Imovel resolverImovelCompartilhado(UUID demandaId, Imovel imovelInformado, Contribuinte contribuinte) {
        List<Relatorio> relatoriosDaDemanda = relatorioRepository.findByDemandaId(demandaId);
        if (!relatoriosDaDemanda.isEmpty()) {
            return imovelService.vincularContribuinteSeAusente(relatoriosDaDemanda.get(0).getImovel(), contribuinte);
        }
        return imovelService.buscarOuCriar(imovelInformado, contribuinte);
    }

    @Transactional
    public AutoFiscalizacao atualizarAuto(UUID autoId, AutoFiscalizacao dados, Usuario fiscalLogado) {
        AutoFiscalizacao auto = buscarPorId(autoId);
        validarPosseDoAuto(auto, fiscalLogado);
        validarDemandaNaoConcluida(auto.getDemanda());

        if (auto.getDocumentoAssinado() != null) {
            throw new IllegalArgumentException("Este Auto já foi assinado e não pode mais ser editado. Remova o documento assinado primeiro, se precisar corrigir algo.");
        }

        Contribuinte contribuinte = contribuinteService.buscarOuCriar(dados.getContribuinte());
        Imovel imovel = resolverImovelCompartilhado(auto.getDemanda().getId(), dados.getImovel(), contribuinte);

        auto.setContribuinte(contribuinte);
        auto.setImovel(imovel);
        auto.setProcessoAdministrativo(dados.getProcessoAdministrativo());
        auto.setIrregularidadesConstatadas(dados.getIrregularidadesConstatadas());
        auto.setPrazoDefesa(dados.getPrazoDefesa());
        auto.setDadosVistoria(dados.getDadosVistoria());
        auto.setProvidencias(dados.getProvidencias());
        auto.setDispositivosLegais(dados.getDispositivosLegais());
        auto.setPenalidades(dados.getPenalidades());
        auto.setTemTestemunhas(dados.getTemTestemunhas());
        auto.setNomeTestemunha1(dados.getNomeTestemunha1());
        auto.setCpfTestemunha1(dados.getCpfTestemunha1());
        auto.setNomeTestemunha2(dados.getNomeTestemunha2());
        auto.setCpfTestemunha2(dados.getCpfTestemunha2());

        return autoFiscalizacaoRepository.save(auto);
    }

    @Transactional
    public void excluirAuto(UUID autoId, Usuario fiscalLogado) {
        AutoFiscalizacao auto = buscarPorId(autoId);
        validarPosseDoAuto(auto, fiscalLogado);
        validarDemandaNaoConcluida(auto.getDemanda());

        if (auto.getDocumentoAssinado() != null) {
            throw new IllegalArgumentException("Remova o documento assinado antes de excluir este Auto.");
        }

        autoFiscalizacaoRepository.delete(auto);
        numeracaoService.descartarNumero(TIPO_DOCUMENTO, auto.getAno(), auto.getNumeroSequencial());
    }

    private void validarPosseDoAuto(AutoFiscalizacao auto, Usuario fiscalLogado) {
        if (fiscalLogado == null || auto.getCriador() == null || !auto.getCriador().getId().equals(fiscalLogado.getId())) {
            throw new IllegalArgumentException("Você só pode gerenciar Autos emitidos por você.");
        }
    }

    private void validarDemandaNaoConcluida(Demanda demanda) {
        if (demanda != null && "CONCLUIDO".equals(demanda.getStatus())) {
            throw new IllegalArgumentException("Esta demanda já foi concluída e não pode mais ser alterada.");
        }
    }
}
