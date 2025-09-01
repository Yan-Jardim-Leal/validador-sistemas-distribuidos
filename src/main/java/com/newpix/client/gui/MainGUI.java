package com.newpix.client.gui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newpix.client.NewPixClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Interface gráfica principal do cliente NewPix.
 */
public class MainGUI extends JFrame {
    
    private NewPixClient client;
    private String token;
    private String nomeUsuario;
    private String cpfUsuario;
    private double saldoUsuario;
    
    // Componentes da interface
    private JLabel nomeLabel;
    private JLabel cpfLabel;
    private JLabel saldoLabel;
    private JTextField valorPixField;
    private JTextField cpfDestinoField;
    private JButton pixButton;
    private JButton atualizarButton;
    private JButton extratoButton;
    private JButton logoutButton;
    private JTable extratoTable;
    private DefaultTableModel tableModel;
    private JTextField novoNomeField;
    private JPasswordField novaSenhaField;
    
    private DecimalFormat currencyFormat = new DecimalFormat("R$ #,##0.00");
    
    public MainGUI(NewPixClient client, String token) {
        this.client = client;
        this.token = token;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        carregarDadosUsuario();
    }
    
    private void initializeComponents() {
        setTitle("NewPix - Sistema Bancário");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        
        // Labels de informação do usuário
        nomeLabel = new JLabel("Nome: Carregando...");
        cpfLabel = new JLabel("CPF: Carregando...");
        saldoLabel = new JLabel("Saldo: Carregando...");
        saldoLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        saldoLabel.setForeground(new Color(0, 150, 0));
        
        // Campos para PIX
        valorPixField = new JTextField(15);
        cpfDestinoField = new JTextField(20);
        pixButton = new JButton("Enviar PIX");
        pixButton.setBackground(new Color(50, 150, 250));
        pixButton.setForeground(Color.WHITE);
        
        // Campos para atualização de dados
        novoNomeField = new JTextField(20);
        novaSenhaField = new JPasswordField(20);
        atualizarButton = new JButton("Atualizar Dados");
        
        // Botões de ação
        extratoButton = new JButton("Atualizar Extrato");
        logoutButton = new JButton("Logout");
        
        // Tabela de extrato
        String[] columns = {"Data", "Tipo", "Valor", "Origem/Destino"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        extratoTable = new JTable(tableModel);
        extratoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel superior - informações do usuário
        JPanel userPanel = new JPanel(new GridBagLayout());
        userPanel.setBorder(BorderFactory.createTitledBorder("Dados do Usuário"));
        userPanel.setBackground(new Color(240, 248, 255));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        userPanel.add(nomeLabel, gbc);
        gbc.gridx = 1;
        userPanel.add(cpfLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        userPanel.add(saldoLabel, gbc);
        
        // Panel central - operações
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Aba PIX
        JPanel pixPanel = createPixPanel();
        tabbedPane.addTab("Enviar PIX", pixPanel);
        
        // Aba Extrato
        JPanel extratoPanel = createExtratoPanel();
        tabbedPane.addTab("Extrato", extratoPanel);
        
        // Aba Configurações
        JPanel configPanel = createConfigPanel();
        tabbedPane.addTab("Configurações", configPanel);
        
        // Panel inferior - botões de ação
        JPanel actionPanel = new JPanel(new FlowLayout());
        actionPanel.add(extratoButton);
        actionPanel.add(logoutButton);
        
        add(userPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createPixPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Valor (R$):"), gbc);
        gbc.gridx = 1;
        panel.add(valorPixField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("CPF Destino:"), gbc);
        gbc.gridx = 1;
        panel.add(cpfDestinoField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(pixButton, gbc);
        
        // Área de instruções
        gbc.gridy = 3;
        JTextArea instructions = new JTextArea(
            "Instruções:\n" +
            "1. Digite o valor a ser enviado\n" +
            "2. Informe o CPF do destinatário\n" +
            "3. Clique em 'Enviar PIX'\n\n" +
            "Formato do CPF: 000.000.000-00"
        );
        instructions.setEditable(false);
        instructions.setBackground(panel.getBackground());
        instructions.setBorder(BorderFactory.createTitledBorder("Instruções"));
        panel.add(instructions, gbc);
        
        return panel;
    }
    
    private JPanel createExtratoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JScrollPane scrollPane = new JScrollPane(extratoTable);
        scrollPane.setPreferredSize(new Dimension(800, 300));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshButton = new JButton("Atualizar");
        refreshButton.addActionListener(e -> carregarExtrato());
        buttonPanel.add(refreshButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Novo Nome:"), gbc);
        gbc.gridx = 1;
        panel.add(novoNomeField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Nova Senha:"), gbc);
        gbc.gridx = 1;
        panel.add(novaSenhaField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(atualizarButton, gbc);
        
        // Aviso
        gbc.gridy = 3;
        JTextArea warning = new JTextArea(
            "Atenção:\n" +
            "• Deixe em branco os campos que não deseja alterar\n" +
            "• A senha deve ter entre 6 e 120 caracteres\n" +
            "• O nome deve ter entre 6 e 120 caracteres"
        );
        warning.setEditable(false);
        warning.setBackground(panel.getBackground());
        warning.setBorder(BorderFactory.createTitledBorder("Aviso"));
        panel.add(warning, gbc);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        pixButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarPix();
            }
        });
        
        atualizarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                atualizarDados();
            }
        });
        
        extratoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                carregarDadosUsuario();
                carregarExtrato();
            }
        });
        
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                realizarLogout();
            }
        });
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                realizarLogout();
            }
        });
    }
    
    private void carregarDadosUsuario() {
        NewPixClient.UserDataResult result = client.lerUsuario(token);
        
        if (result.isSuccess()) {
            nomeUsuario = result.getNome();
            cpfUsuario = result.getCpf();
            saldoUsuario = result.getSaldo();
            
            nomeLabel.setText("Nome: " + nomeUsuario);
            cpfLabel.setText("CPF: " + cpfUsuario);
            saldoLabel.setText("Saldo: " + currencyFormat.format(saldoUsuario));
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados: " + result.getMessage(),
                                        "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void enviarPix() {
        try {
            String valorStr = valorPixField.getText().trim().replace(",", ".");
            String cpfDestino = cpfDestinoField.getText().trim();
            
            if (valorStr.isEmpty() || cpfDestino.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Todos os campos são obrigatórios!",
                                            "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            double valor = Double.parseDouble(valorStr);
            
            if (valor <= 0) {
                JOptionPane.showMessageDialog(this, "Valor deve ser positivo!",
                                            "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            NewPixClient.OperationResult result = client.criarTransacao(token, valor, cpfDestino);
            
            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, "PIX enviado com sucesso!",
                                            "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                valorPixField.setText("");
                cpfDestinoField.setText("");
                carregarDadosUsuario(); // Atualizar saldo
                carregarExtrato(); // Atualizar extrato
            } else {
                JOptionPane.showMessageDialog(this, result.getMessage(),
                                            "Erro", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Valor inválido!",
                                        "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void atualizarDados() {
        String novoNome = novoNomeField.getText().trim();
        String novaSenha = new String(novaSenhaField.getPassword());
        
        if (novoNome.isEmpty() && novaSenha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe pelo menos um campo para atualizar!",
                                        "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        NewPixClient.OperationResult result = client.atualizarUsuario(token, 
                                                                     novoNome.isEmpty() ? null : novoNome,
                                                                     novaSenha.isEmpty() ? null : novaSenha);
        
        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, "Dados atualizados com sucesso!",
                                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            novoNomeField.setText("");
            novaSenhaField.setText("");
            carregarDadosUsuario(); // Atualizar dados exibidos
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(),
                                        "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void carregarExtrato() {
        // Buscar transações dos últimos 30 dias
        LocalDateTime dataFinal = LocalDateTime.now();
        LocalDateTime dataInicial = dataFinal.minusDays(30);
        
        String dataInicialStr = dataInicial.format(DateTimeFormatter.ISO_DATE_TIME);
        String dataFinalStr = dataFinal.format(DateTimeFormatter.ISO_DATE_TIME);
        
        NewPixClient.TransactionResult result = client.lerTransacoes(token, dataInicialStr, dataFinalStr);
        
        if (result.isSuccess() && result.getTransacoes() != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode transacoes = mapper.readTree(result.getTransacoes());
                
                // Limpar tabela
                tableModel.setRowCount(0);
                
                // Adicionar transações à tabela
                for (JsonNode transacao : transacoes) {
                    String data = transacao.get("criado_em").asText();
                    double valor = transacao.get("valor_enviado").asDouble();
                    
                    JsonNode enviador = transacao.get("usuario_enviador");
                    JsonNode recebedor = transacao.get("usuario_recebedor");
                    
                    String cpfEnviador = enviador.get("cpf").asText();
                    String nomeEnviador = enviador.get("nome").asText();
                    String cpfRecebedor = recebedor.get("cpf").asText();
                    String nomeRecebedor = recebedor.get("nome").asText();
                    
                    String tipo;
                    String origem_destino;
                    
                    if (cpfEnviador.equals(cpfUsuario)) {
                        tipo = "Enviado";
                        origem_destino = nomeRecebedor + " (" + cpfRecebedor + ")";
                    } else {
                        tipo = "Recebido";
                        origem_destino = nomeEnviador + " (" + cpfEnviador + ")";
                    }
                    
                    String dataFormatada = LocalDateTime.parse(data, DateTimeFormatter.ISO_DATE_TIME)
                                          .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                    
                    tableModel.addRow(new Object[]{
                        dataFormatada,
                        tipo,
                        currencyFormat.format(valor),
                        origem_destino
                    });
                }
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao processar extrato: " + e.getMessage(),
                                            "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            tableModel.setRowCount(0);
        }
    }
    
    private void realizarLogout() {
        int option = JOptionPane.showConfirmDialog(this, "Deseja realmente sair?", 
                                                  "Confirmar Logout", JOptionPane.YES_NO_OPTION);
        
        if (option == JOptionPane.YES_OPTION) {
            client.logout(token);
            client.disconnect();
            
            // Voltar para tela de login
            LoginGUI loginGUI = new LoginGUI();
            loginGUI.setVisible(true);
            this.dispose();
        }
    }
}
