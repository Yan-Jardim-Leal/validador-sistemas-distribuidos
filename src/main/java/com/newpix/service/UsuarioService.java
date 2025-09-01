package com.newpix.service;

import com.newpix.dao.SessaoDAO;
import com.newpix.dao.UsuarioDAO;
import com.newpix.model.Sessao;
import com.newpix.model.Usuario;

import java.sql.SQLException;
import java.util.List;

/**
 * Serviço para operações de usuário.
 */
public class UsuarioService {
    
    private final UsuarioDAO usuarioDAO;
    private final SessaoDAO sessaoDAO;
    
    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
        this.sessaoDAO = new SessaoDAO();
    }
    
    /**
     * Realiza login de usuário e cria uma sessão.
     */
    public String login(String cpf, String senha) throws SQLException {
        // Verificar credenciais
        if (!usuarioDAO.autenticar(cpf, senha)) {
            return null;
        }
        
        // Invalidar sessões anteriores do usuário
        sessaoDAO.invalidarTodasSessoesUsuario(cpf);
        
        // Criar nova sessão
        Sessao novaSessao = new Sessao(cpf);
        if (sessaoDAO.criar(novaSessao)) {
            return novaSessao.getToken();
        }
        
        return null;
    }
    
    /**
     * Realiza logout invalidando a sessão.
     */
    public boolean logout(String token) throws SQLException {
        return sessaoDAO.invalidar(token);
    }
    
    /**
     * Cria um novo usuário.
     */
    public boolean criarUsuario(String cpf, String nome, String senha) throws SQLException {
        // Verificar se usuário já existe
        if (usuarioDAO.existe(cpf)) {
            return false;
        }
        
        // Validar dados básicos
        if (cpf == null || cpf.trim().length() < 11 || 
            nome == null || nome.trim().length() < 6 || nome.trim().length() > 120 ||
            senha == null || senha.trim().length() < 6 || senha.trim().length() > 120) {
            return false;
        }
        
        Usuario novoUsuario = new Usuario(cpf.trim(), nome.trim(), senha.trim());
        return usuarioDAO.criar(novoUsuario);
    }
    
    /**
     * Busca dados de um usuário por token.
     */
    public Usuario buscarUsuarioPorToken(String token) throws SQLException {
        String cpfUsuario = sessaoDAO.getCpfUsuarioPorToken(token);
        if (cpfUsuario != null) {
            return usuarioDAO.buscarPorCpf(cpfUsuario);
        }
        return null;
    }
    
    /**
     * Atualiza dados de um usuário.
     */
    public boolean atualizarUsuario(String token, String novoNome, String novaSenha) throws SQLException {
        String cpfUsuario = sessaoDAO.getCpfUsuarioPorToken(token);
        if (cpfUsuario == null) {
            return false;
        }
        
        Usuario usuario = usuarioDAO.buscarPorCpf(cpfUsuario);
        if (usuario == null) {
            return false;
        }
        
        // Atualizar apenas os campos fornecidos
        if (novoNome != null && !novoNome.trim().isEmpty()) {
            if (novoNome.trim().length() < 6 || novoNome.trim().length() > 120) {
                return false;
            }
            usuario.setNome(novoNome.trim());
        }
        
        if (novaSenha != null && !novaSenha.trim().isEmpty()) {
            if (novaSenha.trim().length() < 6 || novaSenha.trim().length() > 120) {
                return false;
            }
            usuario.setSenha(novaSenha.trim());
        }
        
        return usuarioDAO.atualizar(usuario);
    }
    
    /**
     * Deleta um usuário e suas sessões.
     */
    public boolean deletarUsuario(String token) throws SQLException {
        String cpfUsuario = sessaoDAO.getCpfUsuarioPorToken(token);
        if (cpfUsuario == null) {
            return false;
        }
        
        // Invalidar todas as sessões do usuário
        sessaoDAO.invalidarTodasSessoesUsuario(cpfUsuario);
        
        // Deletar usuário
        return usuarioDAO.deletar(cpfUsuario);
    }
    
    /**
     * Verifica se um token é válido.
     */
    public boolean isTokenValido(String token) throws SQLException {
        return sessaoDAO.isTokenValido(token);
    }
    
    /**
     * Obtém CPF do usuário por token.
     */
    public String getCpfUsuarioPorToken(String token) throws SQLException {
        return sessaoDAO.getCpfUsuarioPorToken(token);
    }
    
    /**
     * Lista todos os usuários (apenas para admin/debug).
     */
    public List<Usuario> listarTodosUsuarios() throws SQLException {
        return usuarioDAO.listarTodos();
    }
    
    /**
     * Verifica se um CPF existe no sistema.
     */
    public boolean cpfExiste(String cpf) throws SQLException {
        return usuarioDAO.existe(cpf);
    }
    
    /**
     * Busca um usuário por CPF.
     */
    public Usuario buscarPorCpf(String cpf) throws SQLException {
        return usuarioDAO.buscarPorCpf(cpf);
    }
}
