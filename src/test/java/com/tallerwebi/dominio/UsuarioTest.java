package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Usuario;
import java.util.TreeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UsuarioTest {

  @Test
  @DisplayName(
    "HP-01 | getCategoriasIds | Devuelve los ids separados por coma y ordenados ascendente"
  )
  void getCategoriasIdsDeberiaDevolverLosIdsOrdenadosAscendente() {
    Usuario usuario = new Usuario();
    usuario.setCategorias(new TreeSet<>());
    usuario.getCategorias().add(crearCategoria(3L));
    usuario.getCategorias().add(crearCategoria(1L));
    usuario.getCategorias().add(crearCategoria(2L));

    assertEquals("1,2,3", usuario.getCategoriasIds());
  }

  @Test
  @DisplayName(
    "HP-02 | getCategoriasIds | Devuelve un solo id sin coma cuando tiene una única categoría"
  )
  void getCategoriasIdsDeberiaDevolverUnSoloIdSinComa() {
    Usuario usuario = new Usuario();
    usuario.setCategorias(new TreeSet<>());
    usuario.getCategorias().add(crearCategoria(5L));

    assertEquals("5", usuario.getCategoriasIds());
  }

  @Test
  @DisplayName("EDGE-01 | getCategoriasIds | Devuelve string vacío cuando categorias es null")
  void getCategoriasIdsDeberiaDevolverVacioCuandoCategoriasEsNull() {
    Usuario usuario = new Usuario();
    usuario.setCategorias(null);

    assertEquals("", usuario.getCategoriasIds());
  }

  @Test
  @DisplayName("EDGE-02 | getCategoriasIds | Devuelve string vacío cuando categorias está vacío")
  void getCategoriasIdsDeberiaDevolverVacioCuandoCategoriasEstaVacio() {
    Usuario usuario = new Usuario();
    usuario.setCategorias(new TreeSet<>());

    assertEquals("", usuario.getCategoriasIds());
  }

  private Categoria crearCategoria(Long id) {
    Categoria categoria = new Categoria();
    categoria.setId(id);
    return categoria;
  }
}
