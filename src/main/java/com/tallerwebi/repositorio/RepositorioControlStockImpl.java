package com.tallerwebi.repositorio;

import com.tallerwebi.dominio.entity.ControlStock;
import com.tallerwebi.dominio.interfaces.RepositorioControlStock;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("repositorioControlStock")
@Transactional
public class RepositorioControlStockImpl implements RepositorioControlStock {

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
}
