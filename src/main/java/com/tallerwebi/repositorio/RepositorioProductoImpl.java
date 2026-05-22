package com.tallerwebi.repositorio;

import com.tallerwebi.dominio.entity.Categoria;
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
  public List<Categoria> obtenerCategoriasPorIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return java.util.Collections.emptyList();
    }
    return sessionFactory
      .getCurrentSession()
      .createQuery("FROM Categoria c WHERE c.id IN (:ids)", Categoria.class)
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

  @Override
  public Producto buscarPorId(Long id) {
    return null;
  }
}
