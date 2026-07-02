package com.tallerwebi.repositorio;

import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Articulos;
import com.tallerwebi.presentacion.dto.StockArticuloDto;
import java.util.Collections;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RepositorioArticuloImplTest {

  private RepositorioArticuloImpl repositorioArticulo;
  private SessionFactory sessionFactoryMock;
  private Session sessionMock;

  @BeforeEach
  public void init() {
    sessionFactoryMock = mock(SessionFactory.class);
    sessionMock = mock(Session.class);
    when(sessionFactoryMock.getCurrentSession()).thenReturn(sessionMock);
    repositorioArticulo = new RepositorioArticuloImpl(sessionFactoryMock);
  }

  @Test
  public void obtenerTodosDeberiaRetornarLista() {
    Query queryMock = mock(Query.class);
    when(sessionMock.createQuery("FROM Articulos")).thenReturn(queryMock);
    when(queryMock.list()).thenReturn(Collections.singletonList(new Articulos()));

    repositorioArticulo.obtenerTodos();

    verify(sessionMock).createQuery("FROM Articulos");
  }

  @Test
  public void guardarDeberiaLlamarASessionSave() {
    Articulos articulo = new Articulos();
    repositorioArticulo.guardar(articulo);
    verify(sessionMock, times(1)).saveOrUpdate(articulo);
  }

  @Test
  public void buscarPorIdDeberiaRetornarArticulo() {
    repositorioArticulo.buscarPorId(1L);
    verify(sessionMock).get(Articulos.class, 1L);
  }

  @Test
  public void buscarPorNombreDeberiaRetornarLista() {
    Query queryMock = mock(Query.class);
    when(sessionMock.createQuery("FROM Articulos a WHERE lower(a.nombre) LIKE lower(:nombre)"))
      .thenReturn(queryMock);
    when(queryMock.setParameter("nombre", "%Test%")).thenReturn(queryMock);
    when(queryMock.setMaxResults(10)).thenReturn(queryMock);
    when(queryMock.list()).thenReturn(Collections.singletonList(new Articulos()));

    repositorioArticulo.buscarPorNombre("Test");

    verify(queryMock).setParameter("nombre", "%Test%");
  }

  @Test
  public void obtenerStockAgrupadoPorNombreDeberiaRetornarLista() {
    Query queryMock = mock(Query.class);
    String hql =
      "SELECT new com.tallerwebi.presentacion.dto.StockArticuloDto(a.nombre, SUM(a.cantidad)) " +
      "FROM Articulos a GROUP BY a.nombre";
    when(sessionMock.createQuery(hql, StockArticuloDto.class)).thenReturn(queryMock);
    when(queryMock.list()).thenReturn(Collections.singletonList(new StockArticuloDto()));

    repositorioArticulo.obtenerStockAgrupadoPorNombre();

    verify(sessionMock).createQuery(hql, StockArticuloDto.class);
  }
}
