package com.portifolio.fiscalambiental.repository;

import com.portifolio.fiscalambiental.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByMatricula(String matricula);

    Optional<Usuario> findByCpf(String cpf);

    List<Usuario> findByCargo(String cargo);
}
