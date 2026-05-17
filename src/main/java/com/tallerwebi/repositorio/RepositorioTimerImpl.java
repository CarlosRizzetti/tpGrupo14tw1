package com.tallerwebi.repositorio;

import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository("RepositorioTimer")
public class RepositorioTimerImpl implements RepositorioTimer {

  public SessionFactory sessionFactory;

  public RepositorioTimerImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public List<Timer> obtenerTimersSegunEstado(Long id, String estado) {
    String hql =
      "FROM Timer t JOIN FETCH t.producto JOIN FETCH t.categoria WHERE t.estado = :estado AND t.categoria.id = :idCat";
    return sessionFactory
      .getCurrentSession()
      .createQuery(hql, Timer.class)
      .setParameter("estado", estado)
      .setParameter("idCat", id)
      .list();
  }
}
