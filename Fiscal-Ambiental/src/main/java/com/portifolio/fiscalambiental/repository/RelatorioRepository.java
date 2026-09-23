package com.portifolio.fiscalambiental.repository;

import com.portifolio.fiscalambiental.model.Relatorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface RelatorioRepository extends JpaRepository<Relatorio, UUID> {
    boolean existsByDemandaId(UUID demandaId);
    java.util.List<Relatorio> findByFiscaisId(UUID fiscalId);
    java.util.List<Relatorio> findByDemandaId(UUID demandaId);
    java.util.List<Relatorio> findByCriadorId(UUID criadorId);
}
