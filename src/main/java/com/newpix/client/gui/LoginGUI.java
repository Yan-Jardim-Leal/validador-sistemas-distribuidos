package com.newpix.client.gui;

import com.newpix.client.NewPixClient;
import com.newpix.util.ErrorHandler;
import com.newpix.util.ConnectionConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Interface gráfica robusta para login e cadastro de usuários.
 */
public class LoginGUI extends JFrame {
    
    private NewPixClient client;
    private JTextField cpfField;
    private JPasswordField senhaField;
    private JTextField nomeField;
    private JButton loginButton;
    private JButton cadastroButton;
    private JButton conectarButton;
    private JTextField hostField;
    private JTextField portField;
    private JLabel statusLabel;
    private volatile boolean isConnecting = false;
    
    public LoginGUI() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupWindowCloseHandler();
    }
    
    private void initializeComponents() {
        setTitle("NewPix - Sistema Bancário");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(420, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Campos de conexão
        hostField = new JTextField(ConnectionConfig.DEFAULT_HOST, 15);
        portField = new JTextField(String.valueOf(ConnectionConfig.DEFAULT_PORT), 8);
        conectarButton = new JButton("Conectar");
        
        // Campos de login/cadastro
        cpfField = new JTextField(20);
        senhaField = new JPasswordField(20);
        nomeField = new JTextField(20);
        
        loginButton = new JButton("Login");
        cadastroButton = new JButton("Cadastrar");
        
        statusLabel = new JLabel("Desconectado - Configure a conexão", SwingConstants.CENTER);
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
        
        // Desabilitar campos até conectar
        setFieldsEnabled(false);
        
        // Configurar tooltips
        setupTooltips();
    }
    
    private void setupTooltips() {
        hostField.setToolTipText("Endereço do servidor (ex: localhost, 192.168.1.100)");
        portField.setToolTipText("Porta do servidor (1-65535)");
        cpfField.setToolTipText("CPF no formato 000.000.000-00");
        senhaField.setToolTipText("Senha com pelo menos 6 caracteres");
        nomeField.setToolTipText("Nome completo com pelo menos 6 caracteres");
    }
    
    private void setupWindowCloseHandler() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
    }
    
    private void exitApplication() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "Deseja realmente sair do sistema?",
            "Confirmar Saída",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            if (client != null) {
                client.disconnect();
            }
            System.exit(0);
        }
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Panel de conexão
        JPanel conexaoPanel = new JPanel(new GridBagLayout());
        conexaoPanel.setBorder(BorderFactory.createTitledBorder("Conexão com Servidor"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0;
        conexaoPanel.add(new JLabel("Host:"), gbc);
        gbc.gridx = 1;
        conexaoPanel.add(hostField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        conexaoPanel.add(new JLabel("Porta:"), gbc);
        gbc.gridx = 1;
        conexaoPanel.add(portField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        conexaoPanel.add(conectarButton, gbc);
        
        gbc.gridy = 3;
        conexaoPanel.add(statusLabel, gbc);
        
        // Panel principal
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder("Sistema NewPix"));
        
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("CPF:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(cpfField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(nomeField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(senhaField, gbc);
        
        // Panel de botões
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(loginButton);
        buttonPanel.add(cadastroButton);
        
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(buttonPanel, gbc);
        
        // Adicionar painéis ao frame
        add(conexaoPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        
        // Panel de informações
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Informações"));
        
        JTextArea infoArea = new JTextArea(
            "NewPix - Sistema Bancário Distribuído\\n" +
            "1. Conecte-se ao servidor\\n" +
            "2. Faça login ou cadastre-se\\n" +
            "3. Realize transações PIX"
        );
        infoArea.setEditable(false);
        infoArea.setBackground(getBackground());
        infoPanel.add(infoArea);
        
        add(infoPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        conectarButton.addActionListener(e -> conectarServidor());
        loginButton.addActionListener(e -> realizarLogin());
        cadastroButton.addActionListener(e -> realizarCadastro());
        
        // Permitir Enter nos campos
        ActionListener enterAction = e -> {
            if (conectarButton.isEnabled()) {
                conectarServidor();
            } else if (loginButton.isEnabled()) {
                realizarLogin();
            }
        };
        
        hostField.addActionListener(enterAction);
        portField.addActionListener(enterAction);
        cpfField.addActionListener(enterAction);
        senhaField.addActionListener(enterAction);
    }
    
    private void conectarServidor() {
        if (isConnecting) {
            return; // Já conectando
        }
        
        // Executar conexão em thread separada para não travar a GUI
        SwingUtilities.invokeLater(() -> {
            conectarButton.setEnabled(false);
            conectarButton.setText("Conectando...");
            statusLabel.setText("Conectando ao servidor...");
            statusLabel.setForeground(Color.ORANGE);
            isConnecting = true;
        });
        
        new Thread(() -> {
            boolean success = ErrorHandler.safeExecute(() -> {
                
                String host = hostField.getText().trim();
                String portText = portField.getText().trim();
                
                // Validar entrada
                if (host.isEmpty()) {
                    throw new IllegalArgumentException("Host não pode estar vazio");
                }
                
                if (!ConnectionConfig.isValidHost(host)) {
                    throw new IllegalArgumentException("Host inválido: " + host);
                }
                
                if (!ConnectionConfig.isValidPort(portText)) {
                    throw new IllegalArgumentException("Porta inválida: " + portText);
                }
                
                int port = ConnectionConfig.parsePort(portText);
                
                // Criar cliente e conectar
                client = new NewPixClient(host, port);
                
                if (!client.connect()) {
                    throw new RuntimeException("Falha ao conectar ao servidor");
                }
                
                return true;
                
            }, this, "Conectar ao servidor");
            
            // Atualizar GUI na thread principal
            SwingUtilities.invokeLater(() -> {
                isConnecting = false;
                conectarButton.setText("Conectar");
                conectarButton.setEnabled(!success);
                
                if (success) {
                    statusLabel.setText("Conectado ao servidor");
                    statusLabel.setForeground(Color.GREEN);
                    setFieldsEnabled(true);
                    hostField.setEnabled(false);
                    portField.setEnabled(false);
                    cpfField.requestFocus();
                } else {
                    statusLabel.setText("Falha na conexão");
                    statusLabel.setForeground(Color.RED);
                }
            });
            
        }).start();
    }
    
    private void realizarLogin() {
        String cpf = cpfField.getText().trim();
        String senha = new String(senhaField.getPassword());
        
        // Validação básica
        if (cpf.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "CPF e senha são obrigatórios", 
                "Campos Obrigatórios", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Executar login em thread separada
        loginButton.setEnabled(false);
        loginButton.setText("Logando...");
        
        new Thread(() -> {
            boolean success = ErrorHandler.safeExecute(() -> {
                
                if (client == null || !client.isConnected()) {
                    throw new RuntimeException("Cliente não está conectado");
                }
                
                return client.loginSimple(cpf, senha);
                
            }, this, "Realizar login");
            
            SwingUtilities.invokeLater(() -> {
                loginButton.setEnabled(true);
                loginButton.setText("Login");
                
                if (success) {
                    // Fechar janela de login e abrir interface principal
                    abrirInterfacePrincipal(cpf);
                } else {
                    // Limpar senha em caso de erro
                    senhaField.setText("");
                    senhaField.requestFocus();
                }
            });
            
        }).start();
    }
    
    private void realizarCadastro() {
        String cpf = cpfField.getText().trim();
        String nome = nomeField.getText().trim();
        String senha = new String(senhaField.getPassword());
        
        // Validação robusta
        if (!validarDadosCadastro(cpf, nome, senha)) {
            return;
        }
        
        // Executar cadastro em thread separada
        cadastroButton.setEnabled(false);
        cadastroButton.setText("Cadastrando...");
        
        new Thread(() -> {
            boolean success = ErrorHandler.safeExecute(() -> {
                
                if (client == null || !client.isConnected()) {
                    throw new RuntimeException("Cliente não está conectado");
                }
                
                return client.cadastroSimple(cpf, nome, senha);
                
            }, this, "Realizar cadastro");
            
            SwingUtilities.invokeLater(() -> {
                cadastroButton.setEnabled(true);
                cadastroButton.setText("Cadastrar");
                
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Usuário cadastrado com sucesso!\\nFaça login para continuar.",
                        "Cadastro Realizado",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    // Limpar campos e focar no login
                    nomeField.setText("");
                    senhaField.setText("");
                    cpfField.requestFocus();
                }
            });
            
        }).start();
    }
    
    private boolean validarDadosCadastro(String cpf, String nome, String senha) {
        if (cpf.isEmpty() || nome.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Todos os campos são obrigatórios para cadastro",
                "Campos Obrigatórios",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (!cpf.matches("\\\\d{3}\\\\.\\\\d{3}\\\\.\\\\d{3}-\\\\d{2}")) {
            JOptionPane.showMessageDialog(this,
                "CPF deve estar no formato 000.000.000-00",
                "CPF Inválido",
                JOptionPane.WARNING_MESSAGE);
            cpfField.requestFocus();
            return false;
        }
        
        if (nome.length() < 6) {
            JOptionPane.showMessageDialog(this,
                "Nome deve ter pelo menos 6 caracteres",
                "Nome Inválido",
                JOptionPane.WARNING_MESSAGE);
            nomeField.requestFocus();
            return false;
        }
        
        if (senha.length() < 6) {
            JOptionPane.showMessageDialog(this,
                "Senha deve ter pelo menos 6 caracteres",
                "Senha Inválida",
                JOptionPane.WARNING_MESSAGE);
            senhaField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void abrirInterfacePrincipal(String cpf) {
        try {
            // Criar e exibir interface principal
            MainGUI mainGUI = new MainGUI(client, cpf);
            mainGUI.setVisible(true);
            
            // Fechar esta janela
            dispose();
            
        } catch (Exception e) {
            ErrorHandler.handleUnexpectedError(e, this, "Abrir interface principal");
        }
    }
    
    private void setFieldsEnabled(boolean enabled) {
        cpfField.setEnabled(enabled);
        senhaField.setEnabled(enabled);
        nomeField.setEnabled(enabled);
        loginButton.setEnabled(enabled);
        cadastroButton.setEnabled(enabled);
    }
    
    /**
     * Método principal para testes.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginGUI().setVisible(true);
        });
    }
}
