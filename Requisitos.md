Requisitos Funcionais e Não Funcionais do Projeto Final (Transferência de Pix)

Nome Projeto : NewPix® - Envio e recebimento de PIX

Requisitos Funcionais:
CRUD - Usuários
Cadastro de usuário
Leitura dos DADOS do usuário
Atualizar os DADOS do usuário
Apagar o usuário
Login de usuário
Logout de usuário
CRUD - Transações
Cadastro de transação
Leitura dos DADOS de uma transação
E/S - Registro de saldo
Requisitos Não Funcionais:
Só pode interagir no sistema LOGADO
Transações atômicas
Transações alteram E/S - Registro de saldo
Saída do saldo deve verificar quantia suficiente na conta
Pesquisa de extrato
Um saldo por usuário
Sistema de mensagens e suas identificações
A identificação da mensagem será feita por nomes em minúsculo
A separação de cada identificação será realizada por “_” ex:
usuario_login
usuario_logout
As mensagens serão enviadas em JSON
As mensagens não podem ter acento, ex:
{
	“operacao” : “usuario_login”,
	“nome” : “nome”,
	“senha” : “senha”
}
{
	“operacao” : “depositar”,
	“quantidade” : 1000.00,
	“exemplo_arvore” : false
}


Requisitos gerais do projeto :
#Requisitos mínimos:
>Login;
>Logout;
>Pelo menos um CRUD;
>Interface gráfica no Cliente e no Servidor;
>Comunicação por troca de mensagens via sockets;
>Protocolo de troca de mensagens.


Clientes
#Se cadastram para poderem usar o Sistema;
>Cadastro (CRUD);
#Se cadastrados, podem se logar no Sistema:
>Login;
#Podem atualizar/editar cadastro:
>CRUD do cadastro;
>Deve ser exigida uma autenticação (evita a edição do cadastro
por outro cliente): token;
#Podem se descadastrar do Sistema:
>CRUD do cadastro de clientes.
#Se logados, podem sair do Sistema:
>Logout;


Servidor
#Atende vários clientes ao mesmo tempo;
#Guarda informações de cadastro;
#Permite ou não login no Sistema;
#Efetua logout à pedido do cliente

#O que é necessário para a implementação?
>Banco de Dados? Sim
>Pré-cadastro de alguma coisa? Não sei
>Cliente Administrador? Não é necessário