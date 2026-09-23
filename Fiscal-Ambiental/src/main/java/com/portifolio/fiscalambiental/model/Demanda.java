package com.portifolio.fiscalambiental.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "demandas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Demanda {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false)
    private String status;

    /** Opcional — anotação livre de onde fica a demanda, preenchida pelo Gestor se quiser. */
    private String localizacao;

    /** Opcional — BAIXA / MEDIA / ALTA. Sem valor definido, é tratada como não informada. */
    private String urgencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_gestor_criador", nullable = false)
    private Usuario gestorCriador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fiscal_atribuido")
    private Usuario fiscalAtribuido;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;

    /** Não persistido — calculado na resposta para o front saber se pode oferecer edição (nenhum Auto/Relatório gerado ainda). */
    @Transient
    private Boolean podeEditar;

    /** Não persistido — populado na resposta para o front listar/visualizar os anexos direto na tabela de demandas. */
    @Transient
    private List<Anexo> anexos;
}
