package com.tallerwebi.repositorio;

import com.tallerwebi.dominio.entity.Trazabilidad;
import com.tallerwebi.dominio.interfaces.RepositorioTrazabilidad;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioTrazabilidad")
public class RepositorioTrazabilidadImpl implements RepositorioTrazabilidad {

  private SessionFactory sessionFactory;

  @Autowired
  public RepositorioTrazabilidadImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Trazabilidad trazabilidad) {
    sessionFactory.getCurrentSession().saveOrUpdate(trazabilidad);
  }

  @Override
  public List<Trazabilidad> obtenerTodas() {
    return sessionFactory
      .getCurrentSession()
      .createQuery("FROM Trazabilidad t ORDER BY t.fechaGeneracion DESC", Trazabilidad.class)
      .list();
  }
}
