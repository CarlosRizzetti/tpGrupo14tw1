package com.tallerwebi.repositorio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.Receta;
import java.util.Collections;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RepositorioRecetaTest {

  private RepositorioRecetaImpl repositorioReceta;
  private SessionFactory sessionFactoryMock;
  private Session sessionMock;

  @BeforeEach
  public void init() {
    sessionFactoryMock = mock(SessionFactory.class);
    sessionMock = mock(Session.class);
    when(sessionFactoryMock.getCurrentSession()).thenReturn(sessionMock);
    repositorioReceta = new RepositorioRecetaImpl(sessionFactoryMock);
  }

  @Test
  public void buscarPorProductoDeberiaRetornarReceta() {
    Producto producto = new Producto();
    Receta recetaEsperada = new Receta();
    Query<Receta> queryMock = mock(Query.class);

    when(
      sessionMock.createQuery(
        "FROM Receta r JOIN FETCH r.ingredientes WHERE r.producto = :producto",
        Receta.class
      )
    )
      .thenReturn(queryMock);
    when(queryMock.setParameter("producto", producto)).thenReturn(queryMock);
    when(queryMock.uniqueResult()).thenReturn(recetaEsperada);

    Receta resultado = repositorioReceta.buscarPorProducto(producto);

    assertEquals(recetaEsperada, resultado);
    verify(queryMock).setParameter("producto", producto);
    verify(queryMock).uniqueResult();
  }

  @Test
  public void guardarDeberiaLlamarASessionSaveOrUpdate() {
    Receta receta = new Receta();
    repositorioReceta.guardar(receta);
    verify(sessionMock, times(1)).saveOrUpdate(receta);
  }

  @Test
  public void obtenerTodasDeberiaRetornarListaDeRecetas() {
    Query<Receta> queryMock = mock(Query.class);
    List<Receta> listaEsperada = Collections.singletonList(new Receta());

    when(sessionMock.createQuery("SELECT DISTINCT r FROM Receta r", Receta.class))
      .thenReturn(queryMock);
    when(queryMock.list()).thenReturn(listaEsperada);

    List<Receta> resultado = repositorioReceta.obtenerTodas();

    assertEquals(listaEsperada, resultado);
    verify(sessionMock).createQuery("SELECT DISTINCT r FROM Receta r", Receta.class);
    verify(queryMock).list();
  }
}
