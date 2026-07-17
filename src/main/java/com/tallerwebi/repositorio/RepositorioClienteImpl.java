package com.tallerwebi.repositorio;

import com.tallerwebi.dominio.entity.Cliente;
import com.tallerwebi.dominio.interfaces.RepositorioCliente;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("repositorioCliente")
@Transactional
public class RepositorioClienteImpl implements RepositorioCliente {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioClienteImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public Cliente buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(Cliente.class, id);
  }

  @Override
  public Cliente buscarPorDocumento(String documento) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Cliente c where c.documento = :documento", Cliente.class)
      .setParameter("documento", documento)
      .uniqueResult();
  }

  @Override
  public Cliente buscarPorEmail(String email) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Cliente c where c.email = :email", Cliente.class)
      .setParameter("email", email)
      .uniqueResult();
  }

  @Override
  public void guardar(Cliente cliente) {
    if (cliente.getId() != null) {
      sessionFactory.getCurrentSession().merge(cliente);
    } else {
      sessionFactory.getCurrentSession().saveOrUpdate(cliente);
    }
  }
}
