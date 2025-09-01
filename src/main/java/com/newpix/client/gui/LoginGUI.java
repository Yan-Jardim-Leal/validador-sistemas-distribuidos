package com.newpix.client.gui;

import com.newpix.client.NewPixClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Interface gráfica para login e cadastro de usuários.
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
    
    public LoginGUI() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        setTitle("NewPix - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Campos de conexão
        hostField = new JTextField("localhost", 15);
        portField = new JTextField("8080", 8);
        conectarButton = new JButton("Conectar");
        
        // Campos de login/cadastro
        cpfField = new JTextField(20);
        senhaField = new JPasswordField(20);
        nomeField = new JTextField(20);
        
        loginButton = new JButton("Login");
        cadastroButton = new JButton("Cadastrar");
        
        statusLabel = new JLabel("Desconectado");
        statusLabel.setForeground(Color.RED);
        
        // Desabilitar campos até conectar
        setFieldsEnabled(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
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
            "NewPix - Sistema Bancário Distribuído\n" +
            "1. Conecte-se ao servidor\n" +
            "2. Faça login ou cadastre-se\n" +
            "3. Realize transações PIX"
        );
        infoArea.setEditable(false);
        infoArea.setBackground(getBackground());
        infoPanel.add(infoArea);
        
        add(infoPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        conectarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                conectarServidor();
            }
        });
        
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                realizarLogin();
            }
        });
        
        cadastroButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                realizarCadastro();
            }
        });
    }
    
    private void conectarServidor() {
        try {
            String host = hostField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());
            
            client = new NewPixClient(host, port);
            
            if (client.connect()) {
                statusLabel.setText("Conectado");
                statusLabel.setForeground(Color.GREEN);
                conectarButton.setText("Desconectar");
                setFieldsEnabled(true);
                
                // Atualizar action do botão conectar
                for (ActionListener al : conectarButton.getActionListeners()) {
                    conectarButton.removeActionListener(al);
                }
                conectarButton.addActionListener(e -> desconectarServidor());
                
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao conectar ao servidor!", 
                                            "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Porta inválida!", 
                                        "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void desconectarServidor() {
        if (client != null) {
            client.disconnect();
        }
        
        statusLabel.setText("Desconectado");
        statusLabel.setForeground(Color.RED);
        conectarButton.setText("Conectar");
        setFieldsEnabled(false);
        
        // Atualizar action do botão conectar
        for (ActionListener al : conectarButton.getActionListeners()) {
            conectarButton.removeActionListener(al);
        }
        conectarButton.addActionListener(e -> conectarServidor());
    }
    
    private void realizarLogin() {
        String cpf = cpfField.getText().trim();
        String senha = new String(senhaField.getPassword());
        
        if (cpf.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "CPF e senha são obrigatórios!", 
                                        "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        NewPixClient.LoginResult result = client.login(cpf, senha);
        
        if (result.isSuccess()) {
            // Abrir tela principal
            MainGUI mainGUI = new MainGUI(client, result.getToken());
            mainGUI.setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), 
                                        "Erro de Login", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void realizarCadastro() {
        String cpf = cpfField.getText().trim();
        String nome = nomeField.getText().trim();
        String senha = new String(senhaField.getPassword());
        
        if (cpf.isEmpty() || nome.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos os campos são obrigatórios para cadastro!", 
                                        "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        NewPixClient.OperationResult result = client.criarUsuario(cpf, nome, senha);
        
        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, "Usuário cadastrado com sucesso!\nFaça login para continuar.", 
                                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            nomeField.setText("");
            senhaField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), 
                                        "Erro de Cadastro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void setFieldsEnabled(boolean enabled) {
        cpfField.setEnabled(enabled);
        senhaField.setEnabled(enabled);
        nomeField.setEnabled(enabled);
        loginButton.setEnabled(enabled);
        cadastroButton.setEnabled(enabled);
        
        hostField.setEnabled(!enabled);
        portField.setEnabled(!enabled);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginGUI().setVisible(true);
        });
    }
}
