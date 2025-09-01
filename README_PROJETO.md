# NewPix® - Sistema Bancário Distribuído

Sistema bancário distribuído desenvolvido em Java que implementa funcionalidades de PIX (transferências instantâneas) com arquitetura cliente-servidor usando sockets TCP e protocolo JSON.

## 📋 Funcionalidades

### Sistema Completo
- **Arquitetura Cliente-Servidor** com comunicação via sockets TCP
- **Protocolo de mensagens JSON** conforme especificação
- **Banco de dados SQLite** para persistência
- **Interface gráfica** tanto para cliente quanto servidor
- **Sistema de autenticação** com tokens de sessão
- **Transações atômicas** respeitando propriedades ACID

### Operações de Usuário (CRUD Completo)
- ✅ Cadastro de usuário
- ✅ Login/Logout com autenticação segura
- ✅ Leitura de dados do usuário
- ✅ Atualização de dados (nome e senha)
- ✅ Exclusão de usuário

### Operações de Transação
- ✅ Criação de transações PIX
- ✅ Consulta de extrato por período
- ✅ Validação de saldo antes das transferências
- ✅ Operações atômicas garantindo consistência

### Segurança
- 🔐 Senhas criptografadas com BCrypt
- 🎫 Sistema de tokens com expiração
- 🛡️ Validação de protocolo com classe Validator
- 🔒 Transações bancárias atômicas

## 🚀 Como Executar

### Pré-requisitos
- Java 11 ou superior
- Maven 3.6 ou superior

### 1. Compilar o Projeto
```bash
mvn clean compile
```

### 2. Executar o Servidor

#### Via Interface Gráfica (Recomendado)
```bash
mvn exec:java -Dexec.mainClass="com.newpix.server.gui.ServerGUI"
```

#### Via Linha de Comando
```bash
mvn exec:java -Dexec.mainClass="com.newpix.server.NewPixServer" -Dexec.args="8080"
```

### 3. Executar o Cliente
```bash
mvn exec:java -Dexec.mainClass="com.newpix.client.gui.LoginGUI"
```

### 4. Executar Testes
```bash
mvn test
```

## 📱 Como Usar o Sistema

### No Servidor
1. Execute a interface gráfica do servidor
2. Configure a porta (padrão: 8080)
3. Clique em "Iniciar Servidor"
4. Monitore as conexões e logs na interface

### No Cliente
1. Execute a interface gráfica do cliente
2. Configure host (localhost) e porta (8080)
3. Clique em "Conectar"
4. **Cadastre-se** ou faça **Login**
5. Use as funcionalidades:
   - **Enviar PIX**: transfira dinheiro para outros usuários
   - **Ver Extrato**: consulte suas transações
   - **Configurações**: atualize seus dados

## 🏗️ Arquitetura do Sistema

```
src/main/java/
├── com/newpix/
│   ├── model/          # Entidades (Usuario, Transacao, Sessao)
│   ├── dao/            # Acesso a dados (SQLite)
│   ├── service/        # Regras de negócio
│   ├── server/         # Servidor TCP e processamento
│   │   └── gui/        # Interface gráfica do servidor
│   └── client/         # Cliente TCP
│       └── gui/        # Interface gráfica do cliente
└── validador/          # Validação do protocolo JSON
```

## 📊 Banco de Dados

O sistema utiliza SQLite com as seguintes tabelas:

- **usuarios**: CPF, nome, senha (hash), saldo
- **transacoes**: ID, valor, remetente, destinatário, timestamps
- **sessoes**: token, usuário, criação, expiração, status

## 🔌 Protocolo de Comunicação

O sistema segue rigorosamente o protocolo definido no README original:

### Exemplo de Login
```json
// Cliente → Servidor
{
  "operacao": "usuario_login",
  "cpf": "123.456.789-00",
  "senha": "minhasenha"
}

// Servidor → Cliente
{
  "operacao": "usuario_login",
  "token": "a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8",
  "status": true,
  "info": "Login bem-sucedido."
}
```

### Exemplo de PIX
```json
// Cliente → Servidor
{
  "operacao": "transacao_criar",
  "token": "a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8",
  "valor": 150.75,
  "cpf_destino": "098.765.432-11"
}

// Servidor → Cliente
{
  "operacao": "transacao_criar",
  "status": true,
  "info": "Transação realizada com sucesso."
}
```

## 🧪 Testando o Sistema

1. **Inicie o servidor** via GUI
2. **Execute múltiplos clientes** para simular usuários simultâneos
3. **Cadastre usuários** diferentes
4. **Realize transferências** entre eles
5. **Consulte extratos** para verificar as transações

### Exemplo de Teste Completo

1. **Usuário A**: CPF `111.111.111-11`, Nome `João Silva`
2. **Usuário B**: CPF `222.222.222-22`, Nome `Maria Santos`
3. João envia R$ 100,00 para Maria
4. Ambos consultam extratos
5. Saldos são atualizados automaticamente

## 🛠️ Dependências

- **Jackson 2.19.2**: Manipulação JSON
- **SQLite 3.43.0**: Banco de dados
- **BCrypt 0.4**: Criptografia de senhas
- **JUnit 5.9.2**: Testes unitários

## 📝 Requisitos Atendidos

### ✅ Requisitos Mínimos
- [x] Login e Logout
- [x] CRUD completo de usuários
- [x] Interface gráfica no Cliente e Servidor
- [x] Comunicação via sockets TCP
- [x] Protocolo de mensagens JSON

### ✅ Requisitos Funcionais
- [x] CRUD Usuários (Create, Read, Update, Delete)
- [x] Login/Logout com autenticação
- [x] CRUD Transações (Create, Read)
- [x] Registro e consulta de saldo

### ✅ Requisitos Não Funcionais
- [x] Sistema funciona apenas com usuários logados
- [x] Transações atômicas respeitando ACID
- [x] Verificação de saldo suficiente
- [x] Um saldo por usuário
- [x] Sistema de mensagens com identificações padronizadas
- [x] Mensagens em JSON sem acentos

## 👥 Criadores

* **Yan Jardim Leal**
* **Gabriel Pereira Neves**

## 🧪 Testers

* **Thomas Valeranovicz de Oliveira**
* **Rafael Adonis Menon**

## 📄 Licença

Este projeto foi desenvolvido para a disciplina de Sistemas Distribuídos.
