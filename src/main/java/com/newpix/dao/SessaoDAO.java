package com.newpix.dao;

import com.newpix.model.Sessao;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para operações com sessões (tokens) no banco de dados.
 */
public class SessaoDAO {
    
    private final DatabaseManager dbManager;
    
    public SessaoDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    /**
     * Cria uma nova sessão no banco de dados.
     */
    public boolean criar(Sessao sessao) throws SQLException {
        String sql = "INSERT INTO sessoes (token, cpf_usuario, criado_em, expires_em, ativo) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, sessao.getToken());
            pstmt.setString(2, sessao.getCpfUsuario());
            pstmt.setString(3, sessao.getCriadoEm().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            pstmt.setString(4, sessao.getExpiresEm().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            pstmt.setBoolean(5, sessao.isAtivo());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Busca uma sessão pelo token.
     */
    public Sessao buscarPorToken(String token) throws SQLException {
        String sql = "SELECT * FROM sessoes WHERE token = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, token);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return criarSessaoFromResultSet(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Busca sessão ativa de um usuário.
     */
    public Sessao buscarSessaoAtivaUsuario(String cpfUsuario) throws SQLException {
        String sql = "SELECT * FROM sessoes WHERE cpf_usuario = ? AND ativo = TRUE AND expires_em > ? ORDER BY criado_em DESC LIMIT 1";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cpfUsuario);
            pstmt.setString(2, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return criarSessaoFromResultSet(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Atualiza uma sessão.
     */
    public boolean atualizar(Sessao sessao) throws SQLException {
        String sql = "UPDATE sessoes SET expires_em = ?, ativo = ? WHERE token = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, sessao.getExpiresEm().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            pstmt.setBoolean(2, sessao.isAtivo());
            pstmt.setString(3, sessao.getToken());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Invalida uma sessão (logout).
     */
    public boolean invalidar(String token) throws SQLException {
        String sql = "UPDATE sessoes SET ativo = FALSE WHERE token = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, token);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Invalida todas as sessões de um usuário.
     */
    public boolean invalidarTodasSessoesUsuario(String cpfUsuario) throws SQLException {
        String sql = "UPDATE sessoes SET ativo = FALSE WHERE cpf_usuario = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cpfUsuario);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Verifica se um token é válido (existe, está ativo e não expirou).
     */
    public boolean isTokenValido(String token) throws SQLException {
        Sessao sessao = buscarPorToken(token);
        return sessao != null && sessao.isValido();
    }
    
    /**
     * Obtém o CPF do usuário associado a um token válido.
     */
    public String getCpfUsuarioPorToken(String token) throws SQLException {
        Sessao sessao = buscarPorToken(token);
        if (sessao != null && sessao.isValido()) {
            return sessao.getCpfUsuario();
        }
        return null;
    }
    
    /**
     * Remove sessões expiradas do banco de dados.
     */
    public int limparSessoesExpiradas() throws SQLException {
        String sql = "DELETE FROM sessoes WHERE expires_em < ? OR ativo = FALSE";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return pstmt.executeUpdate();
        }
    }
    
    /**
     * Lista todas as sessões ativas.
     */
    public List<Sessao> listarSessoesAtivas() throws SQLException {
        String sql = "SELECT * FROM sessoes WHERE ativo = TRUE AND expires_em > ? ORDER BY criado_em DESC";
        List<Sessao> sessoes = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    sessoes.add(criarSessaoFromResultSet(rs));
                }
            }
        }
        
        return sessoes;
    }
    
    /**
     * Cria um objeto Sessao a partir de um ResultSet.
     */
    private Sessao criarSessaoFromResultSet(ResultSet rs) throws SQLException {
        Sessao sessao = new Sessao();
        sessao.setToken(rs.getString("token"));
        sessao.setCpfUsuario(rs.getString("cpf_usuario"));
        sessao.setAtivo(rs.getBoolean("ativo"));
        
        String criadoEmStr = rs.getString("criado_em");
        String expiresEmStr = rs.getString("expires_em");
        
        if (criadoEmStr != null) {
            sessao.setCriadoEm(LocalDateTime.parse(criadoEmStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (expiresEmStr != null) {
            sessao.setExpiresEm(LocalDateTime.parse(expiresEmStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        return sessao;
    }
}
