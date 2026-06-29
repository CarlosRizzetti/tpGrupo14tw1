package com.tallerwebi.repositorio;

import com.tallerwebi.dominio.entity.Articulos;
import com.tallerwebi.dominio.interfaces.RepositorioArticulo;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioArticuloImpl implements RepositorioArticulo {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioArticuloImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Articulos> obtenerTodos() {
    return sessionFactory.getCurrentSession().createQuery("FROM Articulos").list();
  }

  @Override
  public void guardar(Articulos articulo) {
    sessionFactory.getCurrentSession().save(articulo);
  }

  @Override
  public Articulos buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(Articulos.class, id);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Articulos> buscarPorNombre(String nombre) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("FROM Articulos a WHERE lower(a.nombre) LIKE lower(:nombre)")
      .setParameter("nombre", "%" + nombre + "%")
      .setMaxResults(10)
      .list();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<com.tallerwebi.presentacion.dto.StockArticuloDto> obtenerStockAgrupadoPorNombre() {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT new com.tallerwebi.presentacion.dto.StockArticuloDto(a.nombre, SUM(a.cantidad)) " +
        "FROM Articulos a GROUP BY a.nombre",
        com.tallerwebi.presentacion.dto.StockArticuloDto.class
      )
      .list();
  }
}
