package com.newpix.util;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe centralizada para tratamento robusto de exceções e erros.
 * Garante que nenhuma exceção não tratada seja propagada para o usuário.
 */
public class ErrorHandler {
    
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private ErrorHandler() {}
    
    /**
     * Trata exceções de conexão de rede de forma robusta.
     */
    public static boolean handleNetworkError(Exception e, Component parent, String operation) {
        String userMessage = getNetworkErrorMessage(e, operation);
        logError("NETWORK_ERROR", e, operation);
        
        if (parent != null) {
            showErrorDialog(parent, "Erro de Conexão", userMessage);
        } else {
            System.err.println("ERRO DE CONEXÃO: " + userMessage);
        }
        
        return false;
    }
    
    /**
     * Trata erros de validação de dados de forma amigável.
     */
    public static boolean handleValidationError(Exception e, Component parent, String operation) {
        String userMessage = getValidationErrorMessage(e);
        logError("VALIDATION_ERROR", e, operation);
        
        if (parent != null) {
            showWarningDialog(parent, "Dados Inválidos", userMessage);
        } else {
            System.err.println("ERRO DE VALIDAÇÃO: " + userMessage);
        }
        
        return false;
    }
    
    /**
     * Trata erros de banco de dados de forma robusta.
     */
    public static boolean handleDatabaseError(Exception e, Component parent, String operation) {
        String userMessage = "Erro interno do sistema. Tente novamente em alguns instantes.";
        logError("DATABASE_ERROR", e, operation);
        
        if (parent != null) {
            showErrorDialog(parent, "Erro do Sistema", userMessage);
        } else {
            System.err.println("ERRO DE BANCO: " + userMessage);
        }
        
        return false;
    }
    
    /**
     * Trata erros gerais/inesperados de forma segura.
     */
    public static boolean handleUnexpectedError(Exception e, Component parent, String operation) {
        String userMessage = "Ocorreu um erro inesperado. A operação não pôde ser concluída.";
        logError("UNEXPECTED_ERROR", e, operation);
        
        if (parent != null) {
            showErrorDialog(parent, "Erro Inesperado", userMessage);
        } else {
            System.err.println("ERRO INESPERADO: " + userMessage);
        }
        
        return false;
    }
    
    /**
     * Trata erros de autenticação de forma específica.
     */
    public static boolean handleAuthenticationError(Exception e, Component parent, String operation) {
        String userMessage = "Credenciais inválidas ou sessão expirada. Verifique seus dados e tente novamente.";
        logError("AUTH_ERROR", e, operation);
        
        if (parent != null) {
            showWarningDialog(parent, "Erro de Autenticação", userMessage);
        } else {
            System.err.println("ERRO DE AUTENTICAÇÃO: " + userMessage);
        }
        
        return false;
    }
    
    /**
     * Executa uma operação de forma segura, capturando qualquer exceção.
     */
    public static boolean safeExecute(SafeOperation operation, Component parent, String operationName) {
        try {
            return operation.execute();
        } catch (java.net.ConnectException e) {
            return handleNetworkError(e, parent, operationName);
        } catch (java.net.SocketException e) {
            return handleNetworkError(e, parent, operationName);
        } catch (java.io.IOException e) {
            return handleNetworkError(e, parent, operationName);
        } catch (IllegalArgumentException e) {
            return handleValidationError(e, parent, operationName);
        } catch (java.sql.SQLException e) {
            return handleDatabaseError(e, parent, operationName);
        } catch (SecurityException e) {
            return handleAuthenticationError(e, parent, operationName);
        } catch (Exception e) {
            return handleUnexpectedError(e, parent, operationName);
        }
    }
    
    /**
     * Interface funcional para operações que podem falhar.
     */
    @FunctionalInterface
    public interface SafeOperation {
        boolean execute() throws Exception;
    }
    
    /**
     * Executa uma operação void de forma segura.
     */
    public static void safeExecuteVoid(SafeVoidOperation operation, Component parent, String operationName) {
        try {
            operation.execute();
        } catch (java.net.ConnectException e) {
            handleNetworkError(e, parent, operationName);
        } catch (java.net.SocketException e) {
            handleNetworkError(e, parent, operationName);
        } catch (java.io.IOException e) {
            handleNetworkError(e, parent, operationName);
        } catch (IllegalArgumentException e) {
            handleValidationError(e, parent, operationName);
        } catch (java.sql.SQLException e) {
            handleDatabaseError(e, parent, operationName);
        } catch (SecurityException e) {
            handleAuthenticationError(e, parent, operationName);
        } catch (Exception e) {
            handleUnexpectedError(e, parent, operationName);
        }
    }
    
    /**
     * Interface funcional para operações void que podem falhar.
     */
    @FunctionalInterface
    public interface SafeVoidOperation {
        void execute() throws Exception;
    }
    
    // Métodos privados auxiliares
    
    private static String getNetworkErrorMessage(Exception e, String operation) {
        if (e instanceof java.net.ConnectException) {
            return "Não foi possível conectar ao servidor. Verifique se o servidor está rodando e se o endereço está correto.";
        } else if (e instanceof java.net.SocketException) {
            return "Conexão com o servidor foi perdida. Verifique sua conexão de rede.";
        } else if (e instanceof java.io.IOException) {
            return "Erro de comunicação com o servidor. Tente novamente.";
        }
        return "Erro de rede desconhecido durante: " + operation;
    }
    
    private static String getValidationErrorMessage(Exception e) {
        String message = e.getMessage();
        if (message != null && !message.trim().isEmpty()) {
            return message;
        }
        return "Os dados fornecidos são inválidos. Verifique e tente novamente.";
    }
    
    private static void logError(String errorType, Exception e, String operation) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        System.err.printf("[%s] %s em '%s': %s%n", timestamp, errorType, operation, e.getMessage());
        
        // Log detalhado para depuração
        if (Boolean.getBoolean("newpix.debug")) {
            e.printStackTrace();
        }
    }
    
    private static void showErrorDialog(Component parent, String title, String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
        });
    }
    
    private static void showWarningDialog(Component parent, String title, String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
        });
    }
    
    /**
     * Trata shutdown gracioso do sistema.
     */
    public static void handleShutdown(String reason) {
        System.out.println("Sistema sendo encerrado: " + reason);
        
        // Cleanup adicional se necessário
        System.exit(0);
    }
}
