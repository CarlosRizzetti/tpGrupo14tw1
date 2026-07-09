package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Comanda;
import com.tallerwebi.dominio.entity.ConsumoTimer;
import com.tallerwebi.dominio.entity.DetallePedido;
import com.tallerwebi.dominio.entity.DetallePedidoIngrediente;
import com.tallerwebi.dominio.entity.Pedido;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.enums.EstadoComanda;
import com.tallerwebi.dominio.entity.enums.EstadoPedido;
import com.tallerwebi.dominio.excepcion.IngredientesNoDisponiblesException;
import com.tallerwebi.dominio.interfaces.RepositorioComanda;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.dominio.interfaces.ServicioComanda;
import com.tallerwebi.presentacion.dto.ComandaCocinaDTO;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioComanda")
@Transactional
public class ServicioComandaImpl implements ServicioComanda {

  private final RepositorioComanda repositorioComanda;
  private final RepositorioTimer repositorioTimer;

  @Autowired
  public ServicioComandaImpl(
    RepositorioComanda repositorioComanda,
    RepositorioTimer repositorioTimer
  ) {
    this.repositorioComanda = repositorioComanda;
    this.repositorioTimer = repositorioTimer;
  }

  private static class PlanDeConsumo {

    final Timer timer;
    final int cantidad;

    PlanDeConsumo(Timer timer, int cantidad) {
      this.timer = timer;
      this.cantidad = cantidad;
    }
  }

  @Override
  @Transactional
  public void sacarComanda(Long comandaId) throws IngredientesNoDisponiblesException {
    Comanda comanda = repositorioComanda.buscarPorId(comandaId);
    List<DetallePedidoIngrediente> ingredientes = obtenerIngredientesDelPedido(comanda.getPedido());

    Map<DetallePedidoIngrediente, List<PlanDeConsumo>> plan = planificarConsumos(ingredientes);
    validarPlanCompleto(plan, ingredientes);

    ejecutarPlan(plan);
    finalizarPedido(comanda.getPedido());
    marcarComandaComoSacada(comanda);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ComandaCocinaDTO> listarPendientesPorCategoria(Long idCategoria) {
    return repositorioComanda
      .listarPendientesPorCategoria(idCategoria)
      .stream()
      .map(ComandaCocinaDTO::new)
      .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public int contarPendientesPorCategoria(Long idCategoria) {
    return repositorioComanda.listarPendientesPorCategoria(idCategoria).size();
  }

  private List<DetallePedidoIngrediente> obtenerIngredientesDelPedido(Pedido pedido) {
    List<DetallePedidoIngrediente> ingredientes = new ArrayList<>();
    for (DetallePedido detalle : pedido.getDetalles()) {
      ingredientes.addAll(detalle.getIngredientes());
    }
    return ingredientes;
  }

  private Map<DetallePedidoIngrediente, List<PlanDeConsumo>> planificarConsumos(
    List<DetallePedidoIngrediente> ingredientes
  ) {
    Map<DetallePedidoIngrediente, List<PlanDeConsumo>> plan = new HashMap<>();
    for (DetallePedidoIngrediente ingrediente : ingredientes) {
      List<PlanDeConsumo> consumos = planificarConsumoParaIngrediente(ingrediente);
      if (consumos != null) {
        plan.put(ingrediente, consumos);
      }
    }
    return plan;
  }

  private List<PlanDeConsumo> planificarConsumoParaIngrediente(
    DetallePedidoIngrediente ingrediente
  ) {
    List<Timer> timersActivos = repositorioTimer.obtenerTimersActivosConStockPorProducto(
      ingrediente.getProducto().getId()
    );

    List<PlanDeConsumo> consumos = new ArrayList<>();
    int necesario = ingrediente.getCantidad();

    for (Timer timer : timersActivos) {
      if (necesario == 0) break;
      int aConsumir = Math.min(necesario, timer.getCantidadProducto());
      consumos.add(new PlanDeConsumo(timer, aConsumir));
      necesario -= aConsumir;
    }

    return necesario > 0 ? null : consumos;
  }

  private void validarPlanCompleto(
    Map<DetallePedidoIngrediente, List<PlanDeConsumo>> plan,
    List<DetallePedidoIngrediente> ingredientes
  ) throws IngredientesNoDisponiblesException {
    List<Producto> faltantes = new ArrayList<>();
    for (DetallePedidoIngrediente ingrediente : ingredientes) {
      if (!plan.containsKey(ingrediente)) {
        faltantes.add(ingrediente.getProducto());
      }
    }
    if (!faltantes.isEmpty()) {
      throw new IngredientesNoDisponiblesException(faltantes);
    }
  }

  private void ejecutarPlan(Map<DetallePedidoIngrediente, List<PlanDeConsumo>> plan) {
    for (Map.Entry<DetallePedidoIngrediente, List<PlanDeConsumo>> entrada : plan.entrySet()) {
      for (PlanDeConsumo consumo : entrada.getValue()) {
        aplicarConsumo(entrada.getKey(), consumo);
      }
    }
  }

  private void aplicarConsumo(DetallePedidoIngrediente ingrediente, PlanDeConsumo consumo) {
    consumo.timer.setCantidadProducto(consumo.timer.getCantidadProducto() - consumo.cantidad);

    ConsumoTimer registro = new ConsumoTimer();
    registro.setDetallePedidoIngrediente(ingrediente);
    registro.setTimer(consumo.timer);
    registro.setCantidadConsumida(consumo.cantidad);
    ingrediente.getConsumos().add(registro);
  }

  private void finalizarPedido(Pedido pedido) {
    pedido.setHoraSalida(OffsetDateTime.now());
    pedido.setEstado(EstadoPedido.ENTREGADO);
  }

  private void marcarComandaComoSacada(Comanda comanda) {
    comanda.setEstado(EstadoComanda.SACADA);
    repositorioComanda.actualizar(comanda);
  }
}
