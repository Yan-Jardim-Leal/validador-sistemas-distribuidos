package com.newpix.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Classe que representa um usuário no sistema NewPix.
 */
public class Usuario {
    
    @JsonProperty("cpf")
    private String cpf;
    
    @JsonProperty("nome")
    private String nome;
    
    @JsonProperty("senha")
    private String senha;
    
    @JsonProperty("saldo")
    private double saldo;
    
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    
    // Construtores
    public Usuario() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
        this.saldo = 0.0;
    }
    
    public Usuario(String cpf, String nome, String senha) {
        this();
        this.cpf = cpf;
        this.nome = nome;
        this.senha = senha;
    }
    
    // Getters e Setters
    public String getCpf() {
        return cpf;
    }
    
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
        this.atualizadoEm = LocalDateTime.now();
    }
    
    public String getSenha() {
        return senha;
    }
    
    public void setSenha(String senha) {
        this.senha = senha;
        this.atualizadoEm = LocalDateTime.now();
    }
    
    public double getSaldo() {
        return saldo;
    }
    
    public void setSaldo(double saldo) {
        this.saldo = saldo;
        this.atualizadoEm = LocalDateTime.now();
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
    
    /**
     * Adiciona valor ao saldo do usuário.
     */
    public void adicionarSaldo(double valor) {
        if (valor > 0) {
            this.saldo += valor;
            this.atualizadoEm = LocalDateTime.now();
        }
    }
    
    /**
     * Remove valor do saldo do usuário.
     * @return true se a operação foi bem-sucedida, false se saldo insuficiente
     */
    public boolean removerSaldo(double valor) {
        if (valor > 0 && this.saldo >= valor) {
            this.saldo -= valor;
            this.atualizadoEm = LocalDateTime.now();
            return true;
        }
        return false;
    }
    
    /**
     * Verifica se o usuário tem saldo suficiente.
     */
    public boolean temSaldoSuficiente(double valor) {
        return this.saldo >= valor;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(cpf, usuario.cpf);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(cpf);
    }
    
    @Override
    public String toString() {
        return "Usuario{" +
                "cpf='" + cpf + '\'' +
                ", nome='" + nome + '\'' +
                ", saldo=" + saldo +
                '}';
    }
}
