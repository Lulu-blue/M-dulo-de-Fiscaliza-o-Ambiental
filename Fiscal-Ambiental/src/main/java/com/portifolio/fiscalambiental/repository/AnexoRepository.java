package com.portifolio.fiscalambiental.repository;

import com.portifolio.fiscalambiental.model.Anexo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface AnexoRepository extends JpaRepository<Anexo, UUID> {
    List<Anexo> findByDemandaId(UUID demandaId);
    boolean existsByHashSha256(String hashSha256);
}
