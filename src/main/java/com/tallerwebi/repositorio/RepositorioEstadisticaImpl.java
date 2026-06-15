package com.tallerwebi.repositorio;

import com.tallerwebi.dominio.entity.enums.TipoMovimientoStock;
import com.tallerwebi.dominio.interfaces.RepositorioEstadistica;
import java.time.OffsetDateTime;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación Hibernate del repositorio de estadísticas.
 */
@Repository("repositorioEstadistica")
@Transactional
public class RepositorioEstadisticaImpl implements RepositorioEstadistica {

  private static final String PARAM_DESDE = "desde";

  private final SessionFactory sessionFactory;

  public RepositorioEstadisticaImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public List<OffsetDateTime> obtenerFechasCreacionVencimientos(OffsetDateTime desde) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT t.fechaCreacion FROM Timer t " +
        "WHERE t.fechaCreacion IS NOT NULL AND t.fechaCreacion >= :desde " +
        "ORDER BY t.fechaCreacion ASC",
        OffsetDateTime.class
      )
      .setParameter(PARAM_DESDE, desde)
      .list();
  }

  @Override
  public List<OffsetDateTime> obtenerFechasModificacionesStock(OffsetDateTime desde) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT cs.fecha FROM ControlStock cs " + "WHERE cs.fecha >= :desde ORDER BY cs.fecha ASC",
        OffsetDateTime.class
      )
      .setParameter(PARAM_DESDE, desde)
      .list();
  }

  @Override
  public List<OffsetDateTime> obtenerFechasDemanda(OffsetDateTime desde) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT cs.fecha FROM ControlStock cs " +
        "WHERE cs.tipo = :tipo AND cs.fecha >= :desde ORDER BY cs.fecha ASC",
        OffsetDateTime.class
      )
      .setParameter("tipo", TipoMovimientoStock.EGRESO)
      .setParameter(PARAM_DESDE, desde)
      .list();
  }

  @Override
  public List<Object[]> obtenerConteoVencimientosPorProducto(OffsetDateTime desde) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT t.producto.nombre, COUNT(t) FROM Timer t " +
        "WHERE t.producto IS NOT NULL AND t.fechaCreacion >= :desde " +
        "GROUP BY t.producto.nombre ORDER BY COUNT(t) DESC",
        Object[].class
      )
      .setParameter(PARAM_DESDE, desde)
      .list();
  }
}
