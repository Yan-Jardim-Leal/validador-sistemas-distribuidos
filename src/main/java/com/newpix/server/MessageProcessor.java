package com.newpix.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.newpix.model.Transacao;
import com.newpix.model.Usuario;
import com.newpix.service.TransacaoService;
import com.newpix.service.UsuarioService;
import validador.Validator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Processador de mensagens do protocolo NewPix.
 */
public class MessageProcessor {
    
    private final UsuarioService usuarioService;
    private final TransacaoService transacaoService;
    private final ObjectMapper objectMapper;
    
    public MessageProcessor() {
        this.usuarioService = new UsuarioService();
        this.transacaoService = new TransacaoService();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Processa uma mensagem JSON e retorna a resposta.
     */
    public String processMessage(String jsonMessage) {
        try {
            // Validar mensagem do cliente
            Validator.validateClient(jsonMessage);
            
            JsonNode rootNode = objectMapper.readTree(jsonMessage);
            String operacao = rootNode.get("operacao").asText();
            
            return switch (operacao) {
                case "usuario_login" -> processUsuarioLogin(rootNode);
                case "usuario_logout" -> processUsuarioLogout(rootNode);
                case "usuario_criar" -> processUsuarioCriar(rootNode);
                case "usuario_ler" -> processUsuarioLer(rootNode);
                case "usuario_atualizar" -> processUsuarioAtualizar(rootNode);
                case "usuario_deletar" -> processUsuarioDeletar(rootNode);
                case "transacao_criar" -> processTransacaoCriar(rootNode);
                case "transacao_ler" -> processTransacaoLer(rootNode);
                default -> createErrorResponse(operacao, "Operacao nao suportada");
            };
            
        } catch (Exception e) {
            System.err.println("Erro ao processar mensagem: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse("unknown", "Erro interno do servidor: " + e.getMessage());
        }
    }
    
    private String processUsuarioLogin(JsonNode rootNode) {
        try {
            String cpf = rootNode.get("cpf").asText();
            String senha = rootNode.get("senha").asText();
            
            String token = usuarioService.login(cpf, senha);
            
            ObjectNode response = objectMapper.createObjectNode();
            response.put("operacao", "usuario_login");
            
            if (token != null) {
                response.put("token", token);
                response.put("status", true);
                response.put("info", "Login bem-sucedido.");
            } else {
                response.put("status", false);
                response.put("info", "Credenciais invalidas.");
            }
            
            return objectMapper.writeValueAsString(response);
            
        } catch (Exception e) {
            return createErrorResponse("usuario_login", "Erro ao realizar login: " + e.getMessage());
        }
    }
    
    private String processUsuarioLogout(JsonNode rootNode) {
        try {
            String token = rootNode.get("token").asText();
            
            boolean sucesso = usuarioService.logout(token);
            
            ObjectNode response = objectMapper.createObjectNode();
            response.put("operacao", "usuario_logout");
            response.put("status", sucesso);
            response.put("info", sucesso ? "Logout realizado com sucesso." : "Erro ao realizar logout.");
            
            return objectMapper.writeValueAsString(response);
            
        } catch (Exception e) {
            return createErrorResponse("usuario_logout", "Erro ao realizar logout: " + e.getMessage());
        }
    }
    
    private String processUsuarioCriar(JsonNode rootNode) {
        try {
            String cpf = rootNode.get("cpf").asText();
            String nome = rootNode.get("nome").asText();
            String senha = rootNode.get("senha").asText();
            
            boolean sucesso = usuarioService.criarUsuario(cpf, nome, senha);
            
            ObjectNode response = objectMapper.createObjectNode();
            response.put("operacao", "usuario_criar");
            response.put("status", sucesso);
            response.put("info", sucesso ? "Usuario criado com sucesso." : "Erro ao criar usuario.");
            
            return objectMapper.writeValueAsString(response);
            
        } catch (Exception e) {
            return createErrorResponse("usuario_criar", "Erro ao criar usuario: " + e.getMessage());
        }
    }
    
    private String processUsuarioLer(JsonNode rootNode) {
        try {
            String token = rootNode.get("token").asText();
            
            Usuario usuario = usuarioService.buscarUsuarioPorToken(token);
            
            ObjectNode response = objectMapper.createObjectNode();
            response.put("operacao", "usuario_ler");
            
            if (usuario != null) {
                response.put("status", true);
                response.put("info", "Dados do usuario recuperados com sucesso.");
                
                ObjectNode usuarioNode = objectMapper.createObjectNode();
                usuarioNode.put("cpf", usuario.getCpf());
                usuarioNode.put("nome", usuario.getNome());
                usuarioNode.put("saldo", usuario.getSaldo());
                
                response.set("usuario", usuarioNode);
            } else {
                response.put("status", false);
                response.put("info", "Erro ao ler dados do usuario.");
            }
            
            return objectMapper.writeValueAsString(response);
            
        } catch (Exception e) {
            return createErrorResponse("usuario_ler", "Erro ao ler usuario: " + e.getMessage());
        }
    }
    
    private String processUsuarioAtualizar(JsonNode rootNode) {
        try {
            String token = rootNode.get("token").asText();
            JsonNode usuarioNode = rootNode.get("usuario");
            
            String novoNome = usuarioNode.has("nome") ? usuarioNode.get("nome").asText() : null;
            String novaSenha = usuarioNode.has("senha") ? usuarioNode.get("senha").asText() : null;
            
            boolean sucesso = usuarioService.atualizarUsuario(token, novoNome, novaSenha);
            
            ObjectNode response = objectMapper.createObjectNode();
            response.put("operacao", "usuario_atualizar");
            response.put("status", sucesso);
            response.put("info", sucesso ? "Usuario atualizado com sucesso." : "Erro ao atualizar usuario.");
            
            return objectMapper.writeValueAsString(response);
            
        } catch (Exception e) {
            return createErrorResponse("usuario_atualizar", "Erro ao atualizar usuario: " + e.getMessage());
        }
    }
    
    private String processUsuarioDeletar(JsonNode rootNode) {
        try {
            String token = rootNode.get("token").asText();
            
            boolean sucesso = usuarioService.deletarUsuario(token);
            
            ObjectNode response = objectMapper.createObjectNode();
            response.put("operacao", "usuario_deletar");
            response.put("status", sucesso);
            response.put("info", sucesso ? "Usuario deletado com sucesso." : "Erro ao deletar usuario.");
            
            return objectMapper.writeValueAsString(response);
            
        } catch (Exception e) {
            return createErrorResponse("usuario_deletar", "Erro ao deletar usuario: " + e.getMessage());
        }
    }
    
    private String processTransacaoCriar(JsonNode rootNode) {
        try {
            String token = rootNode.get("token").asText();
            double valor = rootNode.get("valor").asDouble();
            String cpfDestino = rootNode.get("cpf_destino").asText();
            
            boolean sucesso = transacaoService.criarTransacao(token, valor, cpfDestino);
            
            ObjectNode response = objectMapper.createObjectNode();
            response.put("operacao", "transacao_criar");
            response.put("status", sucesso);
            response.put("info", sucesso ? "Transacao realizada com sucesso." : "Erro ao criar transacao.");
            
            return objectMapper.writeValueAsString(response);
            
        } catch (Exception e) {
            return createErrorResponse("transacao_criar", "Erro ao criar transacao: " + e.getMessage());
        }
    }
    
    private String processTransacaoLer(JsonNode rootNode) {
        try {
            String token = rootNode.get("token").asText();
            String dataInicialStr = rootNode.get("data_inicial").asText();
            String dataFinalStr = rootNode.get("data_final").asText();
            
            LocalDateTime dataInicial = LocalDateTime.parse(dataInicialStr, DateTimeFormatter.ISO_DATE_TIME);
            LocalDateTime dataFinal = LocalDateTime.parse(dataFinalStr, DateTimeFormatter.ISO_DATE_TIME);
            
            List<Transacao> transacoes = transacaoService.buscarTransacoesPorPeriodo(token, dataInicial, dataFinal);
            
            ObjectNode response = objectMapper.createObjectNode();
            response.put("operacao", "transacao_ler");
            
            if (transacoes != null) {
                response.put("status", true);
                response.put("info", "Transacoes recuperadas com sucesso.");
                response.set("transacoes", objectMapper.valueToTree(transacoes));
            } else {
                response.put("status", false);
                response.put("info", "Erro ao ler transacoes.");
            }
            
            return objectMapper.writeValueAsString(response);
            
        } catch (DateTimeParseException e) {
            return createErrorResponse("transacao_ler", "Formato de data invalido.");
        } catch (Exception e) {
            return createErrorResponse("transacao_ler", "Erro ao ler transacoes: " + e.getMessage());
        }
    }
    
    private String createErrorResponse(String operacao, String mensagem) {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("operacao", operacao);
            response.put("status", false);
            response.put("info", mensagem);
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return "{\"operacao\":\"" + operacao + "\",\"status\":false,\"info\":\"Erro interno do servidor\"}";
        }
    }
}
