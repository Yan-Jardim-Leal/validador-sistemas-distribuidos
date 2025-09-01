package validador;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Validator {

    private Validator() {}

    // ObjectMapper é a classe principal do Jackson para converter JSON.
    // É uma boa prática reutilizar a mesma instância.
    private static final ObjectMapper mapper = new ObjectMapper();

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
        RulesEnum operacao = RulesEnum.getEnum(operacaoNode.asText());

        // Chama o método de validação específico para a operação
        switch (operacao) {
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
                // Outras operações de sucesso não retornam dados adicionais, então não precisam de validação extra.
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
        validateCpfFormat(node, "cpf");
        getRequiredNumber(node, "valor");
    }

    private static void validateTransacaoLerClient(JsonNode node) {
        validateStringLength(node, "token", 3, 200);
        getRequiredInt(node, "pagina");
        getRequiredInt(node, "limite");
    }

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
}