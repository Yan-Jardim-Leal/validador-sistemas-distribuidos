package com.newpix.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Classe que representa uma sessão de usuário (token) no sistema.
 */
public class Sessao {
    
    private String token;
    private String cpfUsuario;
    private LocalDateTime criadoEm;
    private LocalDateTime expiresEm;
    private boolean ativo;
    
    // Construtores
    public Sessao() {
        this.token = UUID.randomUUID().toString();
        this.criadoEm = LocalDateTime.now();
        this.expiresEm = LocalDateTime.now().plusHours(24); // Token válido por 24 horas
        this.ativo = true;
    }
    
    public Sessao(String cpfUsuario) {
        this();
        this.cpfUsuario = cpfUsuario;
    }
    
    // Getters e Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getCpfUsuario() {
        return cpfUsuario;
    }
    
    public void setCpfUsuario(String cpfUsuario) {
        this.cpfUsuario = cpfUsuario;
    }
    
    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
    
    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
    
    public LocalDateTime getExpiresEm() {
        return expiresEm;
    }
    
    public void setExpiresEm(LocalDateTime expiresEm) {
        this.expiresEm = expiresEm;
    }
    
    public boolean isAtivo() {
        return ativo;
    }
    
    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
    
    /**
     * Verifica se o token está válido (ativo e não expirado).
     */
    public boolean isValido() {
        return ativo && LocalDateTime.now().isBefore(expiresEm);
    }
    
    /**
     * Invalida a sessão (logout).
     */
    public void invalidar() {
        this.ativo = false;
    }
    
    /**
     * Renova a sessão estendendo o tempo de expiração.
     */
    public void renovar() {
        this.expiresEm = LocalDateTime.now().plusHours(24);
    }
    
    @Override
    public String toString() {
        return "Sessao{" +
                "token='" + token + '\'' +
                ", cpfUsuario='" + cpfUsuario + '\'' +
                ", ativo=" + ativo +
                ", expiresEm=" + expiresEm +
                '}';
    }
}
