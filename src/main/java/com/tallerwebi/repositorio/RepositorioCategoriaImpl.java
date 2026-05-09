package com.tallerwebi.repositorio;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

@Repository("repositorioCategoria")
public class RepositorioCategoriaImpl implements RepositorioCategoria {

  public SessionFactory sessionFactory;

  public RepositorioCategoriaImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public List<Categoria> obtenerTodasLasCategoriasActivas() {
    return (List<Categoria>) sessionFactory
      .getCurrentSession()
      .createCriteria(Categoria.class)
      .add(Restrictions.eq("estaActiva", true))
      .list();
  }

  public void agregarNuevaCategoria(Categoria categoria) {
    sessionFactory.getCurrentSession().save(categoria);
  }
}
