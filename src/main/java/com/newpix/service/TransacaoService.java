package com.newpix.service;

import com.newpix.dao.SessaoDAO;
import com.newpix.dao.TransacaoDAO;
import com.newpix.dao.UsuarioDAO;
import com.newpix.model.Transacao;
import com.newpix.model.Usuario;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço para operações de transação.
 */
public class TransacaoService {
    
    private final TransacaoDAO transacaoDAO;
    private final UsuarioDAO usuarioDAO;
    private final SessaoDAO sessaoDAO;
    
    public TransacaoService() {
        this.transacaoDAO = new TransacaoDAO();
        this.usuarioDAO = new UsuarioDAO();
        this.sessaoDAO = new SessaoDAO();
    }
    
    /**
     * Cria uma nova transação (transferência PIX).
     */
    public boolean criarTransacao(String token, double valor, String cpfDestino) throws SQLException {
        // Verificar token válido
        String cpfRemetente = sessaoDAO.getCpfUsuarioPorToken(token);
        if (cpfRemetente == null) {
            return false;
        }
        
        // Validar valor
        if (valor <= 0) {
            return false;
        }
        
        // Verificar se usuário destinatário existe
        if (!usuarioDAO.existe(cpfDestino)) {
            return false;
        }
        
        // Verificar se não está tentando enviar para si mesmo
        if (cpfRemetente.equals(cpfDestino)) {
            return false;
        }
        
        // Buscar usuários
        Usuario remetente = usuarioDAO.buscarPorCpf(cpfRemetente);
        Usuario destinatario = usuarioDAO.buscarPorCpf(cpfDestino);
        
        if (remetente == null || destinatario == null) {
            return false;
        }
        
        // Verificar saldo suficiente
        if (!remetente.temSaldoSuficiente(valor)) {
            return false;
        }
        
        // Executar transferência atômica
        return transacaoDAO.executarTransferencia(cpfRemetente, cpfDestino, valor);
    }
    
    /**
     * Busca transações de um usuário por período.
     */
    public List<Transacao> buscarTransacoesPorPeriodo(String token, LocalDateTime dataInicial, LocalDateTime dataFinal) throws SQLException {
        // Verificar token válido
        String cpfUsuario = sessaoDAO.getCpfUsuarioPorToken(token);
        if (cpfUsuario == null) {
            return null;
        }
        
        // Validar datas
        if (dataInicial == null || dataFinal == null || dataInicial.isAfter(dataFinal)) {
            return null;
        }
        
        return transacaoDAO.buscarPorPeriodo(cpfUsuario, dataInicial, dataFinal);
    }
    
    /**
     * Busca todas as transações de um usuário.
     */
    public List<Transacao> buscarTodasTransacoesUsuario(String token) throws SQLException {
        // Verificar token válido
        String cpfUsuario = sessaoDAO.getCpfUsuarioPorToken(token);
        if (cpfUsuario == null) {
            return null;
        }
        
        return transacaoDAO.buscarPorUsuario(cpfUsuario);
    }
    
    /**
     * Busca uma transação específica por ID.
     */
    public Transacao buscarTransacaoPorId(String token, int id) throws SQLException {
        // Verificar token válido
        String cpfUsuario = sessaoDAO.getCpfUsuarioPorToken(token);
        if (cpfUsuario == null) {
            return null;
        }
        
        Transacao transacao = transacaoDAO.buscarPorId(id);
        
        // Verificar se o usuário está relacionado à transação
        if (transacao != null) {
            String cpfEnviador = transacao.getUsuarioEnviador().getCpf();
            String cpfRecebedor = transacao.getUsuarioRecebedor().getCpf();
            
            if (!cpfUsuario.equals(cpfEnviador) && !cpfUsuario.equals(cpfRecebedor)) {
                return null; // Usuário não tem permissão para ver esta transação
            }
        }
        
        return transacao;
    }
    
    /**
     * Lista todas as transações do sistema (apenas para admin/debug).
     */
    public List<Transacao> listarTodasTransacoes() throws SQLException {
        return transacaoDAO.listarTodas();
    }
    
    /**
     * Deposita um valor na conta de um usuário (para testes).
     */
    public boolean depositar(String cpf, double valor) throws SQLException {
        if (valor <= 0) {
            return false;
        }
        
        Usuario usuario = usuarioDAO.buscarPorCpf(cpf);
        if (usuario == null) {
            return false;
        }
        
        double novoSaldo = usuario.getSaldo() + valor;
        return usuarioDAO.atualizarSaldo(cpf, novoSaldo);
    }
    
    /**
     * Saca um valor da conta de um usuário (para testes).
     */
    public boolean sacar(String cpf, double valor) throws SQLException {
        if (valor <= 0) {
            return false;
        }
        
        Usuario usuario = usuarioDAO.buscarPorCpf(cpf);
        if (usuario == null) {
            return false;
        }
        
        if (!usuario.temSaldoSuficiente(valor)) {
            return false;
        }
        
        double novoSaldo = usuario.getSaldo() - valor;
        return usuarioDAO.atualizarSaldo(cpf, novoSaldo);
    }
    
    /**
     * Obtém o saldo atual de um usuário.
     */
    public Double obterSaldo(String token) throws SQLException {
        String cpfUsuario = sessaoDAO.getCpfUsuarioPorToken(token);
        if (cpfUsuario == null) {
            return null;
        }
        
        Usuario usuario = usuarioDAO.buscarPorCpf(cpfUsuario);
        return usuario != null ? usuario.getSaldo() : null;
    }
}
