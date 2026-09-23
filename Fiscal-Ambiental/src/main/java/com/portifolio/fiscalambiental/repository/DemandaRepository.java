package com.portifolio.fiscalambiental.repository;

import com.portifolio.fiscalambiental.model.Demanda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface DemandaRepository extends JpaRepository<Demanda, UUID> {
    List<Demanda> findByFiscalAtribuidoId(UUID fiscalId);
}
