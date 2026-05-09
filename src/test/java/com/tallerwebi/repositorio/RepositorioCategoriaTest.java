package com.tallerwebi.repositorio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import com.tallerwebi.repositorio.config.HibernateInfraestructuraTestConfig;
import java.util.List;
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
    Categoria categoria1 = new Categoria("mccafe.png", true, "McCafe");
    Categoria categoria2 = new Categoria("mccafe.png", true, "Servicio");
    Categoria categoria3 = new Categoria("mccafe.png", false, "Cocina");
    sessionFactory.getCurrentSession().save(categoria1);
    sessionFactory.getCurrentSession().save(categoria2);
    sessionFactory.getCurrentSession().save(categoria3);
    List<Categoria> categoriasActivas =
      this.repositorioCategoria.obtenerTodasLasCategoriasActivas();
    assertEquals(2, categoriasActivas.size());
    assertThat(categoria1.getNombre(), is(categoriasActivas.get(0).getNombre()));
  }

  @Test
  @Transactional
  public void queSePuedaAgregarUnaNuevaCategoria() {
    Categoria categoria1 = new Categoria("mccafe.png", true, "McCafe");
    List<Categoria> categorias = this.repositorioCategoria.obtenerTodasLasCategoriasActivas();
    assertEquals(0, categorias.size());
    this.repositorioCategoria.agregarNuevaCategoria(categoria1);
    categorias = this.repositorioCategoria.obtenerTodasLasCategoriasActivas();
    assertEquals(1, categorias.size());
  }
}
