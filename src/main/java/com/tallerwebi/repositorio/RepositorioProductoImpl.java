package com.tallerwebi.repositorio;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
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
  public Producto obtenerProductoPorId(Long id) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT p FROM Producto p LEFT JOIN FETCH p.categorias LEFT JOIN FETCH p.reglaVencimiento WHERE p.id = :id",
        Producto.class
      )
      .setParameter("id", id)
      .uniqueResult();
  }

  @Override
  public List<Categoria> obtenerCategoriasPorIds(List<Long> ids) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("FROM Categoria WHERE id IN :ids", Categoria.class)
      .setParameter("ids", ids)
      .list();
  }

  @Override
  public List<Producto> obtenerProductosPorCategoria(Long categoriaId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT p FROM Producto p JOIN p.categorias c WHERE c.id = :categoriaId AND p.estaActivo = true",
        Producto.class
      )
      .setParameter("categoriaId", categoriaId)
      .list();
  }
}
