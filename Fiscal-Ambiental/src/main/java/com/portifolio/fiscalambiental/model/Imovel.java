package com.portifolio.fiscalambiental.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "imoveis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Imovel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String inscricao;

    // Inscrição sem pontos, usada em toda busca/verificação de duplicidade (ver ImovelService.normalizarInscricao)
    @Column(name = "inscricao_normalizada", nullable = false, unique = true)
    private String inscricaoNormalizada;

    @Column(name = "codigo_reduzido", unique = true)
    private String codigoReduzido;

    @Column(nullable = false)
    private String rua;

    @Column(nullable = false)
    private String numero;

    @Column(nullable = false)
    private String bairro;

    @Column(nullable = false, length = 9)
    private String cep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contribuinte")
    private Contribuinte contribuinte;
}
