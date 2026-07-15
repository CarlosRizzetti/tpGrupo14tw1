package com.tallerwebi.repositorio;

import com.tallerwebi.dominio.entity.Pedido;
import com.tallerwebi.dominio.interfaces.RepositorioPedido;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("repositorioPedido")
@Transactional
public class RepositorioPedidoImpl implements RepositorioPedido {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioPedidoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Pedido pedido) {
    sessionFactory.getCurrentSession().saveOrUpdate(pedido);
  }

  @Override
  public Pedido buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(Pedido.class, id);
  }

  @Override
  public List<Pedido> listarTodos() {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Pedido p order by p.horaCobro desc", Pedido.class)
      .list();
  }

  @Override
  public List<Pedido> listarPorCliente(Long idCliente) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from Pedido p where p.cliente.id = :idCliente order by p.horaCobro desc",
        Pedido.class
      )
      .setParameter("idCliente", idCliente)
      .list();
  }
}
