package com.tallerwebi.repositorio;

import com.tallerwebi.dominio.entity.ProductoFinal;
import com.tallerwebi.dominio.interfaces.RepositorioProductoFinal;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("repositorioProductoFinal")
@Transactional
public class RepositorioProductoFinalImpl implements RepositorioProductoFinal {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioProductoFinalImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public ProductoFinal buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(ProductoFinal.class, id);
  }

  @Override
  public List<ProductoFinal> listarTodos() {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from ProductoFinal", ProductoFinal.class)
      .list();
  }

  @Override
  public List<ProductoFinal> listarPorCategoria(Long idCategoria) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select distinct pf from ProductoFinal pf " +
        "join pf.categorias c where c.id = :idCategoria",
        ProductoFinal.class
      )
      .setParameter("idCategoria", idCategoria)
      .list();
  }

  @Override
  public void guardar(ProductoFinal productoFinal) {
    sessionFactory.getCurrentSession().saveOrUpdate(productoFinal);
  }
}
