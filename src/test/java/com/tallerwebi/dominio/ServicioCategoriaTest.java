package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.dominio.services.ServicioCategoriaImpl;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioCategoriaTest {

  public ServicioCategoria servicioCategoria;
  public RepositorioCategoria repositorioCategoriaMock;

  @BeforeEach
  public void init() {
    this.repositorioCategoriaMock = mock(RepositorioCategoria.class);
    this.servicioCategoria = new ServicioCategoriaImpl(repositorioCategoriaMock);
  }

  @Test
  public void queSePuedanObtenerTodasLasCategoriasActivas() {
    Categoria mccafe = new Categoria("mccafe.png", true, "mccafe");
    Categoria servicio = new Categoria("servicio.png", true, "servicio");
    Categoria cocina = new Categoria("cocina.png", true, "cocina");
    List<Categoria> categorias = List.of(mccafe, servicio, cocina);
    when(this.repositorioCategoriaMock.obtenerTodasLasCategoriasActivas()).thenReturn(categorias);
    List<CategoriaDto> categoriasActivas =
      this.servicioCategoria.obtenerLasCategoriasParaElMenu();
    assertEquals(3, categoriasActivas.size());
  }
}
