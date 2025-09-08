package com.newpix.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Utilitário para configurações robustas de conexão de rede.
 */
public class ConnectionConfig {
    
    public static final int DEFAULT_PORT = 8080;
    public static final String DEFAULT_HOST = "localhost";
    public static final int CONNECTION_TIMEOUT = 10000; // 10 segundos
    public static final int READ_TIMEOUT = 30000; // 30 segundos
    public static final int MAX_RETRY_ATTEMPTS = 3;
    public static final long RETRY_DELAY_MS = 1000; // 1 segundo
    
    private ConnectionConfig() {}
    
    /**
     * Valida se um host é válido e acessível.
     */
    public static boolean isValidHost(String host) {
        if (host == null || host.trim().isEmpty()) {
            return false;
        }
        
        try {
            InetAddress.getByName(host.trim());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Valida se uma porta é válida.
     */
    public static boolean isValidPort(String portStr) {
        if (portStr == null || portStr.trim().isEmpty()) {
            return false;
        }
        
        try {
            int port = Integer.parseInt(portStr.trim());
            return port > 0 && port <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Testa conectividade com um servidor antes de estabelecer conexão real.
     */
    public static boolean testConnection(String host, int port) {
        try (Socket testSocket = new Socket()) {
            testSocket.connect(new java.net.InetSocketAddress(host, port), CONNECTION_TIMEOUT);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Cria uma conexão robusta com retry automático.
     */
    public static Socket createRobustConnection(String host, int port) throws IOException {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                Socket socket = new Socket();
                socket.connect(new java.net.InetSocketAddress(host, port), CONNECTION_TIMEOUT);
                socket.setSoTimeout(READ_TIMEOUT);
                socket.setKeepAlive(true);
                socket.setTcpNoDelay(true);
                
                return socket;
                
            } catch (Exception e) {
                lastException = e;
                
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    System.out.printf("Tentativa %d/%d falhou. Tentando novamente em %d ms...%n", 
                                    attempt, MAX_RETRY_ATTEMPTS, RETRY_DELAY_MS);
                    
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Conexão interrompida", ie);
                    }
                }
            }
        }
        
        // Se chegou aqui, todas as tentativas falharam
        throw new IOException("Falha ao conectar após " + MAX_RETRY_ATTEMPTS + " tentativas", lastException);
    }
    
    /**
     * Normaliza um host removendo espaços e convertendo para minúsculo.
     */
    public static String normalizeHost(String host) {
        if (host == null) {
            return DEFAULT_HOST;
        }
        
        String normalized = host.trim().toLowerCase();
        return normalized.isEmpty() ? DEFAULT_HOST : normalized;
    }
    
    /**
     * Converte string de porta para inteiro com valor padrão.
     */
    public static int parsePort(String portStr) {
        if (portStr == null || portStr.trim().isEmpty()) {
            return DEFAULT_PORT;
        }
        
        try {
            int port = Integer.parseInt(portStr.trim());
            if (port > 0 && port <= 65535) {
                return port;
            }
        } catch (NumberFormatException e) {
            // Log do erro, mas retorna porta padrão
            System.err.println("Porta inválida '" + portStr + "', usando porta padrão " + DEFAULT_PORT);
        }
        
        return DEFAULT_PORT;
    }
}
