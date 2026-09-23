package com.portifolio.fiscalambiental.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "relatorio_imagens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RelatorioImagem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_relatorio", nullable = false)
    private Relatorio relatorio;

    /** Foto da vistoria já em data URI ("data:image/...;base64,...") — pequena o bastante para ir direto no JSON. */
    @Column(name = "imagem_base64", columnDefinition = "TEXT", nullable = false)
    private String imagemBase64;

    @Column(columnDefinition = "TEXT")
    private String legenda;

    @Column(nullable = false)
    private Integer ordem = 0;
}
