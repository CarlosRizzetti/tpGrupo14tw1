package com.tallerwebi.repositorio;

import com.tallerwebi.dominio.entity.ComandaSector;
import com.tallerwebi.dominio.entity.enums.EstadoComandaSector;
import com.tallerwebi.dominio.interfaces.RepositorioComandaSector;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("repositorioComandaSector")
@Transactional
public class RepositorioComandaSectorImpl implements RepositorioComandaSector {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioComandaSectorImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public ComandaSector buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(ComandaSector.class, id);
  }

  @Override
  public List<ComandaSector> listarVisiblesPorCategoria(Long idCategoria) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from ComandaSector s " +
        "where s.categoria.id = :idCategoria and s.estado = :estado " +
        "order by s.comanda.id asc",
        ComandaSector.class
      )
      .setParameter("idCategoria", idCategoria)
      .setParameter("estado", EstadoComandaSector.PENDIENTE)
      .list();
  }

  @Override
  public void guardar(ComandaSector sector) {
    sessionFactory.getCurrentSession().saveOrUpdate(sector);
  }

  @Override
  public void actualizar(ComandaSector sector) {
    sessionFactory.getCurrentSession().update(sector);
  }
}
