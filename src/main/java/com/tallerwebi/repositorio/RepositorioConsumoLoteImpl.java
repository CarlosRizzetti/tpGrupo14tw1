package com.tallerwebi.repositorio;

import com.tallerwebi.dominio.entity.ConsumoLote;
import com.tallerwebi.dominio.entity.Pedido;
import com.tallerwebi.dominio.interfaces.RepositorioConsumoLote;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioConsumoLote")
public class RepositorioConsumoLoteImpl implements RepositorioConsumoLote {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioConsumoLoteImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(ConsumoLote consumo) {
    sessionFactory.getCurrentSession().save(consumo);
  }

  @Override
  public List<Pedido> obtenerPedidosPorLote(Long idLote) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select distinct p " +
        "from ConsumoLote cl, ConsumoTimer ct " +
        "join ct.detallePedidoIngrediente dpi " +
        "join dpi.detallePedido dp " +
        "join dp.pedido p " +
        "where cl.lote.id = :idLote and ct.timer = cl.timer " +
        "order by p.id desc",
        Pedido.class
      )
      .setParameter("idLote", idLote)
      .list();
  }

  @Override
  public List<ConsumoLote> listarPorTimer(Long idTimer) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from ConsumoLote cl where cl.timer.id = :idTimer", ConsumoLote.class)
      .setParameter("idTimer", idTimer)
      .list();
  }
}
