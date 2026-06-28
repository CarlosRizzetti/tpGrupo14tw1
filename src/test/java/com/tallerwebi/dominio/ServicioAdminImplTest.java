package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.entity.enums.EstadoUsuario;
import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.services.ServicioAdminImpl;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioAdminImplTest {

  private RepositorioUsuario repositorioUsuario;
  private RepositorioCategoria repositorioCategoria;
  private ServicioAdminImpl servicioAdmin;

  @BeforeEach
  public void init() {
    repositorioUsuario = mock(RepositorioUsuario.class);
    repositorioCategoria = mock(RepositorioCategoria.class);
    servicioAdmin = new ServicioAdminImpl(repositorioUsuario, repositorioCategoria);
  }

  @Test
public void alAprobarUsuarioDebeActivarYAsignarCategoriaSiExisten() {
    Usuario usuario = new Usuario();
    usuario.setEstado(EstadoUsuario.PENDIENTE);
    usuario.setCategorias(new HashSet<>());

    Categoria categoria = new Categoria();

    when(repositorioUsuario.obtenerPorId(1L)).thenReturn(usuario);
    when(repositorioCategoria.buscarPorId(2L)).thenReturn(categoria);

    servicioAdmin.aprobarUsuario(1L, 2L);

    assertEquals(EstadoUsuario.ACTIVO, usuario.getEstado());
    assertTrue(usuario.getCategorias().contains(categoria));
    verify(repositorioUsuario, times(1)).modificar(usuario);
}

  @Test
  public void siUsuarioOCategoriaNoExistenNoDebeHacerNada() {
    when(repositorioUsuario.obtenerPorId(1L)).thenReturn(null);
    when(repositorioCategoria.buscarPorId(2L)).thenReturn(null);

    servicioAdmin.aprobarUsuario(1L, 2L);

    verify(repositorioUsuario, never()).modificar(any());
  }
}
