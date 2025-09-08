package com.newpix.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newpix.util.ErrorHandler;
import com.newpix.util.ConnectionConfig;
import validador.Validator;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Cliente robusto do sistema NewPix que se conecta ao servidor via socket.
 */
public class NewPixClient {
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ObjectMapper objectMapper;
    private String serverHost;
    private int serverPort;
    private volatile boolean connected = false;
    private final Object connectionLock = new Object();
    
    public NewPixClient(String serverHost, int serverPort) {
        this.serverHost = ConnectionConfig.normalizeHost(serverHost);
        this.serverPort = ConnectionConfig.parsePort(String.valueOf(serverPort));
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Conecta ao servidor de forma robusta com retry automático.
     */
    public boolean connect() {
        synchronized (connectionLock) {
            if (connected) {
                return true; // Já conectado
            }
            
            return ErrorHandler.safeExecute(() -> {
                
                // Validar parâmetros de conexão
                if (!ConnectionConfig.isValidHost(serverHost)) {
                    throw new IllegalArgumentException("Host inválido: " + serverHost);
                }
                
                if (!ConnectionConfig.isValidPort(String.valueOf(serverPort))) {
                    throw new IllegalArgumentException("Porta inválida: " + serverPort);
                }
                
                // Testar conectividade antes de tentar conectar
                if (!ConnectionConfig.testConnection(serverHost, serverPort)) {
                    throw new IOException("Servidor não está acessível em " + serverHost + ":" + serverPort);
                }
                
                // Criar conexão robusta
                socket = ConnectionConfig.createRobustConnection(serverHost, serverPort);
                
                // Configurar streams
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
                connected = true;
                
                System.out.println("Conectado com sucesso ao servidor " + serverHost + ":" + serverPort);
                return true;
                
            }, null, "Conectar ao servidor");
        }
    }
    
    /**
     * Desconecta do servidor de forma gracíosa.
     */
    public void disconnect() {
        synchronized (connectionLock) {
            if (!connected) {
                return; // Já desconectado
            }
            
            connected = false;
            
            // Enviar comando de desconexão
            ErrorHandler.safeExecuteVoid(() -> {
                if (out != null && !out.checkError()) {
                    out.println("DISCONNECT");
                    out.flush();
                }
            }, null, "Enviar comando DISCONNECT");
            
            // Fechar streams e socket
            cleanup();
            
            System.out.println("Desconectado do servidor.");
        }
    }
    
    private void cleanup() {
        ErrorHandler.safeExecuteVoid(() -> {
            if (out != null) {
                out.close();
            }
        }, null, "Fechar PrintWriter");
        
        ErrorHandler.safeExecuteVoid(() -> {
            if (in != null) {
                in.close();
            }
        }, null, "Fechar BufferedReader");
        
        ErrorHandler.safeExecuteVoid(() -> {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }, null, "Fechar Socket");
    }
    
    /**
     * Envia uma mensagem para o servidor de forma robusta e retorna a resposta.
     */
    public String sendMessage(String jsonMessage) throws IOException {
        synchronized (connectionLock) {
            if (!connected) {
                throw new IOException("Cliente não está conectado ao servidor");
            }
            
            if (jsonMessage == null || jsonMessage.trim().isEmpty()) {
                throw new IllegalArgumentException("Mensagem não pode ser vazia");
            }
            
            try {
                // Validar mensagem antes de enviar
                validateAndSendMessage(jsonMessage);
                
                // Aguardar e validar resposta
                return receiveAndValidateResponse();
                
            } catch (SocketTimeoutException e) {
                throw new IOException("Timeout na comunicação com o servidor", e);
            } catch (SocketException e) {
                connected = false; // Marcar como desconectado
                throw new IOException("Conexão com servidor perdida", e);
            } catch (Exception e) {
                throw new IOException("Erro na comunicação: " + e.getMessage(), e);
            }
        }
    }
    
    private void validateAndSendMessage(String jsonMessage) throws Exception {
        // Validar estrutura JSON
        try {
            objectMapper.readTree(jsonMessage);
        } catch (Exception e) {
            throw new IllegalArgumentException("Mensagem não é um JSON válido", e);
        }
        
        // Validar mensagem do cliente
        Validator.validateClient(jsonMessage);
        
        // Enviar mensagem
        out.println(jsonMessage);
        out.flush();
        
        // Verificar se houve erro na escrita
        if (out.checkError()) {
            throw new IOException("Erro ao enviar mensagem para o servidor");
        }
    }
    
    private String receiveAndValidateResponse() throws Exception {
        String response = in.readLine();
        
        if (response == null) {
            connected = false;
            throw new IOException("Conexão com servidor perdida (EOF)");
        }
        
        if (response.trim().isEmpty()) {
            throw new IOException("Resposta vazia do servidor");
        }
        
        // Validar estrutura JSON da resposta
        try {
            objectMapper.readTree(response);
        } catch (Exception e) {
            throw new IOException("Resposta do servidor não é um JSON válido: " + response, e);
        }
        
        // Validar resposta do servidor
        Validator.validateServer(response);
        
        return response;
    }
    
    /**
     * Verifica se o cliente está conectado.
     */
    public boolean isConnected() {
        synchronized (connectionLock) {
            return connected && socket != null && !socket.isClosed();
        }
    }
    
    /**
     * Tenta reconectar automaticamente.
     */
    public boolean reconnect() {
        disconnect();
        
        // Aguardar um pouco antes de tentar reconectar
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        
        return connect();
    }
    
    /**
     * Método simplificado para login (compatibilidade com GUI).
     */
    public boolean loginSimple(String cpf, String senha) {
        try {
            String request = String.format(
                "{\"operacao\":\"usuario_login\",\"cpf\":\"%s\",\"senha\":\"%s\"}", 
                cpf, senha
            );
            
            String response = sendMessage(request);
            return response.contains("\"status\":true");
            
        } catch (Exception e) {
            ErrorHandler.handleUnexpectedError(e, null, "Login simples");
            return false;
        }
    }
    
    /**
     * Método simplificado para cadastro (compatibilidade com GUI).
     */
    public boolean cadastroSimple(String cpf, String nome, String senha) {
        try {
            String request = String.format(
                "{\"operacao\":\"usuario_criar\",\"cpf\":\"%s\",\"nome\":\"%s\",\"senha\":\"%s\"}", 
                cpf, nome, senha
            );
            
            String response = sendMessage(request);
            return response.contains("\"status\":true");
            
        } catch (Exception e) {
            ErrorHandler.handleUnexpectedError(e, null, "Cadastro simples");
            return false;
        }
    }
    
    /**
     * Método simplificado para obter dados do usuário.
     */
    public String getDadosUsuario(String token) {
        try {
            String request = String.format(
                "{\"operacao\":\"usuario_ler\",\"token\":\"%s\"}", 
                token
            );
            
            return sendMessage(request);
            
        } catch (Exception e) {
            ErrorHandler.handleUnexpectedError(e, null, "Obter dados do usuário");
            return "{\"status\":false,\"info\":\"Erro na comunicação\"}";
        }
    }
    
    /**
     * Método simplificado para criar transação PIX.
     */
    public boolean criarPix(String token, double valor, String cpfDestino) {
        try {
            String request = String.format(
                "{\"operacao\":\"transacao_criar\",\"token\":\"%s\",\"valor\":%.2f,\"cpf_destino\":\"%s\"}", 
                token, valor, cpfDestino
            );
            
            String response = sendMessage(request);
            return response.contains("\"status\":true");
            
        } catch (Exception e) {
            ErrorHandler.handleUnexpectedError(e, null, "Criar PIX");
            return false;
        }
    }
    
    /**
     * Método simplificado para obter histórico de transações.
     */
    public String getHistoricoTransacoes(String token) {
        try {
            String request = String.format(
                "{\"operacao\":\"transacao_ler\",\"token\":\"%s\",\"pagina\":1,\"limite\":100}", 
                token
            );
            
            return sendMessage(request);
            
        } catch (Exception e) {
            ErrorHandler.handleUnexpectedError(e, null, "Obter histórico");
            return "{\"status\":false,\"info\":\"Erro na comunicação\"}";
        }
    }
}
