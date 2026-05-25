package com.tallerwebi.repositorio;

import static org.junit.jupiter.api.Assertions.*;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import com.tallerwebi.repositorio.config.HibernateInfraestructuraTestConfig;
import java.util.Set;
import javax.transaction.Transactional;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
@ActiveProfiles("test")
public class RepositorioCategoriaTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioCategoria repositorioCategoria;

  @BeforeEach
  public void init() {
    repositorioCategoria = new RepositorioCategoriaImpl(sessionFactory);
  }

  @Test
  @Transactional
  public void queDevuelvaTodasLasCategoriasActivas() {
    Categoria categoria1 = new Categoria()
      .builder()
      .id(1L)
      .nombre("McCafe")
      .estaActiva(true)
      .build();
    Categoria categoria2 = new Categoria()
      .builder()
      .id(2L)
      .nombre("Servicio")
      .estaActiva(true)
      .build();
    Categoria categoria3 = new Categoria()
      .builder()
      .id(3L)
      .nombre("Cocina")
      .estaActiva(false)
      .build();
    sessionFactory.getCurrentSession().save(categoria1);
    sessionFactory.getCurrentSession().save(categoria2);
    sessionFactory.getCurrentSession().save(categoria3);
    Set<Categoria> categoriasActivas = this.repositorioCategoria.obtenerTodasLasCategoriasActivas();
    assertEquals(2, categoriasActivas.size());
    assertTrue(categoriasActivas.contains(categoria1));
    assertTrue(categoriasActivas.contains(categoria2));
    assertFalse(categoriasActivas.contains(categoria3));
  }

  @Test
  @Transactional
  public void queSePuedaAgregarUnaNuevaCategoria() {
    Categoria categoria1 = new Categoria("mccafe.png", true, "McCafe");
    Set<Categoria> categorias = this.repositorioCategoria.obtenerTodasLasCategoriasActivas();
    assertEquals(0, categorias.size());
    this.repositorioCategoria.agregarNuevaCategoria(categoria1);
    categorias = this.repositorioCategoria.obtenerTodasLasCategoriasActivas();
    assertEquals(1, categorias.size());
  }
}
