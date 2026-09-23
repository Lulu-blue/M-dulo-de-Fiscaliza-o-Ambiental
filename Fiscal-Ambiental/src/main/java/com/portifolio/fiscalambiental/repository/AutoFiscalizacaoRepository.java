package com.portifolio.fiscalambiental.repository;

import com.portifolio.fiscalambiental.model.AutoFiscalizacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface AutoFiscalizacaoRepository extends JpaRepository<AutoFiscalizacao, UUID> {
    boolean existsByDemandaId(UUID demandaId);
    java.util.List<AutoFiscalizacao> findByCriadorId(UUID criadorId);
    java.util.List<AutoFiscalizacao> findByDemandaId(UUID demandaId);
}
