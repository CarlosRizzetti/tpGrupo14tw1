package com.tallerwebi.repositorio;

import com.tallerwebi.dominio.entity.Lote;
import com.tallerwebi.dominio.entity.enums.EstadoLote;
import com.tallerwebi.dominio.interfaces.RepositorioLote;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioLote")
public class RepositorioLoteImpl implements RepositorioLote {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioLoteImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public Lote buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(Lote.class, id);
  }

  @Override
  public void guardar(Lote lote) {
    sessionFactory.getCurrentSession().save(lote);
  }

  @Override
  public void actualizar(Lote lote) {
    sessionFactory.getCurrentSession().update(lote);
  }

  @Override
  public List<Lote> listarTodos() {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from Lote l order by l.producto.nombre asc, l.fechaDeVencimiento asc",
        Lote.class
      )
      .list();
  }

  @Override
  public List<Lote> obtenerLotesPorTimer(Long idTimer) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select distinct cl.lote from ConsumoLote cl where cl.timer.id = :idTimer",
        Lote.class
      )
      .setParameter("idTimer", idTimer)
      .list();
  }

  @Override
  public List<Lote> listarConsumiblesDeProducto(Long idProducto) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from Lote l where l.producto.id = :idProducto " +
        "and l.estado in (:estados) " +
        "and l.cantidadDisponible > 0 " +
        "order by l.fechaDeVencimiento asc",
        Lote.class
      )
      .setParameter("idProducto", idProducto)
      .setParameterList("estados", Arrays.asList(EstadoLote.DISPONIBLE, EstadoLote.EN_USO))
      .list();
  }

  @Override
  public Lote buscarEnUsoDeProducto(Long idProducto) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from Lote l where l.producto.id = :idProducto and l.estado = :estado",
        Lote.class
      )
      .setParameter("idProducto", idProducto)
      .setParameter("estado", EstadoLote.EN_USO)
      .uniqueResult();
  }

  @Override
  public List<OffsetDateTime> obtenerFechasIngresoDesde(OffsetDateTime desde) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select l.fechaDeIngreso from Lote l where l.fechaDeIngreso >= :desde",
        OffsetDateTime.class
      )
      .setParameter("desde", desde)
      .list();
  }
}
