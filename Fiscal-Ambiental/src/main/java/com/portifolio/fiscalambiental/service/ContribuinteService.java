package com.portifolio.fiscalambiental.service;

import com.portifolio.fiscalambiental.model.Contribuinte;
import com.portifolio.fiscalambiental.repository.ContribuinteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContribuinteService {

    private final ContribuinteRepository contribuinteRepository;

    // Reaproveita por ID ou CPF/CNPJ e atualiza nome/endereço no cadastro existente (CPF/CNPJ em si nunca muda).
    @Transactional
    public Contribuinte buscarOuCriar(Contribuinte dados) {
        if (dados == null) {
            throw new IllegalArgumentException("Dados do contribuinte não informados.");
        }

        if (dados.getId() != null) {
            Contribuinte existente = contribuinteRepository.findById(dados.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Contribuinte não encontrado: " + dados.getId()));
            aplicarEdicoes(existente, dados);
            return contribuinteRepository.save(existente);
        }

        if (dados.getCpfCnpj() != null && !dados.getCpfCnpj().isBlank()) {
            var existente = contribuinteRepository.findByCpfCnpj(dados.getCpfCnpj());
            if (existente.isPresent()) {
                Contribuinte atualizado = existente.get();
                aplicarEdicoes(atualizado, dados);
                return contribuinteRepository.save(atualizado);
            }
        }

        if (dados.getNome() == null || dados.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome do contribuinte é obrigatório para cadastrar um novo contribuinte.");
        }
        if (dados.getCpfCnpj() == null || dados.getCpfCnpj().isBlank()) {
            throw new IllegalArgumentException("CPF/CNPJ do contribuinte é obrigatório.");
        }

        dados.setId(null);
        return contribuinteRepository.save(dados);
    }

    private void aplicarEdicoes(Contribuinte existente, Contribuinte dados) {
        if (dados.getNome() != null && !dados.getNome().isBlank()) existente.setNome(dados.getNome());
        if (dados.getRua() != null && !dados.getRua().isBlank()) existente.setRua(dados.getRua());
        if (dados.getNumero() != null && !dados.getNumero().isBlank()) existente.setNumero(dados.getNumero());
        if (dados.getBairro() != null && !dados.getBairro().isBlank()) existente.setBairro(dados.getBairro());
        if (dados.getMunicipio() != null && !dados.getMunicipio().isBlank()) existente.setMunicipio(dados.getMunicipio());
        if (dados.getCep() != null && !dados.getCep().isBlank()) existente.setCep(dados.getCep());
    }

    public List<Contribuinte> buscar(String termo) {
        if (termo == null || termo.isBlank()) {
            return List.of();
        }
        return contribuinteRepository.findByNomeContainingIgnoreCaseOrCpfCnpjContaining(termo, termo);
    }
}
