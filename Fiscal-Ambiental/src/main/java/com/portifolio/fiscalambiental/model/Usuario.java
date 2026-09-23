package com.portifolio.fiscalambiental.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail em formato inválido")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Column(name = "senha_hash", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senhaHash;

    @NotBlank(message = "Cargo é obrigatório")
    @Column(nullable = false)
    private String cargo;

    @Column(unique = true, length = 14)
    private String cpf;

    @Column(unique = true, length = 50)
    private String matricula;

    private Boolean ativo = true;
}
