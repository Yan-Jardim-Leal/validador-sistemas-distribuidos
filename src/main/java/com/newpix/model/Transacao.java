package com.newpix.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Classe que representa uma transação no sistema NewPix.
 */
public class Transacao {
    
    @JsonProperty("id")
    private int id;
    
    @JsonProperty("valor_enviado")
    private double valorEnviado;
    
    @JsonProperty("usuario_enviador")
    private Usuario usuarioEnviador;
    
    @JsonProperty("usuario_recebedor")
    private Usuario usuarioRecebedor;
    
    @JsonProperty("criado_em")
    private LocalDateTime criadoEm;
    
    @JsonProperty("atualizado_em")
    private LocalDateTime atualizadoEm;
    
    // Construtores
    public Transacao() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }
    
    public Transacao(double valorEnviado, Usuario usuarioEnviador, Usuario usuarioRecebedor) {
        this();
        this.valorEnviado = valorEnviado;
        this.usuarioEnviador = usuarioEnviador;
        this.usuarioRecebedor = usuarioRecebedor;
    }
    
    // Getters e Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public double getValorEnviado() {
        return valorEnviado;
    }
    
    public void setValorEnviado(double valorEnviado) {
        this.valorEnviado = valorEnviado;
    }
    
    public Usuario getUsuarioEnviador() {
        return usuarioEnviador;
    }
    
    public void setUsuarioEnviador(Usuario usuarioEnviador) {
        this.usuarioEnviador = usuarioEnviador;
    }
    
    public Usuario getUsuarioRecebedor() {
        return usuarioRecebedor;
    }
    
    public void setUsuarioRecebedor(Usuario usuarioRecebedor) {
        this.usuarioRecebedor = usuarioRecebedor;
    }
    
    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
    
    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
    
    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
    
    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transacao transacao = (Transacao) o;
        return id == transacao.id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Transacao{" +
                "id=" + id +
                ", valorEnviado=" + valorEnviado +
                ", usuarioEnviador=" + usuarioEnviador.getCpf() +
                ", usuarioRecebedor=" + usuarioRecebedor.getCpf() +
                ", criadoEm=" + criadoEm +
                '}';
    }
}
