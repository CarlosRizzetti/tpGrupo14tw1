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

  @Override
  public Timer buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(Timer.class, id);
  }

  @Override
  public void guardar(Timer timer) {
    sessionFactory.getCurrentSession().save(timer);
  }

  @Override
  public boolean existeTimerActivoEnCategoriaYGrupo(Long categoriaId, String groupId) {
    String hql =
      " SELECT COUNT(t) > 0 FROM Timer t WHERE t.categoria.id = :categoriaId AND t.groupId = :groupId AND t.estaActivo = true";

    return sessionFactory
      .getCurrentSession()
      .createQuery(hql, Boolean.class)
      .setParameter("categoriaId", categoriaId)
      .setParameter("groupId", groupId)
      .getSingleResult();
  }
}
