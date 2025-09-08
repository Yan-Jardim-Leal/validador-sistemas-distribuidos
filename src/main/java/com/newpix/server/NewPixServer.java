package com.newpix.server;

import com.newpix.dao.DatabaseManager;
import com.newpix.util.ErrorHandler;
import com.newpix.util.ConnectionConfig;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Servidor principal do sistema NewPix com tratamento robusto de erros.
 */
public class NewPixServer {
    
    private final int port;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private volatile boolean running = false;
    private final List<ClientHandler> activeClients = new CopyOnWriteArrayList<>();
    
    public NewPixServer(int port) {
        this.port = port;
        this.threadPool = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("NewPix-ClientHandler-" + System.currentTimeMillis());
            return t;
        });
    }
    
    /**
     * Inicia o servidor de forma robusta.
     */
    public void start() throws IOException {
        // Verificar se já está rodando
        if (running) {
            throw new IllegalStateException("Servidor já está rodando");
        }
        
        // Inicializar banco de dados de forma robusta
        initializeDatabase();
        
        // Criar socket do servidor com tratamento de erros
        createServerSocket();
        
        // Registrar shutdown hook para cleanup gracioso
        registerShutdownHook();
        
        running = true;
        
        System.out.println("Servidor NewPix iniciado na porta " + port);
        System.out.println("Aguardando conexões...");
        
        // Loop principal do servidor
        runServerLoop();
    }
    
    private void initializeDatabase() throws IOException {
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            dbManager.initializeDatabase();
            
            // Testar conexão
            if (!dbManager.testConnection()) {
                throw new IOException("Falha no teste de conexão com o banco de dados");
            }
            
            System.out.println("Banco de dados inicializado e testado com sucesso!");
            
        } catch (Exception e) {
            String message = "Falha crítica na inicialização do banco de dados: " + e.getMessage();
            System.err.println(message);
            throw new IOException(message, e);
        }
    }
    
    private void createServerSocket() throws IOException {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            serverSocket.setSoTimeout(1000); // Timeout para permitir verificação de shutdown
            
        } catch (BindException e) {
            throw new IOException("Porta " + port + " já está em uso. Escolha outra porta.", e);
        } catch (Exception e) {
            throw new IOException("Falha ao criar socket do servidor na porta " + port, e);
        }
    }
    
    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown hook ativado - encerrando servidor graciosamente...");
            try {
                stop();
            } catch (Exception e) {
                System.err.println("Erro durante shutdown: " + e.getMessage());
            }
        }));
    }
    
    private void runServerLoop() {
        while (running && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                
                // Configurar cliente
                configureClientSocket(clientSocket);
                
                // Criar e executar handler do cliente
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                activeClients.add(clientHandler);
                threadPool.execute(clientHandler);
                
                System.out.println("Nova conexão aceita de: " + clientSocket.getRemoteSocketAddress());
                
            } catch (java.net.SocketTimeoutException e) {
                // Timeout normal - apenas continua o loop para verificar shutdown
                continue;
                
            } catch (SocketException e) {
                if (running) {
                    System.err.println("Erro no socket do servidor: " + e.getMessage());
                }
                // Se não está rodando, é um shutdown normal
                
            } catch (IOException e) {
                if (running) {
                    ErrorHandler.handleNetworkError(e, null, "Aceitar conexão de cliente");
                    
                    // Aguardar um pouco antes de tentar novamente
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                
            } catch (Exception e) {
                ErrorHandler.handleUnexpectedError(e, null, "Loop principal do servidor");
                
                // Em caso de erro inesperado, aguardar antes de continuar
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        System.out.println("Loop principal do servidor encerrado.");
    }
    
    private void configureClientSocket(Socket clientSocket) throws IOException {
        try {
            clientSocket.setTcpNoDelay(true);
            clientSocket.setKeepAlive(true);
            clientSocket.setSoTimeout(ConnectionConfig.READ_TIMEOUT);
        } catch (Exception e) {
            throw new IOException("Falha ao configurar socket do cliente", e);
        }
    }
    
    /**
     * Para o servidor de forma gracíosa.
     */
    public void stop() throws IOException {
        if (!running) {
            return; // Já parado
        }
        
        System.out.println("Iniciando parada do servidor...");
        running = false;
        
        // Fechar socket do servidor
        closeServerSocket();
        
        // Encerrar clientes ativos
        shutdownActiveClients();
        
        // Encerrar thread pool
        shutdownThreadPool();
        
        System.out.println("Servidor parado com sucesso.");
    }
    
    private void closeServerSocket() {
        ErrorHandler.safeExecuteVoid(() -> {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        }, null, "Fechar ServerSocket");
    }
    
    private void shutdownActiveClients() {
        System.out.println("Encerrando " + activeClients.size() + " conexões ativas...");
        
        for (ClientHandler client : activeClients) {
            ErrorHandler.safeExecuteVoid(() -> {
                client.shutdown();
            }, null, "Shutdown do cliente");
        }
        
        activeClients.clear();
    }
    
    private void shutdownThreadPool() {
        if (threadPool != null && !threadPool.isShutdown()) {
            try {
                threadPool.shutdown();
                
                // Aguardar terminação por até 10 segundos
                if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.out.println("Forçando encerramento do thread pool...");
                    threadPool.shutdownNow();
                    
                    // Aguardar mais 5 segundos
                    if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                        System.err.println("Thread pool não terminou completamente");
                    }
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                threadPool.shutdownNow();
            }
        }
    }
    
    /**
    /**
     * Verifica se o servidor está rodando.
     */
    public boolean isRunning() {
        return running && serverSocket != null && !serverSocket.isClosed();
    }
    
    /**
     * Retorna o número de clientes conectados.
     */
    public int getActiveClientCount() {
        return activeClients.size();
    }
    
    /**
     * Obtém a porta do servidor.
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Método principal para testes.
     */
    public static void main(String[] args) {
        int port = ConnectionConfig.DEFAULT_PORT;
        
        // Permitir porta customizada via argumentos
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
                if (port <= 0 || port > 65535) {
                    throw new NumberFormatException("Porta fora do range válido");
                }
            } catch (NumberFormatException e) {
                System.err.println("Porta inválida: " + args[0] + ". Usando porta padrão " + ConnectionConfig.DEFAULT_PORT);
                port = ConnectionConfig.DEFAULT_PORT;
            }
        }
        
        NewPixServer server = new NewPixServer(port);
        
        try {
            server.start();
        } catch (Exception e) {
            ErrorHandler.handleUnexpectedError(e, null, "Inicialização do servidor");
            System.exit(1);
        }
    }
}
