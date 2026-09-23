package com.portifolio.fiscalambiental.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NumeracaoService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Long gerarProximoNumero(String tipoDocumento, Integer ano) {
        Query query = entityManager.createNativeQuery("SELECT gerar_numero_sequencial(:tipo, :ano)");
        query.setParameter("tipo", tipoDocumento);
        query.setParameter("ano", ano);

        Number resultado = (Number) query.getSingleResult();
        return resultado.longValue();
    }

    @Transactional
    public void descartarNumero(String tipoDocumento, Integer ano, Long numero) {
        Query query = entityManager.createNativeQuery("SELECT descartar_numero_sequencial(:tipo, :ano, :numero)");
        query.setParameter("tipo", tipoDocumento);
        query.setParameter("ano", ano);
        query.setParameter("numero", numero);

        query.getSingleResult();
    }
}
