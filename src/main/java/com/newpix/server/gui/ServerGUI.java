package com.newpix.server.gui;

import com.newpix.dao.DatabaseManager;
import com.newpix.server.NewPixServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;

/**
 * Interface gráfica do servidor NewPix.
 */
public class ServerGUI extends JFrame {
    
    private NewPixServer server;
    private JButton startButton;
    private JButton stopButton;
    private JTextArea logArea;
    private JTextField portField;
    private JLabel statusLabel;
    private boolean serverRunning = false;
    
    public ServerGUI() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        initializeDatabase();
    }
    
    private void initializeComponents() {
        setTitle("NewPix Server - Sistema Bancario Distribuido");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Componentes
        startButton = new JButton("Iniciar Servidor");
        stopButton = new JButton("Parar Servidor");
        stopButton.setEnabled(false);
        
        logArea = new JTextArea(20, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        portField = new JTextField("8080", 10);
        statusLabel = new JLabel("Servidor Parado");
        statusLabel.setForeground(Color.RED);
        
        // Redirecionar saída para a área de log
        redirectSystemOutput();
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel superior - controles
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(new JLabel("Porta:"));
        controlPanel.add(portField);
        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(statusLabel);
        
        // Panel central - log
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Log do Servidor"));
        
        // Panel inferior - informações
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Informações"));
        
        JTextArea infoArea = new JTextArea(
            "NewPix Server - Sistema Bancário Distribuído\n" +
            "Protocolo: JSON over TCP\n" +
            "Funcionalidades: Login/Logout, CRUD Usuários, Transações PIX\n" +
            "Banco de Dados: SQLite (newpix.db)"
        );
        infoArea.setEditable(false);
        infoArea.setBackground(getBackground());
        infoPanel.add(infoArea, BorderLayout.CENTER);
        
        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startServer();
            }
        });
        
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopServer();
            }
        });
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (serverRunning) {
                    stopServer();
                }
                System.exit(0);
            }
        });
    }
    
    private void initializeDatabase() {
        try {
            DatabaseManager.getInstance().initializeDatabase();
            logArea.append("Banco de dados inicializado com sucesso.\n");
        } catch (Exception e) {
            logArea.append("Erro ao inicializar banco de dados: " + e.getMessage() + "\n");
        }
    }
    
    private void startServer() {
        try {
            int port = Integer.parseInt(portField.getText());
            
            server = new NewPixServer(port);
            new Thread(() -> {
                try {
                    server.start();
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        logArea.append("Erro ao iniciar servidor: " + e.getMessage() + "\n");
                        updateServerStatus(false);
                    });
                }
            }).start();
            
            updateServerStatus(true);
            logArea.append("Servidor iniciado na porta " + port + "\n");
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Porta inválida!", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            logArea.append("Erro ao iniciar servidor: " + e.getMessage() + "\n");
        }
    }
    
    private void stopServer() {
        if (server != null) {
            try {
                server.stop();
                updateServerStatus(false);
                logArea.append("Servidor parado.\n");
            } catch (Exception e) {
                logArea.append("Erro ao parar servidor: " + e.getMessage() + "\n");
            }
        }
    }
    
    private void updateServerStatus(boolean running) {
        serverRunning = running;
        startButton.setEnabled(!running);
        stopButton.setEnabled(running);
        portField.setEnabled(!running);
        
        if (running) {
            statusLabel.setText("Servidor Ativo");
            statusLabel.setForeground(Color.GREEN);
        } else {
            statusLabel.setText("Servidor Parado");
            statusLabel.setForeground(Color.RED);
        }
    }
    
    private void redirectSystemOutput() {
        // Redirecionar System.out e System.err para a área de log
        PrintStream customOut = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                SwingUtilities.invokeLater(() -> {
                    logArea.append(String.valueOf((char) b));
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                });
            }
        });
        
        System.setOut(customOut);
        System.setErr(customOut);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ServerGUI().setVisible(true);
        });
    }
}
