package com.newpix.server;

import java.io.*;
import java.net.Socket;

/**
 * Thread que processa a conexão de um cliente.
 */
public class ClientHandler extends Thread {
    
    private final Socket clientSocket;
    private final MessageProcessor messageProcessor;
    private PrintWriter out;
    private BufferedReader in;
    
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        this.messageProcessor = new MessageProcessor();
    }
    
    @Override
    public void run() {
        try {
            setupStreams();
            handleClient();
        } catch (IOException e) {
            System.err.println("Erro na comunicacao com cliente: " + e.getMessage());
        } finally {
            cleanup();
        }
    }
    
    private void setupStreams() throws IOException {
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }
    
    private void handleClient() throws IOException {
        String clientAddress = clientSocket.getRemoteSocketAddress().toString();
        System.out.println("Cliente conectado: " + clientAddress);
        
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            if ("DISCONNECT".equals(inputLine)) {
                System.out.println("Cliente solicitou desconexao: " + clientAddress);
                break;
            }
            
            try {
                // Processar mensagem JSON
                String response = messageProcessor.processMessage(inputLine);
                
                // Enviar resposta
                out.println(response);
                
                System.out.println("Mensagem processada para: " + clientAddress);
                
            } catch (Exception e) {
                System.err.println("Erro ao processar mensagem do cliente " + clientAddress + ": " + e.getMessage());
                
                // Enviar resposta de erro
                String errorResponse = "{\"operacao\":\"unknown\",\"status\":false,\"info\":\"Erro interno do servidor\"}";
                out.println(errorResponse);
            }
        }
        
        System.out.println("Cliente desconectado: " + clientAddress);
    }
    
    private void cleanup() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Erro ao fechar conexao: " + e.getMessage());
        }
    }
}
