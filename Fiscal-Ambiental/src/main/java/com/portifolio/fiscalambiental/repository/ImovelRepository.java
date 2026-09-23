package com.portifolio.fiscalambiental.repository;

import com.portifolio.fiscalambiental.model.Imovel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface ImovelRepository extends JpaRepository<Imovel, UUID> {
    Optional<Imovel> findByInscricao(String inscricao);
    Optional<Imovel> findByInscricaoNormalizada(String inscricaoNormalizada);
    Optional<Imovel> findByCodigoReduzido(String codigoReduzido);
    List<Imovel> findByInscricaoContainingIgnoreCase(String inscricao);
    List<Imovel> findByInscricaoNormalizadaContaining(String inscricaoNormalizada);
    List<Imovel> findByContribuinteId(UUID contribuinteId);
}
