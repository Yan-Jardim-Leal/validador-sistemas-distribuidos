package com.newpix.server;

import com.newpix.dao.DatabaseManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Servidor principal do sistema NewPix.
 */
public class NewPixServer {
    
    private final int port;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private volatile boolean running = false;
    
    public NewPixServer(int port) {
        this.port = port;
        this.threadPool = Executors.newCachedThreadPool();
    }
    
    /**
     * Inicia o servidor.
     */
    public void start() throws IOException {
        // Inicializar banco de dados
        try {
            DatabaseManager.getInstance().initializeDatabase();
            System.out.println("Banco de dados inicializado.");
        } catch (Exception e) {
            System.err.println("Erro ao inicializar banco de dados: " + e.getMessage());
            throw new RuntimeException("Falha na inicialização do banco de dados", e);
        }
        
        // Criar socket do servidor
        serverSocket = new ServerSocket(port);
        running = true;
        
        System.out.println("Servidor NewPix iniciado na porta " + port);
        System.out.println("Aguardando conexões...");
        
        // Loop principal do servidor
        while (running && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                
                // Criar e executar handler do cliente em thread separada
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                threadPool.execute(clientHandler);
                
            } catch (IOException e) {
                if (running) {
                    System.err.println("Erro ao aceitar conexão: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Para o servidor.
     */
    public void stop() throws IOException {
        running = false;
        
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        
        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
        }
        
        System.out.println("Servidor parado.");
    }
    
    /**
     * Verifica se o servidor está rodando.
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Obtém a porta do servidor.
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Método principal para execução via linha de comando.
     */
    public static void main(String[] args) {
        int port = 8080; // Porta padrão
        
        // Verificar se porta foi fornecida via argumentos
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Porta inválida: " + args[0]);
                System.err.println("Usando porta padrão: " + port);
            }
        }
        
        NewPixServer server = new NewPixServer(port);
        
        // Configurar shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("\nParando servidor...");
                server.stop();
            } catch (IOException e) {
                System.err.println("Erro ao parar servidor: " + e.getMessage());
            }
        }));
        
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Erro ao iniciar servidor: " + e.getMessage());
            System.exit(1);
        }
    }
}
