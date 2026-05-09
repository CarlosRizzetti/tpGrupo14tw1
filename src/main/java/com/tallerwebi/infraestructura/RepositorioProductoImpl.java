package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Categoria;
import com.tallerwebi.dominio.Producto;
import com.tallerwebi.dominio.interfaces.RepositorioProducto;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository("repositorioProducto")
public class RepositorioProductoImpl implements RepositorioProducto {

  private final SessionFactory sessionFactory;

  public RepositorioProductoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Producto producto) {
    sessionFactory.getCurrentSession().save(producto);
  }

  @Override
  public List<Categoria> obtenerCategoriasPorIds(List<Long> ids) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("FROM Categoria WHERE id IN :ids", Categoria.class)
      .setParameter("ids", ids)
      .list();
  }
}
