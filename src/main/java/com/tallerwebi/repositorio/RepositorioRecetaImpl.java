package com.tallerwebi.repositorio;

import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.Receta;
import com.tallerwebi.dominio.interfaces.RepositorioReceta;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioReceta")
public class RepositorioRecetaImpl implements RepositorioReceta {

  private SessionFactory sessionFactory;

  @Autowired
  public RepositorioRecetaImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public Receta buscarPorProducto(Producto producto) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT DISTINCT r FROM Receta r LEFT JOIN FETCH r.ingredientes WHERE r.producto = :producto",
        Receta.class
      )
      .setParameter("producto", producto)
      .uniqueResult();
  }

  @Override
  public void guardar(Receta receta) {
    sessionFactory.getCurrentSession().saveOrUpdate(receta);
  }

  @Override
  public java.util.List<Receta> obtenerTodas() {
    return sessionFactory
      .getCurrentSession()
      .createQuery("SELECT DISTINCT r FROM Receta r", Receta.class)
      .list();
  }
}
