package com.portifolio.fiscalambiental.repository;

import com.portifolio.fiscalambiental.model.RelatorioImagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface RelatorioImagemRepository extends JpaRepository<RelatorioImagem, UUID> {
    List<RelatorioImagem> findByRelatorioId(UUID relatorioId);
}
