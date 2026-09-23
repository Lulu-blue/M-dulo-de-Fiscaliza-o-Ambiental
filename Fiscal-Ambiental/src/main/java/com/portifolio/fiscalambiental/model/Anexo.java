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
@Table(name = "anexos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Anexo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Não serializado — evita recursão infinita quando a Demanda também embute sua lista de anexos. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_demanda", nullable = false)
    private Demanda demanda;

    @Column(name = "nome_arquivo", nullable = false)
    private String nomeArquivo;

    @Column(name = "hash_sha256", nullable = false)
    private String hashSha256;

    @Column(name = "tamanho_bytes", nullable = false)
    private Long tamanhoBytes;

    @Column(name = "data_upload", nullable = false)
    private LocalDateTime dataUpload = LocalDateTime.now();

    /** Bytes reais do arquivo — nunca serializados no JSON (pesado demais); servidos só via endpoint dedicado. */
    @JsonIgnore
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "conteudo")
    private byte[] conteudo;

    @Column(name = "content_type")
    private String contentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_enviado_por")
    private Usuario enviadoPor;
}
