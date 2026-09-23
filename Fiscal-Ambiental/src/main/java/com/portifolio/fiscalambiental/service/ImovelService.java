package com.portifolio.fiscalambiental.service;

import com.portifolio.fiscalambiental.model.Contribuinte;
import com.portifolio.fiscalambiental.model.Imovel;
import com.portifolio.fiscalambiental.repository.ImovelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImovelService {

    private final ImovelRepository imovelRepository;

    // Normaliza sem pontos (ex.: "012.045.0023.01" -> "012045002301") pra busca/duplicidade tratarem igual.
    private static String normalizarInscricao(String inscricao) {
        return inscricao == null ? null : inscricao.replace(".", "").trim();
    }

    // Com id: reaproveitamento explícito (veio da busca) — atualiza o cadastro existente.
    // Só por inscrição digitada: pode ser coincidência, então só completa o contribuinte se estava vazio.
    @Transactional
    public Imovel buscarOuCriar(Imovel dados, Contribuinte contribuinte) {
        if (dados == null) {
            throw new IllegalArgumentException("Dados do imóvel não informados.");
        }

        if (dados.getId() != null) {
            Imovel existente = imovelRepository.findById(dados.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Imóvel não encontrado: " + dados.getId()));
            aplicarEdicoes(existente, dados);
            if (contribuinte != null) {
                existente.setContribuinte(contribuinte);
            }
            return imovelRepository.save(existente);
        }

        String inscricaoNormalizada = normalizarInscricao(dados.getInscricao());
        if (inscricaoNormalizada != null && !inscricaoNormalizada.isBlank()) {
            Imovel existente = imovelRepository.findByInscricaoNormalizada(inscricaoNormalizada).orElse(null);
            if (existente != null) {
                if (existente.getContribuinte() == null && contribuinte != null) {
                    existente.setContribuinte(contribuinte);
                    existente = imovelRepository.save(existente);
                }
                return existente;
            }
        }

        dados.setId(null);
        dados.setInscricaoNormalizada(inscricaoNormalizada);
        dados.setContribuinte(contribuinte);
        return imovelRepository.save(dados);
    }

    private void aplicarEdicoes(Imovel existente, Imovel dados) {
        if (dados.getInscricao() != null && !dados.getInscricao().isBlank()) {
            existente.setInscricao(dados.getInscricao());
            existente.setInscricaoNormalizada(normalizarInscricao(dados.getInscricao()));
        }
        if (dados.getRua() != null && !dados.getRua().isBlank()) existente.setRua(dados.getRua());
        if (dados.getNumero() != null && !dados.getNumero().isBlank()) existente.setNumero(dados.getNumero());
        if (dados.getBairro() != null && !dados.getBairro().isBlank()) existente.setBairro(dados.getBairro());
        if (dados.getCep() != null && !dados.getCep().isBlank()) existente.setCep(dados.getCep());
    }

    public List<Imovel> buscar(String termo) {
        String termoNormalizado = normalizarInscricao(termo);
        if (termoNormalizado == null || termoNormalizado.isBlank()) {
            return List.of();
        }
        return imovelRepository.findByInscricaoNormalizadaContaining(termoNormalizado);
    }

    public List<Imovel> buscarPorContribuinte(UUID contribuinteId) {
        if (contribuinteId == null) {
            return List.of();
        }
        return imovelRepository.findByContribuinteId(contribuinteId);
    }

    @Transactional
    public Imovel vincularContribuinteSeAusente(Imovel imovel, Contribuinte contribuinte) {
        if (imovel.getContribuinte() == null && contribuinte != null) {
            imovel.setContribuinte(contribuinte);
            return imovelRepository.save(imovel);
        }
        return imovel;
    }
}
