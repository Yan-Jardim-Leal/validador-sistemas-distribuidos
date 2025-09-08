package com.newpix.server;

import com.newpix.util.ErrorHandler;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Thread que processa a conexão de um cliente de forma extremamente robusta.
 */
public class ClientHandler extends Thread {
    
    private final Socket clientSocket;
    private final MessageProcessor messageProcessor;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean running = true;
    private String clientAddress = "unknown";
    
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        this.messageProcessor = new MessageProcessor();
        
        // Configurar thread como daemon para shutdown gracioso
        setDaemon(true);
        setName("ClientHandler-" + socket.getRemoteSocketAddress());
    }
    
    @Override
    public void run() {
        ErrorHandler.safeExecuteVoid(() -> {
            setupStreams();
            handleClient();
        }, null, "Cliente Handler Main Loop");
        
        // Sempre fazer cleanup, mesmo em caso de erro
        cleanup();
    }
    
    private void setupStreams() throws IOException {
        try {
            // Configurar timeouts para evitar clientes travados
            clientSocket.setSoTimeout(60000); // 60 segundos timeout
            clientSocket.setKeepAlive(true);
            
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
            clientAddress = clientSocket.getRemoteSocketAddress().toString();
            
        } catch (Exception e) {
            throw new IOException("Falha ao configurar streams de comunicação", e);
        }
    }
    
    private void handleClient() throws IOException {
        System.out.println("Cliente conectado: " + clientAddress);
        
        try {
            String inputLine;
            while (running && (inputLine = readLineWithTimeout()) != null) {
                
                // Verificar comando de desconexão
                if ("DISCONNECT".equals(inputLine.trim())) {
                    System.out.println("Cliente solicitou desconexão: " + clientAddress);
                    break;
                }
                
                // Verificar se não é uma linha vazia
                if (inputLine.trim().isEmpty()) {
                    continue;
                }
                
                // Processar mensagem de forma robusta
                processClientMessage(inputLine);
            }
            
        } catch (SocketTimeoutException e) {
            System.out.println("Timeout na conexão com cliente: " + clientAddress);
        } catch (SocketException e) {
            System.out.println("Conexão perdida com cliente: " + clientAddress);
        } catch (Exception e) {
            ErrorHandler.handleUnexpectedError(e, null, "Comunicação com cliente " + clientAddress);
        }
        
        System.out.println("Cliente desconectado: " + clientAddress);
    }
    
    private String readLineWithTimeout() throws IOException {
        try {
            return in.readLine();
        } catch (SocketTimeoutException e) {
            // Log timeout mas não propaga exceção - permite graceful shutdown
            System.out.println("Timeout de leitura para cliente: " + clientAddress);
            return null;
        }
    }
    
    private void processClientMessage(String inputLine) {
        ErrorHandler.safeExecuteVoid(() -> {
            
            // Processar mensagem JSON
            String response = messageProcessor.processMessage(inputLine);
            
            // Verificar se a resposta é válida
            if (response == null || response.trim().isEmpty()) {
                response = createErrorResponse("Resposta vazia do processador");
            }
            
            // Enviar resposta de forma segura
            sendResponse(response);
            
            System.out.println("Mensagem processada para: " + clientAddress);
            
        }, null, "Processamento de mensagem do cliente " + clientAddress);
    }
    
    private void sendResponse(String response) {
        try {
            if (out != null && !out.checkError()) {
                out.println(response);
                out.flush();
                
                // Verificar se houve erro na escrita
                if (out.checkError()) {
                    throw new IOException("Erro ao escrever resposta para cliente");
                }
            } else {
                throw new IOException("Stream de saída inválido ou com erro");
            }
            
        } catch (Exception e) {
            System.err.println("Erro ao enviar resposta para cliente " + clientAddress + ": " + e.getMessage());
            
            // Tentar enviar resposta de erro como fallback
            try {
                if (out != null) {
                    String errorResponse = createErrorResponse("Erro interno na comunicação");
                    out.println(errorResponse);
                    out.flush();
                }
            } catch (Exception fallbackError) {
                System.err.println("Falha completa na comunicação com cliente " + clientAddress);
                running = false; // Forçar encerramento da conexão
            }
        }
    }
    
    private String createErrorResponse(String errorMessage) {
        return String.format(
            "{\"operacao\":\"unknown\",\"status\":false,\"info\":\"%s\"}", 
            errorMessage.replace("\"", "\\\"")
        );
    }
    
    private void cleanup() {
        running = false;
        
        ErrorHandler.safeExecuteVoid(() -> {
            
            if (out != null) {
                out.close();
            }
            
        }, null, "Cleanup PrintWriter");
        
        ErrorHandler.safeExecuteVoid(() -> {
            
            if (in != null) {
                in.close();
            }
            
        }, null, "Cleanup BufferedReader");
        
        ErrorHandler.safeExecuteVoid(() -> {
            
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            
        }, null, "Cleanup Socket");
        
        System.out.println("Cleanup completo para cliente: " + clientAddress);
    }
    
    /**
     * Método para encerrar graciosamente a conexão.
     */
    public void shutdown() {
        running = false;
        
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (Exception e) {
            System.err.println("Erro durante shutdown do cliente " + clientAddress + ": " + e.getMessage());
        }
    }
}
