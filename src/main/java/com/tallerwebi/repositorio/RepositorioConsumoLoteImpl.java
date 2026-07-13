package com.tallerwebi.repositorio;

import com.tallerwebi.dominio.entity.ConsumoLote;
import com.tallerwebi.dominio.interfaces.RepositorioConsumoLote;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioConsumoLote")
public class RepositorioConsumoLoteImpl implements RepositorioConsumoLote {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioConsumoLoteImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(ConsumoLote consumo) {
    sessionFactory.getCurrentSession().save(consumo);
  }
}
