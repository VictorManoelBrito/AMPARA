package com.victor.ampara.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Denuncia {
    private String id;
    private String tipo;
    private String dataHoraOcorrencia;
    private String local;
    private String relato;
    private boolean anonimo;
    private String evidenciaUrl;
    private String usuarioId;
    private String status = "Em análise";
    
    @ServerTimestamp
    private Date dataCriacao;

    public Denuncia() {
        // Necessário para o Firebase
    }

    public Denuncia(String tipo, String dataHoraOcorrencia, String local, String relato, boolean anonimo, String usuarioId) {
        this.tipo = tipo;
        this.dataHoraOcorrencia = dataHoraOcorrencia;
        this.local = local;
        this.relato = relato;
        this.anonimo = anonimo;
        this.usuarioId = usuarioId;
        this.status = "Em análise";
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDataHoraOcorrencia() { return dataHoraOcorrencia; }
    public void setDataHoraOcorrencia(String dataHoraOcorrencia) { this.dataHoraOcorrencia = dataHoraOcorrencia; }

    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }

    public String getRelato() { return relato; }
    public void setRelato(String relato) { this.relato = relato; }

    public boolean isAnonimo() { return anonimo; }
    public void setAnonimo(boolean anonimo) { this.anonimo = anonimo; }

    public String getEvidenciaUrl() { return evidenciaUrl; }
    public void setEvidenciaUrl(String evidenciaUrl) { this.evidenciaUrl = evidenciaUrl; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(Date dataCriacao) { this.dataCriacao = dataCriacao; }
}
