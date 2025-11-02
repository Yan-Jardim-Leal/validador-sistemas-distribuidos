package br.com.banco.validator;

import java.util.Objects;

public enum RulesEnum {
    // Conectar
    CONECTAR("conectar"),
    // Login e Logoff
    USUARIO_LOGIN("usuario_login"),
    USUARIO_LOGOUT("usuario_logout"),
    // CRUD do usuário
    USUARIO_CRIAR("usuario_criar"),
    USUARIO_LER("usuario_ler"), // É literalmente o envio de um 'getUsuario()' para o servidor, porém os parâmetros influenciam
    USUARIO_ATUALIZAR("usuario_atualizar"),
    USUARIO_DELETAR("usuario_deletar"),
    // CR(UD) da transação
    TRANSACAO_CRIAR("transacao_criar"),
    TRANSACAO_LER("transacao_ler"), // É literalmente o envio de um 'getTransacao()' para o servidor, porém os parâmetros influenciam
    DEPOSITAR("depositar"),
    // Erro no servidor
    ERRO_SERVIDOR("erro_servidor");

    RulesEnum(String rule) {
        this.rule = rule;
    }

    private final String rule;

    public String getValue() {
        return rule;
    }

    /**
     * Busca a constante do enum correspondente ao valor da String.
     * Este método é case-insensitive (ignora maiúsculas e minúsculas).
     *
     * @param rule A string da regra a ser procurada (ex: "usuario_login").
     * @return A constante RulesEnum correspondente.
     * @throws IllegalArgumentException se nenhuma constante for encontrada para a string fornecida.
     */
    public static RulesEnum getEnum(String rule) throws Exception{
        Objects.requireNonNull(rule, "O valor da regra não pode ser nulo.");

        for (RulesEnum enumConstant : RulesEnum.values()) {
            if (enumConstant.getValue().equalsIgnoreCase(rule)) {
                return enumConstant;
            }
        }
        
        throw new IllegalArgumentException("Nenhuma regra encontrada para o valor: " + rule);
    }

}   