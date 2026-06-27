package com.tallerwebi.repositorio;

import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.enums.EstadoTimer;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import java.time.OffsetDateTime;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

@Repository("RepositorioTimer")
public class RepositorioTimerImpl implements RepositorioTimer {

  private static final String PARAM_DESDE = "desde";

  public SessionFactory sessionFactory;

  public RepositorioTimerImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public List<Timer> obtenerTimersSegunEstado(Long id, EstadoTimer estado) {
    String hql =
      "FROM Timer t JOIN FETCH t.producto JOIN FETCH t.categoria WHERE t.estado = :estado AND t.categoria.id = :idCat ORDER BY t.cicloVida.fechaVencimiento ASC";
    return sessionFactory
      .getCurrentSession()
      .createQuery(hql, Timer.class)
      .setParameter("estado", estado)
      .setParameter("idCat", id)
      .list();
  }

  @Override
  public List<Timer> obtenerTodosLosTimers() {
    String hql = "FROM Timer t ORDER BY t.cicloVida.fechaCreacion";
    return sessionFactory.getCurrentSession().createQuery(hql, Timer.class).list();
  }

  @Override
  public List<Timer> obtenerTimersConFiltro(EstadoTimer estado, Long categoriaId) {
    StringBuilder hql = new StringBuilder("FROM Timer t WHERE 1=1");

    if (estado != null) {
      hql.append(" AND t.estado = :estado");
    }

    if (categoriaId != null) {
      hql.append(" AND t.categoria.id = :categoriaId");
    }
    hql.append(" ORDER BY t.cicloVida.fechaCreacion DESC");

    Query<Timer> query = sessionFactory
      .getCurrentSession()
      .createQuery(hql.toString(), Timer.class);

    if (estado != null) {
      query.setParameter("estado", estado);
    }

    if (categoriaId != null) {
      query.setParameter("categoriaId", categoriaId);
    }

    return query.list();
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
      " SELECT COUNT(t) > 0 FROM Timer t WHERE t.categoria.id = :categoriaId AND t.groupId = :groupId AND t.estado = :estado";

    return sessionFactory
      .getCurrentSession()
      .createQuery(hql, Boolean.class)
      .setParameter("categoriaId", categoriaId)
      .setParameter("groupId", groupId)
      .setParameter("estado", EstadoTimer.ACTIVO)
      .getSingleResult();
  }

  @Override
  public List<OffsetDateTime> obtenerFechasCreacionDesde(OffsetDateTime desde) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT t.fechaCreacion FROM Timer t " +
        "WHERE t.fechaCreacion IS NOT NULL AND t.fechaCreacion >= :desde " +
        "ORDER BY t.fechaCreacion ASC",
        OffsetDateTime.class
      )
      .setParameter(PARAM_DESDE, desde)
      .list();
  }

  @Override
  public List<Object[]> contarVencimientosPorProducto(OffsetDateTime desde) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT t.producto.nombre, COUNT(t) FROM Timer t " +
        "WHERE t.producto IS NOT NULL AND t.fechaCreacion >= :desde " +
        "GROUP BY t.producto.nombre ORDER BY COUNT(t) DESC",
        Object[].class
      )
      .setParameter(PARAM_DESDE, desde)
      .list();
  }

  @Override
  public List<Object[]> contarPorEstado(OffsetDateTime desde) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT t.estado, COUNT(t) FROM Timer t " +
        "WHERE t.fechaCreacion >= :desde " +
        "GROUP BY t.estado",
        Object[].class
      )
      .setParameter(PARAM_DESDE, desde)
      .list();
  }
}
