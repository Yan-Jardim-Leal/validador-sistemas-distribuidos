package com.newpix.test;

import com.newpix.dao.DatabaseManager;
import com.newpix.dao.UsuarioDAO;
import com.newpix.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para UsuarioDAO.
 */
public class UsuarioDAOTest {
    
    private UsuarioDAO usuarioDAO;
    
    @BeforeEach
    public void setUp() throws SQLException {
        DatabaseManager.getInstance().initializeDatabase();
        usuarioDAO = new UsuarioDAO();
    }
    
    @Test
    public void testCriarUsuario() throws SQLException {
        Usuario usuario = new Usuario("123.456.789-00", "João Silva", "senha123");
        
        boolean result = usuarioDAO.criar(usuario);
        
        assertTrue(result);
        assertTrue(usuarioDAO.existe("123.456.789-00"));
    }
    
    @Test
    public void testBuscarUsuario() throws SQLException {
        Usuario usuario = new Usuario("987.654.321-00", "Maria Santos", "senha456");
        usuarioDAO.criar(usuario);
        
        Usuario usuarioEncontrado = usuarioDAO.buscarPorCpf("987.654.321-00");
        
        assertNotNull(usuarioEncontrado);
        assertEquals("Maria Santos", usuarioEncontrado.getNome());
        assertEquals("987.654.321-00", usuarioEncontrado.getCpf());
    }
    
    @Test
    public void testAutenticarUsuario() throws SQLException {
        Usuario usuario = new Usuario("111.222.333-44", "Pedro Oliveira", "minhasenha");
        usuarioDAO.criar(usuario);
        
        boolean autenticado = usuarioDAO.autenticar("111.222.333-44", "minhasenha");
        boolean naoAutenticado = usuarioDAO.autenticar("111.222.333-44", "senhaerrada");
        
        assertTrue(autenticado);
        assertFalse(naoAutenticado);
    }
    
    @Test
    public void testAtualizarSaldo() throws SQLException {
        Usuario usuario = new Usuario("555.666.777-88", "Ana Costa", "senha789");
        usuarioDAO.criar(usuario);
        
        boolean result = usuarioDAO.atualizarSaldo("555.666.777-88", 1500.75);
        
        assertTrue(result);
        
        Usuario usuarioAtualizado = usuarioDAO.buscarPorCpf("555.666.777-88");
        assertEquals(1500.75, usuarioAtualizado.getSaldo(), 0.01);
    }
}
