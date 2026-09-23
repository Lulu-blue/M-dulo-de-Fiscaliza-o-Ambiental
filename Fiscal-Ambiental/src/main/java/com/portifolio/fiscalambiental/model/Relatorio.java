package com.portifolio.fiscalambiental.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "relatorios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Relatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_sequencial", nullable = false)
    private Long numeroSequencial;

    @Column(nullable = false)
    private Integer ano;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_demanda", nullable = false)
    private Demanda demanda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_imovel", nullable = false)
    private Imovel imovel;

    @Column(name = "data_hora_vistoria", nullable = false)
    private LocalDateTime dataHoraVistoria;

    private String assunto;

    private String atendimento;

    @Column(name = "processo_administrativo")
    private String processoAdministrativo;

    /** Corpo do relatório: "...verificamos que houve {textoVistoria}." */
    @Column(name = "texto_vistoria", columnDefinition = "TEXT")
    private String textoVistoria;

    @Column(name = "pontos_produtividade_base")
    private Integer pontosProdutividadeBase;

    @Column(name = "data_geracao", nullable = false)
    private LocalDateTime dataGeracao = LocalDateTime.now();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "relatorio_fiscais",
        joinColumns = @JoinColumn(name = "id_relatorio"),
        inverseJoinColumns = @JoinColumn(name = "id_fiscal")
    )
    private List<Usuario> fiscais;

    /** Fiscal que emitiu o relatório — usado para validar quem pode anexar/remover o documento assinado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_criador")
    private Usuario criador;

    /** Documento impresso, assinado e digitalizado — nunca serializado (pesado); servido via endpoint dedicado. */
    @JsonIgnore
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "documento_assinado")
    private byte[] documentoAssinado;

    @Column(name = "documento_assinado_nome")
    private String documentoAssinadoNome;

    @Column(name = "documento_assinado_content_type")
    private String documentoAssinadoContentType;

    /** Momento do anexo — define a janela de 24h em que o fiscal ainda pode remover o documento. */
    @Column(name = "documento_assinado_enviado_em")
    private LocalDateTime documentoAssinadoEnviadoEm;

    @OneToMany(mappedBy = "relatorio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("ordem ASC")
    private List<RelatorioImagem> imagens = new ArrayList<>();
}
