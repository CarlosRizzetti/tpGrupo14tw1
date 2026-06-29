package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.dominio.services.ServicioCategoriaImpl;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioCategoriaTest {

  public ServicioCategoria servicioCategoria;
  public RepositorioCategoria repositorioCategoriaMock;
  public RepositorioUsuario repositorioUsuarioMock;

  @BeforeEach
  public void init() {
    this.repositorioCategoriaMock = mock(RepositorioCategoria.class);
    this.repositorioUsuarioMock = mock(RepositorioUsuario.class);
    this.servicioCategoria =
      new ServicioCategoriaImpl(repositorioCategoriaMock, repositorioUsuarioMock);
  }

  @Test
  public void queSePuedanObtenerTodasLasCategoriasActivas() {
    Categoria mccafe = new Categoria("mccafe.png", true, "mccafe");
    Categoria servicio = new Categoria("servicio.png", true, "servicio");
    Categoria cocina = new Categoria("cocina.png", true, "cocina");
    Set<Categoria> categorias = Set.of(mccafe, servicio, cocina);
    when(this.repositorioCategoriaMock.obtenerTodasLasCategoriasActivas()).thenReturn(categorias);
    List<CategoriaDto> categoriasActivas = this.servicioCategoria.obtenerLasCategoriasParaElMenu();
    assertEquals(3, categoriasActivas.size());
  }

  @Test
  public void queDevuelvaElDtoCorrecto() {
    Categoria categoria = Categoria
      .builder()
      .id(1L)
      .nombre("McCafe")
      .icono("mccafe.png")
      .estaActiva(true)
      .build();
    when(repositorioCategoriaMock.buscarPorId(1L)).thenReturn(categoria);

    CategoriaDto resultado = servicioCategoria.obtenerCategoriaPorId(1L);

    assertEquals(1L, resultado.getId());
    assertEquals("McCafe", resultado.getNombre());
    assertEquals("mccafe.png", resultado.getIcono());
  }

  @Test
  public void queObtenerCategoriasPorUsuarioDevuelvaLasDelUsuario() {
    com.tallerwebi.dominio.entity.Usuario usuario = new com.tallerwebi.dominio.entity.Usuario();
    Categoria categoria = new Categoria();
    categoria.setNombre("cat1");
    usuario.setCategorias(Set.of(categoria));

    when(repositorioUsuarioMock.buscar("test@test.com")).thenReturn(usuario);

    List<CategoriaDto> dtos = servicioCategoria.obtenerCategoriasPorUsuario("test@test.com");
    assertEquals(1, dtos.size());
    assertEquals("cat1", dtos.get(0).getNombre());
  }
}
