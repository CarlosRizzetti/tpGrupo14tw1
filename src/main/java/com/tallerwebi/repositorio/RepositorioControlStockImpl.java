package com.tallerwebi.repositorio;

import com.tallerwebi.dominio.entity.ControlStock;
import com.tallerwebi.dominio.entity.enums.TipoMovimientoStock;
import com.tallerwebi.dominio.interfaces.RepositorioControlStock;
import java.time.OffsetDateTime;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("repositorioControlStock")
@Transactional
public class RepositorioControlStockImpl implements RepositorioControlStock {

  private static final String PARAM_DESDE = "desde";

  private final SessionFactory sessionFactory;

  public RepositorioControlStockImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(ControlStock controlStock) {
    sessionFactory.getCurrentSession().saveOrUpdate(controlStock);
  }

  @Override
  public List<ControlStock> obtenerPorProducto(Long productoId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT cs FROM ControlStock cs WHERE cs.producto.id = :productoId ORDER BY cs.fecha DESC",
        ControlStock.class
      )
      .setParameter("productoId", productoId)
      .list();
  }

  @Override
  public List<OffsetDateTime> obtenerFechasMovimientosDesde(OffsetDateTime desde) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT cs.fecha FROM ControlStock cs WHERE cs.fecha >= :desde ORDER BY cs.fecha ASC",
        OffsetDateTime.class
      )
      .setParameter(PARAM_DESDE, desde)
      .list();
  }

  @Override
  public List<OffsetDateTime> obtenerFechasEgresosDesde(OffsetDateTime desde) {
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
}
