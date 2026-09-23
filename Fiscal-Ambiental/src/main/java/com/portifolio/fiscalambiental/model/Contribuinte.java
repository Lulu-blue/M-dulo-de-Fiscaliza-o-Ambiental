package com.portifolio.fiscalambiental.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "contribuintes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Contribuinte {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "cpf_cnpj", nullable = false, unique = true, length = 18)
    private String cpfCnpj;

    @Column(nullable = false)
    private String rua;

    @Column(nullable = false)
    private String numero;

    @Column(nullable = false)
    private String bairro;

    @Column(nullable = false)
    private String municipio;

    @Column(nullable = false, length = 9)
    private String cep;
}
