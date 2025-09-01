package com.newpix.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import validador.Validator;

import java.io.*;
import java.net.Socket;

/**
 * Cliente do sistema NewPix que se conecta ao servidor via socket.
 */
public class NewPixClient {
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ObjectMapper objectMapper;
    private String serverHost;
    private int serverPort;
    private boolean connected = false;
    
    public NewPixClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Conecta ao servidor.
     */
    public boolean connect() {
        try {
            socket = new Socket(serverHost, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;
            
            System.out.println("Conectado ao servidor " + serverHost + ":" + serverPort);
            return true;
            
        } catch (IOException e) {
            System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Desconecta do servidor.
     */
    public void disconnect() {
        try {
            if (out != null) {
                out.println("DISCONNECT");
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null) {
                socket.close();
            }
            connected = false;
            System.out.println("Desconectado do servidor.");
            
        } catch (IOException e) {
            System.err.println("Erro ao desconectar: " + e.getMessage());
        }
    }
    
    /**
     * Envia uma mensagem para o servidor e retorna a resposta.
     */
    public String sendMessage(String jsonMessage) throws IOException {
        if (!connected) {
            throw new IOException("Cliente não está conectado ao servidor");
        }
        
        try {
            // Validar mensagem antes de enviar
            Validator.validateClient(jsonMessage);
            
            // Enviar mensagem
            out.println(jsonMessage);
            
            // Aguardar resposta
            String response = in.readLine();
            
            if (response != null) {
                // Validar resposta do servidor
                Validator.validateServer(response);
                return response;
            } else {
                throw new IOException("Conexão com servidor perdida");
            }
            
        } catch (Exception e) {
            throw new IOException("Erro na comunicação: " + e.getMessage(), e);
        }
    }
    
    /**
     * Realiza login no sistema.
     */
    public LoginResult login(String cpf, String senha) {
        try {
            ObjectNode request = objectMapper.createObjectNode();
            request.put("operacao", "usuario_login");
            request.put("cpf", cpf);
            request.put("senha", senha);
            
            String response = sendMessage(objectMapper.writeValueAsString(request));
            JsonNode responseNode = objectMapper.readTree(response);
            
            boolean success = responseNode.get("status").asBoolean();
            String info = responseNode.get("info").asText();
            String token = success && responseNode.has("token") ? responseNode.get("token").asText() : null;
            
            return new LoginResult(success, info, token);
            
        } catch (Exception e) {
            return new LoginResult(false, "Erro na comunicação: " + e.getMessage(), null);
        }
    }
    
    /**
     * Realiza logout do sistema.
     */
    public OperationResult logout(String token) {
        try {
            ObjectNode request = objectMapper.createObjectNode();
            request.put("operacao", "usuario_logout");
            request.put("token", token);
            
            String response = sendMessage(objectMapper.writeValueAsString(request));
            JsonNode responseNode = objectMapper.readTree(response);
            
            boolean success = responseNode.get("status").asBoolean();
            String info = responseNode.get("info").asText();
            
            return new OperationResult(success, info);
            
        } catch (Exception e) {
            return new OperationResult(false, "Erro na comunicação: " + e.getMessage());
        }
    }
    
    /**
     * Cria um novo usuário.
     */
    public OperationResult criarUsuario(String cpf, String nome, String senha) {
        try {
            ObjectNode request = objectMapper.createObjectNode();
            request.put("operacao", "usuario_criar");
            request.put("cpf", cpf);
            request.put("nome", nome);
            request.put("senha", senha);
            
            String response = sendMessage(objectMapper.writeValueAsString(request));
            JsonNode responseNode = objectMapper.readTree(response);
            
            boolean success = responseNode.get("status").asBoolean();
            String info = responseNode.get("info").asText();
            
            return new OperationResult(success, info);
            
        } catch (Exception e) {
            return new OperationResult(false, "Erro na comunicação: " + e.getMessage());
        }
    }
    
    /**
     * Lê dados do usuário.
     */
    public UserDataResult lerUsuario(String token) {
        try {
            ObjectNode request = objectMapper.createObjectNode();
            request.put("operacao", "usuario_ler");
            request.put("token", token);
            
            String response = sendMessage(objectMapper.writeValueAsString(request));
            JsonNode responseNode = objectMapper.readTree(response);
            
            boolean success = responseNode.get("status").asBoolean();
            String info = responseNode.get("info").asText();
            
            if (success && responseNode.has("usuario")) {
                JsonNode userNode = responseNode.get("usuario");
                String cpf = userNode.get("cpf").asText();
                String nome = userNode.get("nome").asText();
                double saldo = userNode.get("saldo").asDouble();
                
                return new UserDataResult(success, info, cpf, nome, saldo);
            }
            
            return new UserDataResult(success, info, null, null, 0.0);
            
        } catch (Exception e) {
            return new UserDataResult(false, "Erro na comunicação: " + e.getMessage(), null, null, 0.0);
        }
    }
    
    /**
     * Atualiza dados do usuário.
     */
    public OperationResult atualizarUsuario(String token, String novoNome, String novaSenha) {
        try {
            ObjectNode request = objectMapper.createObjectNode();
            request.put("operacao", "usuario_atualizar");
            request.put("token", token);
            
            ObjectNode userNode = objectMapper.createObjectNode();
            if (novoNome != null && !novoNome.trim().isEmpty()) {
                userNode.put("nome", novoNome);
            }
            if (novaSenha != null && !novaSenha.trim().isEmpty()) {
                userNode.put("senha", novaSenha);
            }
            
            request.set("usuario", userNode);
            
            String response = sendMessage(objectMapper.writeValueAsString(request));
            JsonNode responseNode = objectMapper.readTree(response);
            
            boolean success = responseNode.get("status").asBoolean();
            String info = responseNode.get("info").asText();
            
            return new OperationResult(success, info);
            
        } catch (Exception e) {
            return new OperationResult(false, "Erro na comunicação: " + e.getMessage());
        }
    }
    
    /**
     * Deleta o usuário.
     */
    public OperationResult deletarUsuario(String token) {
        try {
            ObjectNode request = objectMapper.createObjectNode();
            request.put("operacao", "usuario_deletar");
            request.put("token", token);
            
            String response = sendMessage(objectMapper.writeValueAsString(request));
            JsonNode responseNode = objectMapper.readTree(response);
            
            boolean success = responseNode.get("status").asBoolean();
            String info = responseNode.get("info").asText();
            
            return new OperationResult(success, info);
            
        } catch (Exception e) {
            return new OperationResult(false, "Erro na comunicação: " + e.getMessage());
        }
    }
    
    /**
     * Cria uma transação (PIX).
     */
    public OperationResult criarTransacao(String token, double valor, String cpfDestino) {
        try {
            ObjectNode request = objectMapper.createObjectNode();
            request.put("operacao", "transacao_criar");
            request.put("token", token);
            request.put("valor", valor);
            request.put("cpf_destino", cpfDestino);
            
            String response = sendMessage(objectMapper.writeValueAsString(request));
            JsonNode responseNode = objectMapper.readTree(response);
            
            boolean success = responseNode.get("status").asBoolean();
            String info = responseNode.get("info").asText();
            
            return new OperationResult(success, info);
            
        } catch (Exception e) {
            return new OperationResult(false, "Erro na comunicação: " + e.getMessage());
        }
    }
    
    /**
     * Lê transações por período.
     */
    public TransactionResult lerTransacoes(String token, String dataInicial, String dataFinal) {
        try {
            ObjectNode request = objectMapper.createObjectNode();
            request.put("operacao", "transacao_ler");
            request.put("token", token);
            request.put("data_inicial", dataInicial);
            request.put("data_final", dataFinal);
            
            String response = sendMessage(objectMapper.writeValueAsString(request));
            JsonNode responseNode = objectMapper.readTree(response);
            
            boolean success = responseNode.get("status").asBoolean();
            String info = responseNode.get("info").asText();
            String transacoes = success && responseNode.has("transacoes") ? 
                               responseNode.get("transacoes").toString() : null;
            
            return new TransactionResult(success, info, transacoes);
            
        } catch (Exception e) {
            return new TransactionResult(false, "Erro na comunicação: " + e.getMessage(), null);
        }
    }
    
    /**
     * Verifica se está conectado.
     */
    public boolean isConnected() {
        return connected;
    }
    
    // Classes de resultado
    public static class LoginResult {
        private final boolean success;
        private final String message;
        private final String token;
        
        public LoginResult(boolean success, String message, String token) {
            this.success = success;
            this.message = message;
            this.token = token;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getToken() { return token; }
    }
    
    public static class OperationResult {
        private final boolean success;
        private final String message;
        
        public OperationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
    
    public static class UserDataResult extends OperationResult {
        private final String cpf;
        private final String nome;
        private final double saldo;
        
        public UserDataResult(boolean success, String message, String cpf, String nome, double saldo) {
            super(success, message);
            this.cpf = cpf;
            this.nome = nome;
            this.saldo = saldo;
        }
        
        public String getCpf() { return cpf; }
        public String getNome() { return nome; }
        public double getSaldo() { return saldo; }
    }
    
    public static class TransactionResult extends OperationResult {
        private final String transacoes;
        
        public TransactionResult(boolean success, String message, String transacoes) {
            super(success, message);
            this.transacoes = transacoes;
        }
        
        public String getTransacoes() { return transacoes; }
    }
}
