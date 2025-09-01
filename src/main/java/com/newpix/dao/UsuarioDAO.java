package com.newpix.dao;

import com.newpix.model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para operações com usuários no banco de dados.
 */
public class UsuarioDAO {
    
    private final DatabaseManager dbManager;
    
    public UsuarioDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    /**
     * Cria um novo usuário no banco de dados.
     */
    public boolean criar(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios (cpf, nome, senha, saldo, criado_em, atualizado_em) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, usuario.getCpf());
            pstmt.setString(2, usuario.getNome());
            pstmt.setString(3, BCrypt.hashpw(usuario.getSenha(), BCrypt.gensalt())); // Hash da senha
            pstmt.setDouble(4, usuario.getSaldo());
            pstmt.setString(5, usuario.getCriadoEm().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            pstmt.setString(6, usuario.getAtualizadoEm().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Busca um usuário pelo CPF.
     */
    public Usuario buscarPorCpf(String cpf) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE cpf = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cpf);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return criarUsuarioFromResultSet(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Atualiza os dados de um usuário.
     */
    public boolean atualizar(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuarios SET nome = ?, senha = ?, saldo = ?, atualizado_em = ? WHERE cpf = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, usuario.getNome());
            pstmt.setString(2, usuario.getSenha().startsWith("$2a$") ? usuario.getSenha() : 
                            BCrypt.hashpw(usuario.getSenha(), BCrypt.gensalt())); // Hash apenas se não for já hasheada
            pstmt.setDouble(3, usuario.getSaldo());
            pstmt.setString(4, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            pstmt.setString(5, usuario.getCpf());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Deleta um usuário do banco de dados.
     */
    public boolean deletar(String cpf) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE cpf = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cpf);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Lista todos os usuários.
     */
    public List<Usuario> listarTodos() throws SQLException {
        String sql = "SELECT * FROM usuarios";
        List<Usuario> usuarios = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                usuarios.add(criarUsuarioFromResultSet(rs));
            }
        }
        
        return usuarios;
    }
    
    /**
     * Verifica se um usuário existe.
     */
    public boolean existe(String cpf) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE cpf = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cpf);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    
    /**
     * Autentica um usuário verificando CPF e senha.
     */
    public boolean autenticar(String cpf, String senha) throws SQLException {
        Usuario usuario = buscarPorCpf(cpf);
        if (usuario != null) {
            return BCrypt.checkpw(senha, usuario.getSenha());
        }
        return false;
    }
    
    /**
     * Atualiza apenas o saldo de um usuário (operação atômica).
     */
    public boolean atualizarSaldo(String cpf, double novoSaldo) throws SQLException {
        String sql = "UPDATE usuarios SET saldo = ?, atualizado_em = ? WHERE cpf = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, novoSaldo);
            pstmt.setString(2, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            pstmt.setString(3, cpf);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Cria um objeto Usuario a partir de um ResultSet.
     */
    private Usuario criarUsuarioFromResultSet(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setCpf(rs.getString("cpf"));
        usuario.setNome(rs.getString("nome"));
        usuario.setSenha(rs.getString("senha"));
        usuario.setSaldo(rs.getDouble("saldo"));
        
        String criadoEmStr = rs.getString("criado_em");
        String atualizadoEmStr = rs.getString("atualizado_em");
        
        if (criadoEmStr != null) {
            usuario.setCriadoEm(LocalDateTime.parse(criadoEmStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (atualizadoEmStr != null) {
            usuario.setAtualizadoEm(LocalDateTime.parse(atualizadoEmStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        return usuario;
    }
}
