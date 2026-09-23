package com.portifolio.fiscalambiental.service;

import com.portifolio.fiscalambiental.model.AutoFiscalizacao;
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
public class RelatorioService {

    private final RelatorioRepository relatorioRepository;
    private final AutoFiscalizacaoRepository autoFiscalizacaoRepository;
    private final NumeracaoService numeracaoService;
    private final ImovelService imovelService;

    public static final String TIPO_DOCUMENTO = "RELATORIO";
    public static final int PONTOS_PRODUTIVIDADE_BASE = 60;

    @Transactional
    public Relatorio gerarRelatorio(Relatorio relatorio) {
        validarDemandaNaoConcluida(relatorio.getDemanda());

        UUID demandaId = relatorio.getDemanda().getId();
        if (relatorioRepository.existsByDemandaId(demandaId)) {
            throw new IllegalArgumentException("Esta demanda já possui um Relatório de Vistoria emitido. Edite o Relatório existente em vez de criar outro.");
        }

        Imovel imovel = resolverImovelCompartilhado(demandaId, relatorio.getImovel());
        relatorio.setImovel(imovel);

        int anoAtual = LocalDate.now().getYear();
        Long proximoNumero = numeracaoService.gerarProximoNumero(TIPO_DOCUMENTO, anoAtual);

        relatorio.setNumeroSequencial(proximoNumero);
        relatorio.setAno(anoAtual);
        relatorio.setDataGeracao(LocalDateTime.now());
        relatorio.setPontosProdutividadeBase(0);

        if (relatorio.getImagens() != null) {
            for (int i = 0; i < relatorio.getImagens().size(); i++) {
                relatorio.getImagens().get(i).setRelatorio(relatorio);
                relatorio.getImagens().get(i).setOrdem(i);
            }
        }

        return relatorioRepository.save(relatorio);
    }

    public List<Relatorio> listarTodos() {
        return relatorioRepository.findAll();
    }

    public List<Relatorio> listarPorDemanda(UUID demandaId) {
        return relatorioRepository.findByDemandaId(demandaId);
    }

    public List<Relatorio> listarPorCriador(UUID criadorId) {
        return relatorioRepository.findByCriadorId(criadorId);
    }

    public Relatorio buscarPorId(UUID id) {
        return relatorioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Relatório não encontrado com ID: " + id));
    }

    @Transactional
    public Relatorio anexarDocumentoAssinado(UUID relatorioId, byte[] conteudo, String nomeArquivo, String contentType, Usuario fiscalLogado) {
        Relatorio relatorio = buscarPorId(relatorioId);
        validarPosseDoRelatorio(relatorio, fiscalLogado);
        validarDemandaNaoConcluida(relatorio.getDemanda());

        relatorio.setDocumentoAssinado(conteudo);
        relatorio.setDocumentoAssinadoNome(nomeArquivo);
        relatorio.setDocumentoAssinadoContentType(contentType);
        relatorio.setDocumentoAssinadoEnviadoEm(LocalDateTime.now());
        relatorio.setPontosProdutividadeBase(PONTOS_PRODUTIVIDADE_BASE);

        return relatorioRepository.save(relatorio);
    }

    @Transactional
    public Relatorio removerDocumentoAssinado(UUID relatorioId, Usuario fiscalLogado) {
        Relatorio relatorio = buscarPorId(relatorioId);
        validarPosseDoRelatorio(relatorio, fiscalLogado);
        validarDemandaNaoConcluida(relatorio.getDemanda());

        if (relatorio.getDocumentoAssinadoEnviadoEm() == null) {
            throw new IllegalArgumentException("Nenhum documento assinado anexado a este Relatório.");
        }
        if (relatorio.getDocumentoAssinadoEnviadoEm().plusHours(24).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("O prazo de 24 horas para remover o documento assinado já expirou.");
        }

        relatorio.setDocumentoAssinado(null);
        relatorio.setDocumentoAssinadoNome(null);
        relatorio.setDocumentoAssinadoContentType(null);
        relatorio.setDocumentoAssinadoEnviadoEm(null);
        relatorio.setPontosProdutividadeBase(0);

        return relatorioRepository.save(relatorio);
    }

    private Imovel resolverImovelCompartilhado(UUID demandaId, Imovel imovelInformado) {
        List<AutoFiscalizacao> autosDaDemanda = autoFiscalizacaoRepository.findByDemandaId(demandaId);
        if (!autosDaDemanda.isEmpty()) {
            return autosDaDemanda.get(0).getImovel();
        }
        return imovelService.buscarOuCriar(imovelInformado, null);
    }

    @Transactional
    public Relatorio atualizarRelatorio(UUID relatorioId, Relatorio dados, Usuario fiscalLogado) {
        Relatorio relatorio = buscarPorId(relatorioId);
        validarPosseDoRelatorio(relatorio, fiscalLogado);
        validarDemandaNaoConcluida(relatorio.getDemanda());

        if (relatorio.getDocumentoAssinado() != null) {
            throw new IllegalArgumentException("Este Relatório já foi assinado e não pode mais ser editado. Remova o documento assinado primeiro, se precisar corrigir algo.");
        }

        Imovel imovel = resolverImovelCompartilhado(relatorio.getDemanda().getId(), dados.getImovel());
        relatorio.setImovel(imovel);

        relatorio.setDataHoraVistoria(dados.getDataHoraVistoria());
        relatorio.setAssunto(dados.getAssunto());
        relatorio.setAtendimento(dados.getAtendimento());
        relatorio.setProcessoAdministrativo(dados.getProcessoAdministrativo());
        relatorio.setTextoVistoria(dados.getTextoVistoria());

        relatorio.getImagens().clear();
        if (dados.getImagens() != null) {
            for (int i = 0; i < dados.getImagens().size(); i++) {
                var imagem = dados.getImagens().get(i);
                imagem.setId(null);
                imagem.setRelatorio(relatorio);
                imagem.setOrdem(i);
                relatorio.getImagens().add(imagem);
            }
        }

        return relatorioRepository.save(relatorio);
    }

    @Transactional
    public void excluirRelatorio(UUID relatorioId, Usuario fiscalLogado) {
        Relatorio relatorio = buscarPorId(relatorioId);
        validarPosseDoRelatorio(relatorio, fiscalLogado);
        validarDemandaNaoConcluida(relatorio.getDemanda());

        if (relatorio.getDocumentoAssinado() != null) {
            throw new IllegalArgumentException("Remova o documento assinado antes de excluir este Relatório.");
        }

        relatorioRepository.delete(relatorio);
        numeracaoService.descartarNumero(TIPO_DOCUMENTO, relatorio.getAno(), relatorio.getNumeroSequencial());
    }

    private void validarPosseDoRelatorio(Relatorio relatorio, Usuario fiscalLogado) {
        if (fiscalLogado == null || relatorio.getCriador() == null || !relatorio.getCriador().getId().equals(fiscalLogado.getId())) {
            throw new IllegalArgumentException("Você só pode gerenciar Relatórios emitidos por você.");
        }
    }

    private void validarDemandaNaoConcluida(Demanda demanda) {
        if (demanda != null && "CONCLUIDO".equals(demanda.getStatus())) {
            throw new IllegalArgumentException("Esta demanda já foi concluída e não pode mais ser alterada.");
        }
    }
}
