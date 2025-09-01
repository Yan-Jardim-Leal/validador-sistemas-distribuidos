package com.newpix.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Classe responsável pela conexão e inicialização do banco de dados SQLite.
 */
public class DatabaseManager {
    
    private static final String DATABASE_URL = "jdbc:sqlite:newpix.db";
    private static DatabaseManager instance;
    
    private DatabaseManager() {}
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * Obtém uma conexão com o banco de dados.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }
    
    /**
     * Inicializa o banco de dados criando as tabelas necessárias.
     */
    public void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Criar tabela de usuários
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS usuarios (
                    cpf TEXT PRIMARY KEY,
                    nome TEXT NOT NULL,
                    senha TEXT NOT NULL,
                    saldo REAL DEFAULT 0.0,
                    criado_em DATETIME DEFAULT CURRENT_TIMESTAMP,
                    atualizado_em DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """;
            
            // Criar tabela de transações
            String createTransactionsTable = """
                CREATE TABLE IF NOT EXISTS transacoes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    valor_enviado REAL NOT NULL,
                    cpf_enviador TEXT NOT NULL,
                    cpf_recebedor TEXT NOT NULL,
                    criado_em DATETIME DEFAULT CURRENT_TIMESTAMP,
                    atualizado_em DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (cpf_enviador) REFERENCES usuarios(cpf),
                    FOREIGN KEY (cpf_recebedor) REFERENCES usuarios(cpf)
                )
            """;
            
            // Criar tabela de sessões
            String createSessionsTable = """
                CREATE TABLE IF NOT EXISTS sessoes (
                    token TEXT PRIMARY KEY,
                    cpf_usuario TEXT NOT NULL,
                    criado_em DATETIME DEFAULT CURRENT_TIMESTAMP,
                    expires_em DATETIME NOT NULL,
                    ativo BOOLEAN DEFAULT TRUE,
                    FOREIGN KEY (cpf_usuario) REFERENCES usuarios(cpf)
                )
            """;
            
            stmt.execute(createUsersTable);
            stmt.execute(createTransactionsTable);
            stmt.execute(createSessionsTable);
            
            System.out.println("Banco de dados inicializado com sucesso!");
        }
    }
    
    /**
     * Testa a conexão com o banco de dados.
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Erro ao testar conexão: " + e.getMessage());
            return false;
        }
    }
}
