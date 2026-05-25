package com.tallerwebi.repositorio;

import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.interfaces.RepositorioProducto;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("repositorioProducto")
@Transactional
public class RepositorioProductoImpl implements RepositorioProducto {

  private final SessionFactory sessionFactory;

  public RepositorioProductoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Producto producto) {
    sessionFactory.getCurrentSession().saveOrUpdate(producto);
  }

  @Override
  public Producto obtenerProductoPorId(Long id) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT p FROM Producto p LEFT JOIN FETCH p.reglas WHERE p.id = :id",
        Producto.class
      )
      .setParameter("id", id)
      .uniqueResult();
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

  @Override
  public Producto obtenerProductoConReglasYCategorias(Long id) {
    return (Producto) sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT p FROM Producto p LEFT JOIN FETCH p.reglas LEFT JOIN FETCH p.categorias WHERE p.id = :id"
      )
      .setParameter("id", id)
      .uniqueResult();
  }
}
