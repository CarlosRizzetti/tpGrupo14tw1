package com.tallerwebi.repositorio;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

@Repository("repositorioCategoria")
public class RepositorioCategoriaImpl implements RepositorioCategoria {

  public SessionFactory sessionFactory;

  public RepositorioCategoriaImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public Set<Categoria> obtenerTodasLasCategoriasActivas() {
    List<Categoria> resultado = sessionFactory
      .getCurrentSession()
      .createCriteria(Categoria.class)
      .add(Restrictions.eq("estaActiva", true))
      .addOrder(Order.asc("id"))
      .list();
    return new TreeSet<>(resultado);
  }

  @Override
  public void agregarNuevaCategoria(Categoria categoria) {
    sessionFactory.getCurrentSession().save(categoria);
  }

  @Override
  public Categoria buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(Categoria.class, id);
  }

  @Override
  public Set<Categoria> obtenerCategoriasPorIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return java.util.Collections.emptySet();
    }
    return new TreeSet<>(
      sessionFactory
        .getCurrentSession()
        .createQuery("FROM Categoria c WHERE c.id IN (:ids)", Categoria.class)
        .setParameter("ids", ids)
        .list()
    );
  }
}
