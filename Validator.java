package br.com.banco.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class Validator {

    private Validator() {}

    // ObjectMapper é a classe principal do Jackson para converter JSON.
    // É uma boa prática reutilizar a mesma instância.
    private static final ObjectMapper mapper = new ObjectMapper();
    // --- DEFINIÇÃO DAS CHAVES ESPERADAS ---

    private static final Map<RulesEnum, Set<String>> EXPECTED_CLIENT_KEYS = new HashMap<>();
    private static final Map<RulesEnum, Set<String>> EXPECTED_SERVER_KEYS = new HashMap<>();

    // Bloco estático para inicializar os mapas
    static {
        // Cliente -> Servidor
        EXPECTED_CLIENT_KEYS.put(RulesEnum.CONECTAR, Set.of("operacao"));
        EXPECTED_CLIENT_KEYS.put(RulesEnum.USUARIO_LOGIN, Set.of("operacao", "cpf", "senha"));
        EXPECTED_CLIENT_KEYS.put(RulesEnum.USUARIO_CRIAR, Set.of("operacao", "nome", "cpf", "senha"));
        EXPECTED_CLIENT_KEYS.put(RulesEnum.USUARIO_LER, Set.of("operacao", "token"));
        EXPECTED_CLIENT_KEYS.put(RulesEnum.USUARIO_ATUALIZAR, Set.of("operacao", "token", "usuario"));
        EXPECTED_CLIENT_KEYS.put(RulesEnum.USUARIO_DELETAR, Set.of("operacao", "token"));
        EXPECTED_CLIENT_KEYS.put(RulesEnum.USUARIO_LOGOUT, Set.of("operacao", "token"));
        EXPECTED_CLIENT_KEYS.put(RulesEnum.TRANSACAO_CRIAR, Set.of("operacao", "token", "valor", "cpf_destino"));
        EXPECTED_CLIENT_KEYS.put(RulesEnum.TRANSACAO_LER, Set.of("operacao", "token", "data_inicial", "data_final"));
        EXPECTED_CLIENT_KEYS.put(RulesEnum.DEPOSITAR, Set.of("operacao", "token", "valor_enviado"));
        EXPECTED_CLIENT_KEYS.put(RulesEnum.ERRO_SERVIDOR, Set.of("operacao", "operacao_enviada", "info"));

        // Servidor -> Cliente (Respostas)
        // Chaves base para todas as respostas
        Set<String> serverBaseKeys = Set.of("operacao", "status", "info");
        // Respostas de sucesso que contêm dados adicionais
        EXPECTED_SERVER_KEYS.put(RulesEnum.USUARIO_LOGIN, Set.of("operacao", "status", "info", "token"));
        EXPECTED_SERVER_KEYS.put(RulesEnum.USUARIO_LER, Set.of("operacao", "status", "info", "usuario"));
        EXPECTED_SERVER_KEYS.put(RulesEnum.TRANSACAO_LER, Set.of("operacao", "status", "info", "transacoes"));
        
        // Para as demais operações, a resposta (sucesso ou falha) só contém as chaves base.
        for (RulesEnum rule : RulesEnum.values()) {
            EXPECTED_CLIENT_KEYS.computeIfAbsent(rule, k -> new HashSet<>()); // Garante que não haja nulls
            // Se a regra já não tiver chaves específicas de servidor, usa as chaves base
            EXPECTED_SERVER_KEYS.computeIfAbsent(rule, k -> serverBaseKeys);
        }
    }

    /**
     * Valida uma mensagem JSON enviada do Cliente para o Servidor.
     *
     * @param jsonString A mensagem JSON como uma String.
     * @throws Exception se o JSON for inválido ou não seguir o protocolo.
     */
    public static void validateClient(String jsonString) throws Exception {
        JsonNode rootNode = parseJson(jsonString);

        // Valida a presença e o tipo do campo 'operacao'
        JsonNode operacaoNode = getRequiredField(rootNode, "operacao");
        validateStringLength(rootNode, "operacao", 3, 200); // Operacao também é uma string

        // Converte a string da operação para o nosso Enum
        // NOTA: Certifique-se de que o RulesEnum.java contenha a operação DEPOSITAR.
        RulesEnum operacao = RulesEnum.getEnum(operacaoNode.asText());

        checkExtraKeys(rootNode, operacao, EXPECTED_CLIENT_KEYS);

        // Chama o método de validação específico para a operação
        switch (operacao) {
            case CONECTAR:
                break;
            case USUARIO_LOGIN:
                validateUsuarioLoginClient(rootNode);
                break;
            case USUARIO_LOGOUT:
                validateUsuarioLogoutClient(rootNode);
                break;
            case USUARIO_CRIAR:
                validateUsuarioCriarClient(rootNode);
                break;
            case USUARIO_LER:
                validateUsuarioLerClient(rootNode);
                break;
            case USUARIO_ATUALIZAR:
                validateUsuarioAtualizarClient(rootNode);
                break;
            case USUARIO_DELETAR:
                validateUsuarioDeletarClient(rootNode);
                break;
            case TRANSACAO_CRIAR:
                validateTransacaoCriarClient(rootNode);
                break;
            case TRANSACAO_LER:
                validateTransacaoLerClient(rootNode);
                break;
            case DEPOSITAR:
                validateDepositarClient(rootNode);
                break;
            case ERRO_SERVIDOR:
                validateErroServidorClient(rootNode);
                break;
            // =======================================================
            default:
                throw new IllegalArgumentException("Operação do cliente desconhecida ou não suportada: " + operacao);
        }
    }

    /**
     * Valida uma mensagem JSON enviada do Servidor para o Cliente.
     *
     * @param jsonString A mensagem JSON como uma String.
     * @throws Exception se o JSON for inválido ou não seguir o protocolo.
     */
    public static void validateServer(String jsonString) throws Exception {
        JsonNode rootNode = parseJson(jsonString);

        // Toda resposta do servidor deve ter 'operacao', 'status' e 'info'
        JsonNode operacaoNode = getRequiredField(rootNode, "operacao");
        validateStringLength(rootNode, "operacao", 3, 200);
        
        JsonNode statusNode = getRequiredField(rootNode, "status");
        if (!statusNode.isBoolean()) {
            throw new IllegalArgumentException("O campo 'status' na resposta do servidor deve ser um booleano (true/false).");
        }

        validateStringLength(rootNode, "info", 3, 200);

        RulesEnum operacao = RulesEnum.getEnum(operacaoNode.asText());

        Set<String> expectedKeysForThisResponse;
        if (statusNode.asBoolean()) {
            expectedKeysForThisResponse = EXPECTED_SERVER_KEYS.get(operacao);
            if (expectedKeysForThisResponse == null) { // Segurança extra
                throw new IllegalArgumentException("Definição de chaves não encontrada para operação de sucesso: " + operacao);
            }
        } else {
            expectedKeysForThisResponse = Set.of("operacao", "status", "info");
        }

        checkExtraKeys(rootNode, operacao, Map.of(operacao, expectedKeysForThisResponse));

        // Chama a validação específica apenas se o status for true (sucesso)
        if (statusNode.asBoolean()) {
            switch (operacao) {
                case USUARIO_LOGIN:
                    validateUsuarioLoginServer(rootNode);
                    break;
                case USUARIO_LER:
                    validateUsuarioLerServer(rootNode);
                    break;
                case TRANSACAO_LER:
                    validateTransacaoLerServer(rootNode);
                    break;
                // Outras operações de sucesso (como criar, atualizar, deletar e depositar)
                // não retornam dados adicionais, então não precisam de validação extra.
                default:
                    break; 
            }
        }
    }

    // ===================================================================================
    // MÉTODOS DE VALIDAÇÃO PRIVADOS (CLIENTE -> SERVIDOR)
    // ===================================================================================

    private static void validateUsuarioLoginClient(JsonNode node) {
        validateCpfFormat(node, "cpf");
        validateStringLength(node, "senha", 6, 120);
    }

    private static void validateUsuarioLogoutClient(JsonNode node) {
        validateStringLength(node, "token", 3, 200);
    }

    private static void validateUsuarioCriarClient(JsonNode node) {
        validateStringLength(node, "nome", 6, 120);
        validateCpfFormat(node, "cpf");
        validateStringLength(node, "senha", 6, 120);
    }

    private static void validateUsuarioLerClient(JsonNode node) {
        validateStringLength(node, "token", 3, 200);
    }

    private static void validateUsuarioAtualizarClient(JsonNode node) {
        validateStringLength(node, "token", 3, 200);
        JsonNode usuarioNode = getRequiredObject(node, "usuario");
        
        if (!usuarioNode.has("nome") && !usuarioNode.has("senha")) {
            throw new IllegalArgumentException("O objeto 'usuario' para atualização deve conter pelo menos o campo 'nome' ou 'senha'.");
        }
        if (usuarioNode.has("nome")){
            validateStringLength(usuarioNode, "nome", 6, 120);
        }
        if (usuarioNode.has("senha")){
            validateStringLength(usuarioNode, "senha", 6, 120);
        }
    }

    private static void validateUsuarioDeletarClient(JsonNode node) {
        validateStringLength(node, "token", 3, 200);
    }

    private static void validateTransacaoCriarClient(JsonNode node) {
        validateStringLength(node, "token", 3, 200);
        validateCpfFormat(node, "cpf_destino");
        getRequiredNumber(node, "valor");
    }

    private static void validateTransacaoLerClient(JsonNode node) {
        validateStringLength(node, "token", 3, 200);
        validateDateFormat(node, "data_inicial"); 
        validateDateFormat(node, "data_final");   
    }

    private static void validateDepositarClient(JsonNode node) {
        validateStringLength(node, "token", 3, 200);
        getRequiredNumber(node, "valor_enviado");
    }

    private static void validateErroServidorClient(JsonNode node) {
        getRequiredField(node, "operacao");
        getRequiredField(node, "operacao_enviada");
        getRequiredField(node, "info");
    }
    // =======================================================

    // ===================================================================================
    // MÉTODOS DE VALIDAÇÃO PRIVADOS (SERVIDOR -> CLIENTE)
    // ===================================================================================

    private static void validateUsuarioLoginServer(JsonNode node) {
        validateStringLength(node, "token", 3, 200);
    }

    private static void validateUsuarioLerServer(JsonNode node) {
        JsonNode usuarioNode = getRequiredObject(node, "usuario");
        validateCpfFormat(usuarioNode, "cpf");
        validateStringLength(usuarioNode, "nome", 6, 120);
        getRequiredNumber(usuarioNode, "saldo");
        if (usuarioNode.has("senha")) {
            throw new IllegalArgumentException("A resposta do servidor para 'usuario_ler' não deve conter o campo 'senha'.");
        }
    }
    
    private static void validateTransacaoLerServer(JsonNode node) {
        JsonNode transacoesNode = getRequiredArray(node, "transacoes");
        for (JsonNode transacao : transacoesNode) {
            getRequiredInt(transacao, "id");
            getRequiredNumber(transacao, "valor_enviado");
            
            JsonNode enviadorNode = getRequiredObject(transacao, "usuario_enviador");
            validateStringLength(enviadorNode, "nome", 6, 120);
            validateCpfFormat(enviadorNode, "cpf");
            
            JsonNode recebedorNode = getRequiredObject(transacao, "usuario_recebedor");
            validateStringLength(recebedorNode, "nome", 6, 120);
            validateCpfFormat(recebedorNode, "cpf");

            validateDateFormat(transacao, "criado_em");
            validateDateFormat(transacao, "atualizado_em");
        }
    }

    // ===================================================================================
    // MÉTODOS AUXILIARES (HELPERS)
    // ===================================================================================

    private static JsonNode parseJson(String jsonString) throws Exception {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            throw new Exception("A mensagem JSON não pode ser nula ou vazia.");
        }
        try {
            return mapper.readTree(jsonString);
        } catch (Exception e) {
            throw new Exception("Erro de sintaxe. A mensagem não é um JSON válido.", e);
        }
    }

    private static JsonNode getRequiredField(JsonNode parentNode, String fieldName) {
        if (parentNode.has(fieldName) && !parentNode.get(fieldName).isNull()) {
            return parentNode.get(fieldName);
        }
        throw new IllegalArgumentException("O campo obrigatório '" + fieldName + "' não foi encontrado ou é nulo.");
    }

    private static void validateStringLength(JsonNode parentNode, String fieldName, int minLength, int maxLength) {
        JsonNode field = getRequiredField(parentNode, fieldName);
        if (!field.isTextual()) {
            throw new IllegalArgumentException("O campo '" + fieldName + "' deve ser do tipo String.");
        }
        
        String value = field.asText().trim();
        
        if (value.length() < minLength) {
            throw new IllegalArgumentException("O campo '" + fieldName + "' deve ter no mínimo " + minLength + " caracteres.");
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException("O campo '" + fieldName + "' deve ter no máximo " + maxLength + " caracteres.");
        }
    }

    private static void validateCpfFormat(JsonNode parentNode, String fieldName) {
        JsonNode field = getRequiredField(parentNode, fieldName);
        if (!field.isTextual()) {
            throw new IllegalArgumentException("O campo '" + fieldName + "' deve ser do tipo String.");
        }
        String cpf = field.asText();
        String cpfRegex = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}";
        if (!cpf.matches(cpfRegex)) {
            throw new IllegalArgumentException("O campo '" + fieldName + "' deve estar no formato '000.000.000-00'.");
        }
    }

    private static void validateDateFormat(JsonNode parentNode, String fieldName) {
        JsonNode field = getRequiredField(parentNode, fieldName);
        if (!field.isTextual()) {
            throw new IllegalArgumentException("O campo '" + fieldName + "' deve ser do tipo String.");
        }
        String date = field.asText();
        String isoRegex = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z";
        if (!date.matches(isoRegex)) {
            throw new IllegalArgumentException("O campo '" + fieldName + "' deve estar no formato ISO 8601 UTC 'yyyy-MM-dd'T'HH:mm:ss'Z'.");
        }
    }
    
    private static void getRequiredNumber(JsonNode parentNode, String fieldName) {
        JsonNode field = getRequiredField(parentNode, fieldName);
        if (!field.isNumber()) {
            throw new IllegalArgumentException("O campo '" + fieldName + "' deve ser do tipo numérico (int, double, etc).");
        }
    }

    private static void getRequiredInt(JsonNode parentNode, String fieldName) {
        JsonNode field = getRequiredField(parentNode, fieldName);
        if (!field.isInt()) {
            throw new IllegalArgumentException("O campo '" + fieldName + "' deve ser do tipo int.");
        }
    }

    private static JsonNode getRequiredObject(JsonNode parentNode, String fieldName) {
        JsonNode field = getRequiredField(parentNode, fieldName);
        if (!field.isObject()) {
            throw new IllegalArgumentException("O campo '" + fieldName + "' deve ser um objeto JSON (ex: { ... }).");
        }
        return field;
    }
    
    private static JsonNode getRequiredArray(JsonNode parentNode, String fieldName) {
        JsonNode field = getRequiredField(parentNode, fieldName);
        if (!field.isArray()) {
            throw new IllegalArgumentException("O campo '" + fieldName + "' deve ser um array JSON (ex: [ ... ]).");
        }
        return field;
    }

    private static void checkExtraKeys(JsonNode node, RulesEnum operacao, Map<RulesEnum, Set<String>> expectedKeysMap) {
        Set<String> expected = expectedKeysMap.get(operacao);
        if (expected == null) {
            throw new IllegalArgumentException("Definição de chaves esperadas não encontrada para a operação: " + operacao);
        }
        
        Iterator<String> actualKeys = node.fieldNames();
        while (actualKeys.hasNext()) {
            String key = actualKeys.next();
            if (!expected.contains(key)) {
                throw new IllegalArgumentException("Chave inesperada '" + key + "' encontrada para a operação '" + operacao + "'.");
            }
        }
    }
}