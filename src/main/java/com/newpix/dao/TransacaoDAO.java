package com.newpix.dao;

import com.newpix.model.Transacao;
import com.newpix.model.Usuario;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para operações com transações no banco de dados.
 */
public class TransacaoDAO {
    
    private final DatabaseManager dbManager;
    private final UsuarioDAO usuarioDAO;
    
    public TransacaoDAO() {
        this.dbManager = DatabaseManager.getInstance();
        this.usuarioDAO = new UsuarioDAO();
    }
    
    /**
     * Cria uma nova transação no banco de dados.
     */
    public boolean criar(Transacao transacao) throws SQLException {
        String sql = "INSERT INTO transacoes (valor_enviado, cpf_enviador, cpf_recebedor, criado_em, atualizado_em) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, transacao.getValorEnviado());
            pstmt.setString(2, transacao.getUsuarioEnviador().getCpf());
            pstmt.setString(3, transacao.getUsuarioRecebedor().getCpf());
            pstmt.setString(4, transacao.getCriadoEm().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            pstmt.setString(5, transacao.getAtualizadoEm().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Busca uma transação pelo ID.
     */
    public Transacao buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM transacoes WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return criarTransacaoFromResultSet(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Busca transações por período para um usuário específico.
     */
    public List<Transacao> buscarPorPeriodo(String cpfUsuario, LocalDateTime dataInicial, LocalDateTime dataFinal) throws SQLException {
        String sql = """
            SELECT * FROM transacoes 
            WHERE (cpf_enviador = ? OR cpf_recebedor = ?) 
            AND criado_em BETWEEN ? AND ? 
            ORDER BY criado_em DESC
        """;
        
        List<Transacao> transacoes = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cpfUsuario);
            pstmt.setString(2, cpfUsuario);
            pstmt.setString(3, dataInicial.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            pstmt.setString(4, dataFinal.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transacoes.add(criarTransacaoFromResultSet(rs));
                }
            }
        }
        
        return transacoes;
    }
    
    /**
     * Lista todas as transações de um usuário.
     */
    public List<Transacao> buscarPorUsuario(String cpfUsuario) throws SQLException {
        String sql = """
            SELECT * FROM transacoes 
            WHERE cpf_enviador = ? OR cpf_recebedor = ? 
            ORDER BY criado_em DESC
        """;
        
        List<Transacao> transacoes = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cpfUsuario);
            pstmt.setString(2, cpfUsuario);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transacoes.add(criarTransacaoFromResultSet(rs));
                }
            }
        }
        
        return transacoes;
    }
    
    /**
     * Lista todas as transações.
     */
    public List<Transacao> listarTodas() throws SQLException {
        String sql = "SELECT * FROM transacoes ORDER BY criado_em DESC";
        List<Transacao> transacoes = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                transacoes.add(criarTransacaoFromResultSet(rs));
            }
        }
        
        return transacoes;
    }
    
    /**
     * Executa uma transação completa (transferência de valores) de forma atômica.
     */
    public boolean executarTransferencia(String cpfEnviador, String cpfRecebedor, double valor) throws SQLException {
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            conn.setAutoCommit(false); // Iniciar transação
            
            // Verificar se ambos os usuários existem
            Usuario enviador = usuarioDAO.buscarPorCpf(cpfEnviador);
            Usuario recebedor = usuarioDAO.buscarPorCpf(cpfRecebedor);
            
            if (enviador == null || recebedor == null) {
                conn.rollback();
                return false;
            }
            
            // Verificar saldo suficiente
            if (!enviador.temSaldoSuficiente(valor)) {
                conn.rollback();
                return false;
            }
            
            // Atualizar saldos
            double novoSaldoEnviador = enviador.getSaldo() - valor;
            double novoSaldoRecebedor = recebedor.getSaldo() + valor;
            
            // Executar atualizações de saldo
            String sqlUpdateEnviador = "UPDATE usuarios SET saldo = ?, atualizado_em = ? WHERE cpf = ?";
            String sqlUpdateRecebedor = "UPDATE usuarios SET saldo = ?, atualizado_em = ? WHERE cpf = ?";
            String sqlInsertTransacao = "INSERT INTO transacoes (valor_enviado, cpf_enviador, cpf_recebedor, criado_em, atualizado_em) VALUES (?, ?, ?, ?, ?)";
            
            String agora = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            try (PreparedStatement pstmtEnviador = conn.prepareStatement(sqlUpdateEnviador);
                 PreparedStatement pstmtRecebedor = conn.prepareStatement(sqlUpdateRecebedor);
                 PreparedStatement pstmtTransacao = conn.prepareStatement(sqlInsertTransacao)) {
                
                // Atualizar saldo do enviador
                pstmtEnviador.setDouble(1, novoSaldoEnviador);
                pstmtEnviador.setString(2, agora);
                pstmtEnviador.setString(3, cpfEnviador);
                pstmtEnviador.executeUpdate();
                
                // Atualizar saldo do recebedor
                pstmtRecebedor.setDouble(1, novoSaldoRecebedor);
                pstmtRecebedor.setString(2, agora);
                pstmtRecebedor.setString(3, cpfRecebedor);
                pstmtRecebedor.executeUpdate();
                
                // Registrar a transação
                pstmtTransacao.setDouble(1, valor);
                pstmtTransacao.setString(2, cpfEnviador);
                pstmtTransacao.setString(3, cpfRecebedor);
                pstmtTransacao.setString(4, agora);
                pstmtTransacao.setString(5, agora);
                pstmtTransacao.executeUpdate();
                
                conn.commit(); // Confirmar transação
                return true;
            }
            
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
    
    /**
     * Cria um objeto Transacao a partir de um ResultSet.
     */
    private Transacao criarTransacaoFromResultSet(ResultSet rs) throws SQLException {
        Transacao transacao = new Transacao();
        transacao.setId(rs.getInt("id"));
        transacao.setValorEnviado(rs.getDouble("valor_enviado"));
        
        // Buscar usuários relacionados
        String cpfEnviador = rs.getString("cpf_enviador");
        String cpfRecebedor = rs.getString("cpf_recebedor");
        
        Usuario enviador = usuarioDAO.buscarPorCpf(cpfEnviador);
        Usuario recebedor = usuarioDAO.buscarPorCpf(cpfRecebedor);
        
        transacao.setUsuarioEnviador(enviador);
        transacao.setUsuarioRecebedor(recebedor);
        
        String criadoEmStr = rs.getString("criado_em");
        String atualizadoEmStr = rs.getString("atualizado_em");
        
        if (criadoEmStr != null) {
            transacao.setCriadoEm(LocalDateTime.parse(criadoEmStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (atualizadoEmStr != null) {
            transacao.setAtualizadoEm(LocalDateTime.parse(atualizadoEmStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        return transacao;
    }
}
