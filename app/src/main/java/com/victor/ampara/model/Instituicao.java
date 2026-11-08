package com.victor.ampara.model;

import java.io.Serializable;

/**
 * Instituição da Rede de Apoio (entidade instituicao_apoio do diagrama de classes).
 * Os dados são cadastrados pelo administrador no painel web (coleção "instituicoes").
 */
public class Instituicao implements Serializable {

    private String id;
    private String nome;
    private String categoria;
    private String cidade;
    private String uf;
    private String endereco;
    private String telefone;
    private String email;
    private String descricao;
    private boolean ativo = true;

    // Construtor vazio exigido pelo Firestore
    public Instituicao() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    /** Ex.: "Apucarana - PR" */
    public String getCidadeUf() {
        if (cidade == null) return "";
        return (uf == null || uf.isEmpty()) ? cidade : cidade + " - " + uf;
    }
}
