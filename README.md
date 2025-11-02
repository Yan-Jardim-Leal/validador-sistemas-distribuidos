
# Protocolo de Mensagens - Sistema Bancário Simples

Versão atual: 1.3

Últimos tópicos atualizados: `4.6.` `7.3.`

Um projeto para a disciplina de Sistemas Distribuídos que define um protocolo de comunicação baseado em JSON para as operações de um sistema bancário simplificado.

**Criadores:**

* Yan Jardim Leal
* Gabriel Pereira Neves

**Testers**
* Thomas Valeranovicz de Oliveira
* Rafael Adonis Menon

## 1. Pré-requisitos e Instalação

Para a correta manipulação das mensagens JSON, o projeto utiliza a biblioteca  **Jackson** . Recomendamos utilizarem Maven, é necessário adicionar as seguintes dependências ao seu projeto Java:

1.1 Links com as dependências (Caso saiba instalar sozinho)
* **Jackson Databind:**
  * [https://central.sonatype.com/artifact/com.fasterxml.jackson.core/jackson-databind/2.17.1](https://central.sonatype.com/artifact/com.fasterxml.jackson.core/jackson-databind)
* **Jackson Core:**
  * [https://central.sonatype.com/artifact/com.fasterxml.jackson.core/jackson-core/2.17.1](https://central.sonatype.com/artifact/com.fasterxml.jackson.core/jackson-core/2.17.1 "null")
* **Jackson Annotations:**
  * [https://central.sonatype.com/artifact/com.fasterxml.jackson.core/jackson-annotations/2.17.1](https://central.sonatype.com/artifact/com.fasterxml.jackson.core/jackson-annotations/2.17.1 "null")
 
1.2 Guia com as dependências (Caso não saiba instalar sozinho)
- Baixar a ferramenta [Eclipse](https://eclipseide.org/)
- Na aba de 'Criar um Projeto' selecione a opção 'Criar um projeto Maven'
- Habilite o botão "Create a simple project" e clique em next
- Preencha todas as informações requiridas (Ou peça para alguma IA caso não se sinta criativo(a))
- Ao criar, você terá um arquivo pom.xml, colar dentro de < project > COLE AQUI < /project >:
  
`<dependencies>`    
`  <dependency>`        
`    <groupId>com.fasterxml.jackson.core</groupId>`        
`    <artifactId>jackson-databind</artifactId>`        
`    <version>2.19.2</version>`    
`  </dependency>`       
`</dependencies>`

- Para instalar as dependencias de fato, você deve clicar com o botão direito em seu projeto->Maven->Update Project, isso instalará as dependências.

1.3 Possíveis Erros
- Caso esteja instalando eclipse pela primeira vez, o erro "Downloading external resources is disabled. [DownloadResourceDisabled]" pode ocorrer, para consertar basta ir em Window->Preferences->Maven->Habilite os botões que dizem "download" e "update"->Apply

1.4 Recomendações
- Utilizarem JDK a partir do 21

## 2. Como Utilizar o Validador

O repositório fornece uma classe `Validator` para garantir que as mensagens trocadas entre cliente e servidor sigam o protocolo definido.

1. Baixe o repositório do GitHub.
2. Crie uma package chamada "validador".
3. Mova as classes `Validator.java` e `RulesEnum.java` para a pasta recém criada.
4. Antes de enviar uma mensagem (ou após recebê-la), utilize os métodos estáticos da classe `Validator` para verificar sua integridade:

```
// Exemplo de uso
String jsonParaEnviar = "...";

// Valida uma mensagem que será enviada de um cliente para o servidor, a função irá converter a String em JSON e irá verificar.
Validator.validateClient(jsonParaEnviar);

// Valida uma mensagem que será enviada do servidor para um cliente, a função irá converter a String em JSON e irá verificar.
Validator.validateServer(jsonParaEnviar);

```

A função pode lançar uma exceção ou imprimir no console caso a mensagem esteja fora dos padrões definidos.

## 3. Regras de Negócio e Padrões Gerais
Essas são as regras de negócio do protocolo, não as do Sistema Bancário

### 3.1. Estrutura de Dados

O sistema opera com três entidades principais:

* **Usuário:** Os dados devem ser persistidos e respeitar às propriedades  **ACID** . Operações de CRUD completas são suportadas.
* **Transação:** Os dados devem ser persistidos e respeitar às propriedades  **ACID** . Operações de CR (Criar e Ler) são suportadas.
* **Sessão:** Os dados de sessão (tokens) podem ser mantidos em memória. Operações de CRD (Criar, Ler, Deletar) são suportadas.

### 3.2. Padrão do Campo `operacao`

Toda mensagem trocada deve conter um campo `operacao`. Em envios de mensagem ao servidor, a operação se refere à ação que será realizada pelo servidor. Em recebimentos de mensagem pelo cliente, a operação se refere à ação que seria (em mensagens erro) ou que foi (em mensagens de sucesso) realizada pelo servidor.

**Valores possíveis para `operacao`:**

* `conectar`
* `usuario_login`
* `usuario_logout`
* `usuario_criar`
* `usuario_ler`
* `usuario_atualizar`
* `usuario_deletar`
* `transacao_criar`
* `transacao_ler`
* `depositar`

### 3.3. Padrão de Resposta (`status` e `info`)

Toda resposta do servidor deve conter os campos `status` (booleano) e `info` (string) para indicar o resultado da operação.
Caso `status` retornado seja false, significa que ocorreu um erro ao processar a mensagem enviada. Caso seja true, a mensagem enviada foi processada com sucesso.

```
{
  "status": false,
  "info": "Erro ao enviar uma transação, saldo insuficiente."
}

```

### 3.4. Padrão do Token de Autenticação

O token de sessão, gerado no login e utilizado para autenticar operações subsequentes, deve ser sempre tratado como uma  **String**.<br>

## 4. Protocolo da API: Objetos e Mensagens

A seguir, a especificação detalhada para cada operação.

### 4.1. Login de Usuário (`usuario_login`)

#### Envio (Cliente → Servidor)

```
{
  "operacao": "usuario_login",
  "cpf": "123.456.789-00",
  "senha": "senhaSegura123"
}

```

#### Recebimento (Servidor → Cliente) em caso de sucesso

```
{
  "operacao": "usuario_login",
  "token": "a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8",
  "status": true,
  "info": "Login bem-sucedido."
}

```

#### Recebimento (Servidor → Cliente) em caso de falha

```
{
  "operacao": "usuario_login",
  "status": false,
  "info": "Ocorreu um erro ao realizar login."
}

```

### 4.2. Logout de Usuário (`usuario_logout`)

#### Envio (Cliente → Servidor)

```
{
  "operacao": "usuario_logout",
  "token": "a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8"
}

```

#### Recebimento (Servidor → Cliente) em caso de sucesso

```
{
  "operacao": "usuario_logout",
  "status": true,
  "info": "Logout realizado com sucesso."
}

```

#### Recebimento (Servidor → Cliente) em caso de falha

```
{
  "operacao": "usuario_logout",
  "status": false,
  "info": "Ocorreu um erro ao realizar logout."
}

```

### 4.3. Criação de Usuário (`usuario_criar`)

#### Envio (Cliente → Servidor)

```
{
  "operacao": "usuario_criar",
  "nome": "Gabriel Pereira Neves",
  "cpf": "123.456.789-00",
  "senha": "senhaSegura123"
}

```

#### Recebimento (Servidor → Cliente) em caso de sucesso

```
{
  "operacao": "usuario_criar",
  "status": true,
  "info": "Usuário criado com sucesso."
}

```

#### Recebimento (Servidor → Cliente) em caso de falha

```
{
  "operacao": "usuario_criar",
  "status": false,
  "info": "Ocorreu um erro ao criar usuário."
}

```

### 4.4. Leitura de Dados do Usuário (`usuario_ler`)

#### Envio (Cliente → Servidor)

```
{
  "operacao": "usuario_ler",
  "token": "a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8"
}

```

#### Recebimento (Servidor → Cliente) em caso de sucesso

```
{
  "operacao": "usuario_ler",
  "status": true,
  "info": "Dados do usuário recuperados com sucesso.",
  "usuario": {
    "cpf": "123.456.789-00",
    "saldo": 5430.21,
    "nome": "Gabriel Pereira Neves"
  }
}

```

#### Recebimento (Servidor → Cliente) em caso de falha

```
{
  "operacao": "usuario_ler",
  "status": false,
  "info": "Erro ao ler dados do usuário.",
}

```

### 4.5. Atualização de Dados do Usuário (`usuario_atualizar`)

*Nota: Apenas os campos a serem alterados devem ser enviados. A omissão de um campo significa que ele não deve ser modificado.*<br>
É muito importante que criem um sistema robusto que valide todas as possibilidades.

#### Envio (Cliente → Servidor)

```
{
  "operacao": "usuario_atualizar",
  "token": "a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8",
  "usuario": {
    "nome": "Gabriel P. Neves",
    "senha": "novaSenhaSuperSegura456"
  }
}

```

#### Recebimento (Servidor → Cliente) em caso de sucesso

```
{
  "operacao": "usuario_atualizar",
  "status": true,
  "info": "Usuário atualizado com sucesso."
}

```

#### Recebimento (Servidor → Cliente) em caso de falha

```
{
  "operacao": "usuario_atualizar",
  "status": false,
  "info": "Erro ao atualizar usuário."
}

```

### 4.6. Deleção de Usuário (`usuario_deletar`)

Ao deletar um usuário com sucesso a conexão entre o servidor e cliente se mantém.

#### Envio (Cliente → Servidor)

```
{
  "operacao": "usuario_deletar",
  "token": "a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8"
}

```

#### Recebimento (Servidor → Cliente) em caso de sucesso

```
{
  "operacao": "usuario_deletar",
  "status": true,
  "info": "Usuário deletado com sucesso."
}

```

#### Recebimento (Servidor → Cliente) em caso de falha

```
{
  "operacao": "usuario_deletar",
  "status": false,
  "info": "Erro ao deletar usuário."
}

```

### 4.7. Criação de Transação (`transacao_criar`)

OBS: Na operação 'transacao_criar', é enviada a quantidade especificada na propriedade 'valor' pelo usuário contido no token, enviando ao usuário correspondente no 'cpf_destino'.
Exemplo:<br>
- Pedro fez login no sistema, e cria uma transação para enviar R$10,00 a João<br>
- O "token" enviado será o token de Pedro, que será seu identificador<br>
- O "valor" a ser armazenado será R$10,00<br>
- O "cpf_destino" a ser armazenado será o de João<br>

#### Envio (Cliente → Servidor)

```
{
  "operacao": "transacao_criar",
  "token": "a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8",
  "valor": 150.75,
  "cpf_destino": "098.765.432-11"
}

```

#### Recebimento (Servidor → Cliente) em caso de sucesso

```
{
  "operacao": "transacao_criar",
  "status": true,
  "info": "Transação realizada com sucesso."
}

```

#### Recebimento (Servidor → Cliente) em caso de falha

```
{
  "operacao": "transacao_criar",
  "status": false,
  "info": "Erro ao criar transação."
}

```

### 4.8. Leitura de Transações (`transacao_ler`)

*Nota: Esta operação utiliza **filtragem por datas** para lidar com grandes volumes de dados.*<br>
- Envia-se uma data inicial e uma data final. Assim, apenas as transações ocorridas no período determinado são devolvidas.<br>
- O token a ser enviado deve ser do usuário logado no sistema.

O servidor deve ter como limite máximo de retorno 31 dias (31 dias foi escolhido pois consegue acolher todos os meses), exemplo:<br>
O Usuário pediu as transações do dia 1 de janeiro a 1 de fevereiro, é esperado que o servidor retorne todas as transações entre esse tempo<br>
O Usuário pediu as transações do dia 1 de janeiro a 1 de maio, o servidor deve retornar um erro.

#### Envio (Cliente → Servidor)

```
{
  "operacao": "transacao_ler",
  "token": "a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8",
  "data_inicial": "2025-08-01T00:00:00Z",
  "data_final": "2025-08-27T23:59:59Z"
}

```

#### Recebimento (Servidor → Cliente) em caso de sucesso

```
{
  "operacao": "transacao_ler",
  "status": true,
  "info": "Transações recuperadas com sucesso.",
  "transacoes": [
    {
      "id": 101,
      "valor_enviado": 150.75,
      "usuario_enviador": {
        "nome": "Gabriel Pereira Neves",
        "cpf": "123.456.789-00"
      },
      "usuario_recebedor": {
        "nome": "Yan Jardim Leal",
        "cpf": "098.765.432-11"
      },
      criado_em: "2025-08-02T00:00:00Z",
      atualizado_em: "2025-08-02T00:00:00Z"
    }
  ]
}

```

#### Recebimento (Servidor → Cliente) em caso de falha

```
{
  "operacao": "transacao_ler",
  "status": false,
  "info": "Erro ao ler transações."
}

```

### 4.9. Realizar depósito (`depositar`)
Essa ação permite que o usuário deposite quantia X de dinheiro em sua conta,<br>
O `valor_enviado` representa a quantidade que está sendo depositada.

#### Envio (Cliente → Servidor)

```
{
  "operacao": "depositar",
  "token": "a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8",
  "valor_enviado": 123.12
}

```

#### Recebimento (Servidor → Cliente) em caso de sucesso

```
{
  "operacao": "depositar",
  "status": true,
  "info": "Deposito realizado com sucesso."
}

```

#### Recebimento (Servidor → Cliente) em caso de falha

```
{
  "operacao": "depositar",
  "status": false,
  "info": "Erro ao depositar."
}

```

### 4.10. Conectar com o servidor (`conectar`)
Essa ação permite que o usuário se conecte com o servidor e deve ser a primeira a ser chamada.

Caso o usuário chame `conectar` novamente, mesmo já estando conectado, a operação deve ser bem-sucedida.

#### Envio (Cliente → Servidor)

```
{
  "operacao": "conectar"
}

```

#### Recebimento (Servidor → Cliente) em caso de sucesso

```
{
  "operacao": "conectar",
  "status": true,
  "info": "Servidor conectado com sucesso."
}

```

#### Recebimento (Servidor → Cliente) em caso de falha

ADENDO: Muito provavelmente caso o servidor se conecte com sucesso, ele nem mesmo irá retornar algo, mas apenas por padrão, iremos adicionar o "caso de erro"

```
{
  "operacao": "conectar",
  "status": false,
  "info": "Erro ao se conectar."
}

```

### 4.11. Erro na resposta do servidor (`erro_servidor`)
Essa ação permite ao usuário reportar erros ao servidor em casos em que o servidor envia mensagens incorretas.

Por exemplo caso o servidor responda com o usuário "null" durante a operação "usuario_ler"

#### Recebimento (Servidor → Cliente) do exemplo acima

```
{
  "operacao": "usuario_ler",
  "status": true,
  "info": "Dados do usuário recuperados com sucesso.",
  "usuario": null
}
```

O cliente deve ser capaz de reportar este erro:

#### Envio (Cliente → Servidor)

```
{
  "operacao": "erro_servidor",
  "operacao_enviada": "usuario_ler",
  "info": "O usuário enviado pelo servidor era nulo".
}
```

Servidor deve apenas responder que recebeu a mensagem, não é necessário fazer nenhuma tratativa de erro, apenas
armazenar em algum log de erro ou exibir o erro em terminal.

### Recebimento (Servidor → Cliente)
```
  {
    "operacao": "erro_servidor",
    "status": true,
    "info": "Erro registrado no servidor"
  }
```

## 5. Em caso de erro

### 5.1. Erros padrões
O servidor deverá retornar uma mensagem com `operacao`, `status` e `info`, mais nenhuma informação deve ser enviada adiante.<br>
As possíveis mensagens de erro (`info`) serão responsabilidade do servidor, o cliente apenas deve passar essa mensagem para a interface do usuário e tratar o erro conforme a `operacao`.

O que isso significa? Quando o cliente receber uma operação com o `status` como `false` ele deve tratar o erro pela operação (Caso precise) e mostrar a `info` para interface do usuário caso o cliente deseje.

```
{
  "operacao": "operacaoXYZ",
  "status": false,
  "info": "Texto de erro."
}

```

### 5.2. Erros de JSON
Caso o servidor envie uma mensagem que não contenha `operacao`, `status` ou `info`, ou o cliente envie uma mensagem que não contenha `operacao`,<br>o servidor/cliente que recebe devem retornar `null` para encerrar a conexão.

### 5.3 Erros na conexão
Caso a primeira `operacao` a ser recebida não seja `conectar` um erro deve ser retornado como:
```
{
  "operacao": OPERACAO_QUE_FOI_RECEBIDA,
  "status": false,
  "info": "Erro, para receber uma operacao, a primeira operacao deve ser 'conectar'"
}

```

## 6. Tipagem de Dados

| **Campo(s)**           | **Tipo de Dado** | **Descrição**                                                                                                                                                                                   |
| ---------------------------- | ---------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `id`,`pagina`,`limite` | **`int`**      | Valores numéricos inteiros.                                                                                                                                                                            |
| `valor_enviado`,`saldo`  | **`double`**   | Valores numéricos de ponto flutuante com no máximo *duas* casas decimais. |
| `cpf`, `cpf_destino` | **`String: 000.000.000-00`**   | O Validador apenas valida se o CPF está na formatação, não se é válido (espaçamentos no começo e no final são desconsiderados). |
| `nome`,`senha`  | **`String: Min 6 e Max 120 caracteres`**   | O Validador apenas valida o tamanho, ele desconsidera espaços no começo e no fim. |
| `data_inicial`,`data_final`| **`String: yyyy-MM-dd'T'HH:mm:ss'Z'`** | Datas devem estar no formato ISO 8601 UTC. |
| Todos os outros campos     | **`String: Min 3 e Max 200`** | Valores de texto (espaçamentos no começo e no final são desconsiderados).                   |

## 7. Conexão e desconexão entre cliente e servidor
As conexões quando criadas devem se manter até o final da sessão do usuário, apenas se desconectando no final de cada sessão.

### 7.1. Conexão.
- O cliente deve enviar uma operacao chamada `conectar` para o IP e a porta escolhida.
- O servidor deve responder de acordo conforme o protocolo.
### 7.3. Desconexão.
- Tanto o cliente como o servidor podem se desconectar enviando `null` caso alguma operação não cumpra o protocolo e não haja alguma regra de retorno. (Ou caso apenas queira se desconectar)

## 8. Explicações adicionais e avisos

### 8.1. Esperado de cada aluno.
- É esperado que o servidor retorne os dados corretamente, porém o cliente sempre deve se previnir para caso o servidor retorne um `status` como `false`,<br>
ou até mesmo não retorne nada, mesmo que seus dados enviados estejam corretos, prever erros ou falta de respostas é de inteira responsabilidade do aluno.

- É esperado que o aluno tenha lido todo o documento, erros claramente contra o protocolo serão de inteira responsabilidade do aluno.

- É esperado do aluno, que caso encontre uma vulnerabilidade ou ponto importante no protocolo, ele imediatamente avise no grupo de Whatsapp da turma ou em sala.

### 8.2. Pontos subentendidos.
Há algumas informações que estão subentendidas sobre o projeto, o protocolo não visa em conta as regras de negócio do sistema bancário,<br>
que por sua vez estão disponíveis [clicando aqui](https://docs.google.com/document/d/1MRiMjnu9PdJSWPyAKl4zBdkZN0iFwNujDLjRW_oP-IA/edit?tab=t.0)

Todas possiblidades de conversas entre o `cliente->servidor` e `servidor->cliente` estão listadas aqui, para que sua mensagem esteja correta ela deve seguir o molde fornecido a **risca**, do contrário, estará contra o protocolo e suas orientações.

### 8.3. ISO 8601
**Explicação:**<br>
Apenas será necessário transformar em String, e ler a String com funções já nativas do Java (Date)

`yyyy`: ano com 4 dígitos<br>
`MM`: mês com 2 dígitos<br>
`dd`: dia com 2 dígitos<br>
`'T'`: separador entre data e hora<br>
`HH`: hora (00-23)<br>
`mm`: minutos<br>
`ss`: segundos<br>
`'Z'`: indica que o horário está em UTC (tempo universal coordenado)

**Exemplo:**

`{
  "data_inicial": "2025-08-01T00:00:00Z",
  "data_final": "2025-08-27T23:59:59Z"
}`

