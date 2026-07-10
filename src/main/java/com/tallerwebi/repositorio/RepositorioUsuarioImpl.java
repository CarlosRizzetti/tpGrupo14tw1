package com.tallerwebi.repositorio;

import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioUsuario")
public class RepositorioUsuarioImpl implements RepositorioUsuario {

  private SessionFactory sessionFactory;

  @Autowired
  public RepositorioUsuarioImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public Usuario buscarUsuario(String email) {
    return (Usuario) sessionFactory
      .getCurrentSession()
      .createCriteria(Usuario.class)
      .add(Restrictions.eq("email", email))
      .uniqueResult();
  }

  @Override
  public void guardar(Usuario usuario) {
    sessionFactory.getCurrentSession().save(usuario);
  }

  @Override
  public Usuario buscar(String email) {
    return (Usuario) sessionFactory
      .getCurrentSession()
      .createCriteria(Usuario.class)
      .add(Restrictions.eq("email", email))
      .uniqueResult();
  }

  @Override
  public Usuario buscarPorTokenValidacion(String tokenValidacion) {
    return (Usuario) sessionFactory
      .getCurrentSession()
      .createCriteria(Usuario.class)
      .add(Restrictions.eq("tokenValidacion", tokenValidacion))
      .uniqueResult();
  }

  @Override
  public void modificar(Usuario usuario) {
    sessionFactory.getCurrentSession().update(usuario);
  }

  @Override
  public List<Usuario> listarTodos() {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT DISTINCT u FROM Usuario u LEFT JOIN FETCH u.categorias ORDER BY u.id",
        Usuario.class
      )
      .list();
  }

  @Override
  public List<Usuario> listarLosUsuariosDeLasCategorias(Long categoriaId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT DISTINCT u " +
        "FROM Usuario u " +
        "JOIN u.categorias c " +
        "WHERE c.id = :categoriaId",
        Usuario.class
      )
      .setParameter("categoriaId", categoriaId)
      .list();
  }

  @Override
  public Usuario obtenerPorId(Long id) {
    return sessionFactory.getCurrentSession().get(Usuario.class, id);
  }
}
