# INSTRUÇÕES DE USO - NewPix Banking System

## 🚀 Execução Rápida

### 1. Iniciar o Servidor
Execute o script: `start-server.bat`
OU execute manualmente:
```bash
mvn exec:java -Dexec.mainClass="com.newpix.server.gui.ServerGUI"
```

### 2. Iniciar o Cliente  
Execute o script: `start-client.bat`
OU execute manualmente:
```bash
mvn exec:java -Dexec.mainClass="com.newpix.client.gui.LoginGUI"
```

## 📖 Tutorial de Uso Completo

### Passo 1: Configurar o Servidor
1. Execute `start-server.bat`
2. Na interface do servidor:
   - Mantenha a porta padrão (8080)
   - Clique em "Iniciar Servidor"
   - Aguarde a mensagem "Servidor iniciado na porta 8080"

### Passo 2: Conectar Cliente
1. Execute `start-client.bat` 
2. Na tela de login:
   - Host: `localhost`
   - Porta: `8080`
   - Clique em "Conectar"
   - Status deve mudar para "Conectado"

### Passo 3: Cadastrar Primeiro Usuário
1. Preencha os campos:
   - **CPF**: `111.111.111-11`
   - **Nome**: `João Silva`
   - **Senha**: `senha123`
2. Clique em "Cadastrar"
3. Aguarde mensagem de sucesso

### Passo 4: Fazer Login
1. Use as mesmas credenciais do cadastro
2. Clique em "Login"
3. Será redirecionado para tela principal

### Passo 5: Adicionar Saldo (Para Testes)
**IMPORTANTE**: Para demonstrar transferências, você pode usar o console do servidor para adicionar saldo inicial.

Execute no terminal do servidor:
```sql
-- Conectar ao banco SQLite
sqlite3 newpix.db

-- Adicionar saldo ao usuário
UPDATE usuarios SET saldo = 1000.0 WHERE cpf = '111.111.111-11';

-- Verificar saldo
SELECT * FROM usuarios;
```

### Passo 6: Criar Segundo Usuário
1. Abra outro cliente (execute `start-client.bat` novamente)
2. Cadastre:
   - **CPF**: `222.222.222-22`
   - **Nome**: `Maria Santos`
   - **Senha**: `senha456`
3. Faça login com este usuário

### Passo 7: Realizar Transferência PIX
1. No cliente do João Silva:
   - Vá para aba "Enviar PIX"
   - **Valor**: `100`
   - **CPF Destino**: `222.222.222-22`
   - Clique em "Enviar PIX"

### Passo 8: Verificar Transações
1. Em ambos os clientes:
   - Clique em "Atualizar Extrato"
   - Vá para aba "Extrato"
   - Verifique as transações listadas
   - Observe os saldos atualizados

## 🔧 Funcionalidades Testáveis

### ✅ CRUD de Usuários
- **Create**: Cadastro na tela de login
- **Read**: Dados exibidos na tela principal
- **Update**: Aba "Configurações" → alterar nome/senha
- **Delete**: Disponível via configurações

### ✅ Operações de Transação  
- **Create**: Enviar PIX na aba correspondente
- **Read**: Visualizar extrato com filtro por período

### ✅ Sistema de Autenticação
- Login/Logout com tokens
- Sessões com expiração automática
- Validação de permissões

### ✅ Funcionalidades Avançadas
- Múltiplos clientes simultâneos
- Transações atômicas
- Validação de saldo
- Interface gráfica responsiva
- Logs em tempo real no servidor

## 🧪 Cenários de Teste

### Teste 1: Transferência Básica
1. João envia R$ 50,00 para Maria
2. Verificar saldos atualizados
3. Conferir extrato de ambos

### Teste 2: Saldo Insuficiente  
1. Tentar enviar valor maior que o saldo
2. Verificar mensagem de erro
3. Confirmar que não houve transferência

### Teste 3: Múltiplos Clientes
1. Conectar 3+ clientes simultaneamente
2. Realizar transferências cruzadas
3. Verificar consistência dos dados

### Teste 4: Desconexão e Reconexão
1. Fechar cliente durante operação
2. Reconectar com mesmo usuário
3. Verificar estado consistente

## 📊 Monitoramento

### Servidor
- Logs em tempo real na interface
- Contagem de conexões ativas
- Mensagens de erro detalhadas

### Cliente  
- Status de conexão visível
- Validação de formulários
- Feedback imediato das operações

## 🛠️ Resolução de Problemas

### Erro de Conexão
- Verificar se servidor está rodando
- Confirmar porta 8080 está livre
- Verificar firewall/antivírus

### Erro de Compilação
- Verificar Java 17+ instalado
- Executar `mvn clean compile`
- Verificar dependências do Maven

### Banco de Dados
- Arquivo `newpix.db` criado automaticamente
- Para reset: deletar arquivo e reiniciar servidor
- Backup: copiar arquivo `newpix.db`

## ✨ Recursos Avançados

### Protocolo JSON
- Todas as mensagens seguem especificação
- Validação automática via classe Validator
- Logs detalhados das comunicações

### Segurança
- Senhas criptografadas com BCrypt
- Tokens de sessão únicos
- Validação de dados de entrada

### Performance
- Pool de threads no servidor
- Conexões assíncronas
- Cache de sessões em memória

---

**Criado por**: Yan Jardim Leal & Gabriel Pereira Neves  
**Testado por**: Thomas Valeranovicz de Oliveira & Rafael Adonis Menon
