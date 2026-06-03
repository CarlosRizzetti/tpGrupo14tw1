package com.tallerwebi.repositorio;

import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.interfaces.RepositorioReglaVencimiento;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("repositorioReglaVencimiento")
@Transactional
public class RepositorioReglaVencimientoImpl implements RepositorioReglaVencimiento {

  private final SessionFactory sessionFactory;

  public RepositorioReglaVencimientoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(ReglaVencimiento reglaVencimiento) {
    sessionFactory.getCurrentSession().saveOrUpdate(reglaVencimiento);
  }

  @Override
  public ReglaVencimiento obtenerReglaVencimientoPorId(Long id) {
    return sessionFactory.getCurrentSession().get(ReglaVencimiento.class, id);
  }
}
