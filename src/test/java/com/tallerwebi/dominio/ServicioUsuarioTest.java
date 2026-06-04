package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.excepcion.PasswordInvalida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import com.tallerwebi.dominio.interfaces.ServicioUsuario;
import com.tallerwebi.dominio.services.ServicioUsuarioImpl;
import com.tallerwebi.presentacion.dto.UsuarioDto;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioUsuarioTest {

  private ServicioUsuario servicioUsuario;
  private RepositorioUsuario repositorioUsuarioMock;
  private RepositorioCategoria repositorioCategoriasMock;

  @BeforeEach
  public void init() {
    this.repositorioUsuarioMock = mock(RepositorioUsuario.class);
    this.repositorioCategoriasMock = mock(RepositorioCategoria.class);
    this.servicioUsuario = new ServicioUsuarioImpl(this.repositorioUsuarioMock, this.repositorioCategoriasMock);
  }

  @Test
  public void listarUsuariosDeberiaRetornarLaListaDelRepositorio() {
    // preparacion
    List<Usuario> usuariosEsperados = Arrays.asList(new Usuario(), new Usuario());
    when(repositorioUsuarioMock.listarTodos()).thenReturn(usuariosEsperados);

    // ejecucion
    List<Usuario> resultado = servicioUsuario.listarUsuarios();

    // validacion
    assertThat(resultado, hasSize(2));
    verify(repositorioUsuarioMock, times(1)).listarTodos();
  }

  @Test
  public void crearUsuarioConDatosValidosDeberiaGuardarlo() throws Exception {
    // preparacion
    UsuarioDto dto = dtoValido();
    when(repositorioUsuarioMock.buscar(dto.getEmail())).thenReturn(null);

    // ejecucion
    servicioUsuario.crearUsuario(dto);

    // validacion
    verify(repositorioUsuarioMock, times(1)).guardar(any(Usuario.class));
  }

  @Test
  public void crearUsuarioConEmailExistenteDeberiaLanzarExcepcion() {
    // preparacion
    UsuarioDto dto = dtoValido();
    when(repositorioUsuarioMock.buscar(dto.getEmail())).thenReturn(new Usuario());

    // ejecucion y validacion
    assertThrows(UsuarioExistente.class, () -> servicioUsuario.crearUsuario(dto));
    verify(repositorioUsuarioMock, times(0)).guardar(any(Usuario.class));
  }

  @Test
  public void crearUsuarioConPasswordInvalidaDeberiaLanzarExcepcion() {
    // preparacion
    UsuarioDto dto = new UsuarioDto();
    dto.setEmail("test@test.com");
    dto.setPassword("invalida");
    dto.setRol("USER");

    // ejecucion y validacion
    assertThrows(PasswordInvalida.class, () -> servicioUsuario.crearUsuario(dto));
    verify(repositorioUsuarioMock, times(0)).guardar(any(Usuario.class));
  }

  @Test
  public void crearUsuarioSinRolDeberiaAsignarRolUserPorDefecto() throws Exception {
    // preparacion
    UsuarioDto dto = new UsuarioDto();
    dto.setEmail("test@test.com");
    dto.setPassword("Valida1");
    dto.setRol(null);
    when(repositorioUsuarioMock.buscar(dto.getEmail())).thenReturn(null);

    // ejecucion
    servicioUsuario.crearUsuario(dto);

    // validacion
    verify(repositorioUsuarioMock, times(1)).guardar(argThat(u -> "USER".equals(u.getRol())));
  }

  @Test
  public void editarUsuarioExistenteDeberiaModificarlo() throws Exception {
    // preparacion
    Usuario usuario = new Usuario();
    usuario.setEmail("viejo@test.com");
    usuario.setRol("USER");
    when(repositorioUsuarioMock.obtenerPorId(1L)).thenReturn(usuario);

    UsuarioDto dto = new UsuarioDto();
    dto.setEmail("nuevo@test.com");
    dto.setRol("ADMIN");

    // ejecucion
    servicioUsuario.editarUsuario(1L, dto);

    // validacion
    assertThat(usuario.getEmail(), equalTo("nuevo@test.com"));
    assertThat(usuario.getRol(), equalTo("ADMIN"));
    verify(repositorioUsuarioMock, times(1)).modificar(usuario);
  }

  @Test
  public void editarUsuarioInexistenteDeberiaLanzarExcepcion() throws Exception {
    // preparacion
    when(repositorioUsuarioMock.obtenerPorId(99L)).thenReturn(null);
    UsuarioDto dto = new UsuarioDto();
    dto.setEmail("x@test.com");

    // ejecucion y validacion
    assertThrows(IllegalArgumentException.class, () -> servicioUsuario.editarUsuario(99L, dto));
  }

  @Test
  public void darDeBajaDeberiaDesactivarElUsuario() {
    // preparacion
    Usuario usuario = new Usuario();
    usuario.setActivo(true);
    when(repositorioUsuarioMock.obtenerPorId(1L)).thenReturn(usuario);

    // ejecucion
    servicioUsuario.darDeBaja(1L);

    // validacion
    assertThat(usuario.getActivo(), is(false));
    verify(repositorioUsuarioMock, times(1)).modificar(usuario);
  }

  @Test
  public void darDeBajaUsuarioInexistenteDeberiaLanzarExcepcion() {
    // preparacion
    when(repositorioUsuarioMock.obtenerPorId(99L)).thenReturn(null);

    // ejecucion y validacion
    assertThrows(IllegalArgumentException.class, () -> servicioUsuario.darDeBaja(99L));
  }

  @Test
  public void editarUsuarioConPasswordValidaDeberiaActualizarlaPassword() throws Exception {
    // preparacion
    Usuario usuario = new Usuario();
    usuario.setEmail("test@test.com");
    usuario.setPassword("Vieja01");
    when(repositorioUsuarioMock.obtenerPorId(1L)).thenReturn(usuario);

    UsuarioDto dto = new UsuarioDto();
    dto.setEmail("test@test.com");
    dto.setPassword("Nueva01");
    dto.setRol("USER");

    // ejecucion
    servicioUsuario.editarUsuario(1L, dto);

    // validacion
    assertThat(usuario.getPassword(), equalTo("Nueva01"));
    verify(repositorioUsuarioMock, times(1)).modificar(usuario);
  }

  @Test
  public void editarUsuarioConPasswordInvalidaDeberiaLanzarExcepcion() {
    // preparacion
    Usuario usuario = new Usuario();
    when(repositorioUsuarioMock.obtenerPorId(1L)).thenReturn(usuario);

    UsuarioDto dto = new UsuarioDto();
    dto.setEmail("test@test.com");
    dto.setPassword("invalida");

    // ejecucion y validacion
    assertThrows(
      com.tallerwebi.dominio.excepcion.PasswordInvalida.class,
      () -> servicioUsuario.editarUsuario(1L, dto)
    );
  }

  @Test
  public void editarUsuarioSinPasswordDeberiaNoModificarla() throws Exception {
    // preparacion
    Usuario usuario = new Usuario();
    usuario.setPassword("Original1");
    when(repositorioUsuarioMock.obtenerPorId(1L)).thenReturn(usuario);

    UsuarioDto dto = new UsuarioDto();
    dto.setEmail("test@test.com");
    dto.setPassword("");
    dto.setRol("USER");

    // ejecucion
    servicioUsuario.editarUsuario(1L, dto);

    // validacion
    assertThat(usuario.getPassword(), equalTo("Original1"));
  }

  @Test
  public void reactivarDeberiaActivarElUsuario() {
    // preparacion
    Usuario usuario = new Usuario();
    usuario.setActivo(false);
    when(repositorioUsuarioMock.obtenerPorId(1L)).thenReturn(usuario);

    // ejecucion
    servicioUsuario.reactivar(1L);

    // validacion
    assertThat(usuario.getActivo(), is(true));
    verify(repositorioUsuarioMock, times(1)).modificar(usuario);
  }

  @Test
  public void reactivarUsuarioInexistenteDeberiaLanzarExcepcion() {
    // preparacion
    when(repositorioUsuarioMock.obtenerPorId(99L)).thenReturn(null);

    // ejecucion y validacion
    assertThrows(IllegalArgumentException.class, () -> servicioUsuario.reactivar(99L));
  }

  @Test
  public void asignarCategoriaAUsuarioExistenteDeberiaAsignarlaYActivarUsuario() {
    // preparacion
    Usuario usuario = new Usuario();
    usuario.setActivo(false);
    Categoria categoria = new Categoria("icono", true, "Electrónica");
    categoria.setId(1L);

    when(repositorioUsuarioMock.obtenerPorId(1L)).thenReturn(usuario);
    when(repositorioCategoriasMock.buscarPorId(1L)).thenReturn(categoria);

    // ejecucion
    servicioUsuario.asignarCategoria(1L, 1L);

    // validacion
    assertThat(usuario.getCategoria(), equalTo(categoria));
    assertThat(usuario.getActivo(), is(true));
    verify(repositorioUsuarioMock, times(1)).modificar(usuario);
  }

  @Test
  public void asignarCategoriaAUsuarioInexistenteDeberiaLanzarExcepcion() {
    // preparacion
    when(repositorioUsuarioMock.obtenerPorId(99L)).thenReturn(null);

    // ejecucion y validacion
    assertThrows(IllegalArgumentException.class, () -> servicioUsuario.asignarCategoria(99L, 1L));
  }

  @Test
  public void asignarCategoriaInexistenteDeberiaLanzarExcepcion() {
    // preparacion
    Usuario usuario = new Usuario();
    when(repositorioUsuarioMock.obtenerPorId(1L)).thenReturn(usuario);
    when(repositorioCategoriasMock.buscarPorId(99L)).thenReturn(null);

    // ejecucion y validacion
    assertThrows(IllegalArgumentException.class, () -> servicioUsuario.asignarCategoria(1L, 99L));
  }

  private UsuarioDto dtoValido() {
    UsuarioDto dto = new UsuarioDto();
    dto.setEmail("test@test.com");
    dto.setPassword("Valida1");
    dto.setRol("USER");
    return dto;
  }
}
