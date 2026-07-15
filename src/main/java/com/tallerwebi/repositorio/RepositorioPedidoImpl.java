package com.tallerwebi.repositorio;

import com.tallerwebi.dominio.entity.Cliente;
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
  public List<Pedido> buscarPorCliente(Cliente cliente) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select distinct p from Pedido p left join fetch p.detalles where p.cliente = :cliente order by p.horaCobro desc",
        Pedido.class
      )
      .setParameter("cliente", cliente)
      .list();
  }
}
