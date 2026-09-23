package com.portifolio.fiscalambiental.service;

import com.portifolio.fiscalambiental.model.Anexo;
import com.portifolio.fiscalambiental.model.Demanda;
import com.portifolio.fiscalambiental.model.Usuario;
import com.portifolio.fiscalambiental.repository.AnexoRepository;
import com.portifolio.fiscalambiental.repository.DemandaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnexoService {

    private final AnexoRepository anexoRepository;
    private final DemandaRepository demandaRepository;

    @Transactional
    public Anexo registrarAnexo(UUID demandaId, String nomeArquivo, String hashSha256, Long tamanhoBytes,
                                 byte[] conteudo, String contentType, Usuario enviadoPor) {
        if (anexoRepository.existsByHashSha256(hashSha256)) {
            throw new IllegalArgumentException("Arquivo duplicado detectado no sistema pelo hash SHA-256.");
        }

        Demanda demanda = demandaRepository.findById(demandaId)
                .orElseThrow(() -> new IllegalArgumentException("Demanda não encontrada. ID: " + demandaId));

        Anexo anexo = new Anexo();
        anexo.setDemanda(demanda);
        anexo.setNomeArquivo(nomeArquivo);
        anexo.setHashSha256(hashSha256);
        anexo.setTamanhoBytes(tamanhoBytes);
        anexo.setDataUpload(LocalDateTime.now());
        anexo.setConteudo(conteudo);
        anexo.setContentType(contentType);
        anexo.setEnviadoPor(enviadoPor);

        return anexoRepository.save(anexo);
    }

    public List<Anexo> listarPorDemanda(UUID demandaId) {
        return anexoRepository.findByDemandaId(demandaId);
    }

    public boolean verificarSeExisteHash(String hashSha256) {
        return anexoRepository.existsByHashSha256(hashSha256);
    }

    public Anexo buscarPorId(UUID id) {
        return anexoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Anexo não encontrado. ID: " + id));
    }
}
