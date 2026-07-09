package com.tallerwebi.repositorio;

import com.tallerwebi.dominio.entity.Comanda;
import com.tallerwebi.dominio.entity.enums.EstadoComanda;
import com.tallerwebi.dominio.interfaces.RepositorioComanda;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("repositorioComanda")
@Transactional
public class RepositorioComandaImpl implements RepositorioComanda {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioComandaImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public Comanda buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(Comanda.class, id);
  }

  @Override
  public List<Comanda> listarPendientes() {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from Comanda c where c.estado = :estado order by c.pedido.horaCobro asc",
        Comanda.class
      )
      .setParameter("estado", EstadoComanda.PENDIENTE)
      .list();
  }

  @Override
  public List<Comanda> listarPendientesPorCategoria(Long idCategoria) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select distinct c from Comanda c " +
        "join c.pedido.detalles d " +
        "join d.productoFinal.categorias cat " +
        "where c.estado = :estado and cat.id = :idCategoria " +
        "order by c.id asc",
        Comanda.class
      )
      .setParameter("estado", EstadoComanda.PENDIENTE)
      .setParameter("idCategoria", idCategoria)
      .list();
  }

  @Override
  public void actualizar(Comanda comanda) {
    sessionFactory.getCurrentSession().update(comanda);
  }
}
