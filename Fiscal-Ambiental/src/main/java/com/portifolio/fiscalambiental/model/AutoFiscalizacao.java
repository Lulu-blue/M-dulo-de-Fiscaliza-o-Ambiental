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
import java.util.UUID;

@Entity
@Table(name = "autos_fiscalizacao")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AutoFiscalizacao {

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
    @JoinColumn(name = "id_criador", nullable = false)
    private Usuario criador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contribuinte", nullable = false)
    private Contribuinte contribuinte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_imovel", nullable = false)
    private Imovel imovel;

    @Column(name = "processo_administrativo")
    private String processoAdministrativo;

    @Column(name = "irregularidades_constatadas", columnDefinition = "TEXT")
    private String irregularidadesConstatadas;

    @Column(name = "prazo_defesa")
    private Integer prazoDefesa;

    @Column(name = "dados_vistoria", columnDefinition = "TEXT")
    private String dadosVistoria;

    @Column(name = "pontos_produtividade")
    private Integer pontosProdutividade;

    @Column(name = "data_geracao", nullable = false)
    private LocalDateTime dataGeracao = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String providencias;

    @Column(name = "dispositivos_legais", columnDefinition = "TEXT")
    private String dispositivosLegais;

    @Column(columnDefinition = "TEXT")
    private String penalidades;

    @Column(name = "tem_testemunhas", nullable = false)
    private Boolean temTestemunhas = false;

    @Column(name = "nome_testemunha_1")
    private String nomeTestemunha1;

    @Column(name = "cpf_testemunha_1")
    private String cpfTestemunha1;

    @Column(name = "nome_testemunha_2")
    private String nomeTestemunha2;

    @Column(name = "cpf_testemunha_2")
    private String cpfTestemunha2;

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
}
