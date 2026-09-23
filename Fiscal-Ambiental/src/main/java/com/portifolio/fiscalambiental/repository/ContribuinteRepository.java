package com.portifolio.fiscalambiental.repository;

import com.portifolio.fiscalambiental.model.Contribuinte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface ContribuinteRepository extends JpaRepository<Contribuinte, UUID> {
    Optional<Contribuinte> findByCpfCnpj(String cpfCnpj);
    List<Contribuinte> findByNomeContainingIgnoreCaseOrCpfCnpjContaining(String nome, String cpfCnpj);
}
