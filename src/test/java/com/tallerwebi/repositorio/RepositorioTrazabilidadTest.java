package com.tallerwebi.repositorio;

import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Trazabilidad;
import java.util.Collections;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RepositorioTrazabilidadTest {

  private RepositorioTrazabilidadImpl repositorioTrazabilidad;
  private SessionFactory sessionFactoryMock;
  private Session sessionMock;

  @BeforeEach
  public void init() {
    sessionFactoryMock = mock(SessionFactory.class);
    sessionMock = mock(Session.class);
    when(sessionFactoryMock.getCurrentSession()).thenReturn(sessionMock);
    repositorioTrazabilidad = new RepositorioTrazabilidadImpl(sessionFactoryMock);
  }

  @Test
  public void guardarDeberiaLlamarASessionSaveOrUpdate() {
    Trazabilidad trazabilidad = new Trazabilidad();
    repositorioTrazabilidad.guardar(trazabilidad);
    verify(sessionMock, times(1)).saveOrUpdate(trazabilidad);
  }

  @Test
  public void obtenerTodasDeberiaRetornarListaDeTrazabilidadOrdenada() {
    Query<Trazabilidad> queryMock = mock(Query.class);
    when(
      sessionMock.createQuery(
        "FROM Trazabilidad t ORDER BY t.fechaGeneracion DESC",
        Trazabilidad.class
      )
    )
      .thenReturn(queryMock);
    when(queryMock.list()).thenReturn(Collections.singletonList(new Trazabilidad()));

    repositorioTrazabilidad.obtenerTodas();

    verify(sessionMock)
      .createQuery("FROM Trazabilidad t ORDER BY t.fechaGeneracion DESC", Trazabilidad.class);
    verify(queryMock).list();
  }
}
