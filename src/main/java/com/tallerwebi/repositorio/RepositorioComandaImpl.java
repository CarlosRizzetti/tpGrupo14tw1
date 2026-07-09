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
      .createQuery("from Comanda c where c.estado = :estado", Comanda.class)
      .setParameter("estado", EstadoComanda.PENDIENTE)
      .list();
  }

  @Override
  public void actualizar(Comanda comanda) {
    sessionFactory.getCurrentSession().update(comanda);
  }
}
