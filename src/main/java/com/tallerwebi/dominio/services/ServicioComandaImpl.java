package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Comanda;
import com.tallerwebi.dominio.entity.ComandaSector;
import com.tallerwebi.dominio.entity.ConsumoTimer;
import com.tallerwebi.dominio.entity.DetallePedido;
import com.tallerwebi.dominio.entity.DetallePedidoIngrediente;
import com.tallerwebi.dominio.entity.Pedido;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.enums.EstadoComanda;
import com.tallerwebi.dominio.entity.enums.EstadoComandaSector;
import com.tallerwebi.dominio.entity.enums.EstadoPedido;
import com.tallerwebi.dominio.excepcion.IngredientesNoDisponiblesException;
import com.tallerwebi.dominio.interfaces.RepositorioComanda;
import com.tallerwebi.dominio.interfaces.RepositorioComandaSector;
import com.tallerwebi.dominio.interfaces.ServicioComanda;
import com.tallerwebi.dominio.interfaces.ServicioTimer;
import com.tallerwebi.presentacion.dto.ComandaCocinaDTO;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioComanda")
@Transactional
public class ServicioComandaImpl implements ServicioComanda {

  private static final String NOMBRE_CATEGORIA_COCINA = "Cocina";

  private final RepositorioComanda repositorioComanda;
  private final RepositorioComandaSector repositorioComandaSector;
  private final ServicioTimer servicioTimer;

  @Autowired
  public ServicioComandaImpl(
    RepositorioComanda repositorioComanda,
    RepositorioComandaSector repositorioComandaSector,
    ServicioTimer servicioTimer
  ) {
    this.repositorioComanda = repositorioComanda;
    this.repositorioComandaSector = repositorioComandaSector;
    this.servicioTimer = servicioTimer;
  }

  private static class PlanDeConsumo {

    final Timer timer;
    final int cantidad;

    PlanDeConsumo(Timer timer, int cantidad) {
      this.timer = timer;
      this.cantidad = cantidad;
    }
  }

  // ========================================================
  // Creación de sectores
  // ========================================================

  @Override
  @Transactional
  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  public void crearSectoresDeComanda(Comanda comanda) {
    Set<Categoria> categoriasDelPedido = obtenerCategoriasDelPedido(comanda.getPedido());
    boolean tieneCocina = categoriasDelPedido.stream().anyMatch(this::esCategoriaCocina);

    for (Categoria categoria : categoriasDelPedido) {
      boolean esCocina = esCategoriaCocina(categoria);
      EstadoComandaSector estadoInicial = (!tieneCocina || esCocina)
        ? EstadoComandaSector.PENDIENTE
        : EstadoComandaSector.BLOQUEADO;

      ComandaSector sector = new ComandaSector();
      sector.setComanda(comanda);
      sector.setCategoria(categoria);
      sector.setEstado(estadoInicial);
      repositorioComandaSector.guardar(sector);
    }
  }

  private Set<Categoria> obtenerCategoriasDelPedido(Pedido pedido) {
    Set<Categoria> categorias = new HashSet<>();
    for (DetallePedido detalle : pedido.getDetalles()) {
      categorias.addAll(detalle.getProductoFinal().getCategorias());
    }
    return categorias;
  }

  private boolean esCategoriaCocina(Categoria categoria) {
    return NOMBRE_CATEGORIA_COCINA.equalsIgnoreCase(categoria.getNombre());
  }

  // ========================================================
  // Servir un sector
  // ========================================================

  @Override
  @Transactional
  public void servirSector(Long idSector) throws IngredientesNoDisponiblesException {
    ComandaSector sector = repositorioComandaSector.buscarPorId(idSector);
    Comanda comanda = sector.getComanda();
    Pedido pedido = comanda.getPedido();

    List<DetallePedidoIngrediente> ingredientesDelSector = obtenerIngredientesDelSector(
      pedido,
      sector.getCategoria()
    );

    Map<DetallePedidoIngrediente, List<PlanDeConsumo>> plan = planificarConsumos(
      ingredientesDelSector
    );
    validarPlanCompleto(plan, ingredientesDelSector);

    ejecutarPlan(plan);
    marcarSectorComoServido(sector);
    desbloquearSectoresSiCorresponde(comanda, sector);
    finalizarPedidoSiTodosLosSectoresSirvieron(comanda);
  }

  private List<DetallePedidoIngrediente> obtenerIngredientesDelSector(
    Pedido pedido,
    Categoria categoria
  ) {
    List<DetallePedidoIngrediente> ingredientes = new ArrayList<>();
    for (DetallePedido detalle : pedido.getDetalles()) {
      boolean perteneceAlSector = detalle
        .getProductoFinal()
        .getCategorias()
        .stream()
        .anyMatch(c -> c.getId().equals(categoria.getId()));
      if (perteneceAlSector) {
        for (DetallePedidoIngrediente ingrediente : detalle.getIngredientes()) {
          if (ingrediente.getConsumos().isEmpty()) {
            ingredientes.add(ingrediente);
          }
        }
      }
    }
    return ingredientes;
  }

  private void marcarSectorComoServido(ComandaSector sector) {
    sector.setEstado(EstadoComandaSector.SERVIDO);
    sector.setHoraServido(OffsetDateTime.now());
    repositorioComandaSector.actualizar(sector);
  }

  private void desbloquearSectoresSiCorresponde(
    Comanda comanda,
    ComandaSector sectorRecienServido
  ) {
    if (!esCategoriaCocina(sectorRecienServido.getCategoria())) {
      return;
    }

    for (ComandaSector otroSector : comanda.getSectores()) {
      if (otroSector.getEstado() == EstadoComandaSector.BLOQUEADO) {
        otroSector.setEstado(EstadoComandaSector.PENDIENTE);
        repositorioComandaSector.actualizar(otroSector);
      }
    }
  }

  private void finalizarPedidoSiTodosLosSectoresSirvieron(Comanda comanda) {
    boolean todosServidos = comanda
      .getSectores()
      .stream()
      .allMatch(s -> s.getEstado() == EstadoComandaSector.SERVIDO);

    if (!todosServidos) {
      return;
    }

    Pedido pedido = comanda.getPedido();
    pedido.setHoraSalida(OffsetDateTime.now());
    pedido.setEstado(EstadoPedido.ENTREGADO);

    comanda.setEstado(EstadoComanda.SACADA);
    repositorioComanda.actualizar(comanda);
  }

  // ========================================================
  // Listado / conteo por categoría
  // ========================================================

  @Override
  @Transactional(readOnly = true)
  public List<ComandaCocinaDTO> listarPendientesPorCategoria(Long idCategoria) {
    return repositorioComandaSector
      .listarVisiblesPorCategoria(idCategoria)
      .stream()
      .map(ComandaCocinaDTO::new)
      .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public int contarPendientesPorCategoria(Long idCategoria) {
    return repositorioComandaSector.listarVisiblesPorCategoria(idCategoria).size();
  }

  // ========================================================
  // Planificación de consumo de timers (sin cambios respecto a la versión anterior)
  // ========================================================

  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  private Map<DetallePedidoIngrediente, List<PlanDeConsumo>> planificarConsumos(
    List<DetallePedidoIngrediente> ingredientes
  ) {
    Map<DetallePedidoIngrediente, List<PlanDeConsumo>> plan = new HashMap<>();

    Map<Long, Integer> reservadoPorTimer = new HashMap<>();

    for (DetallePedidoIngrediente ingrediente : ingredientes) {
      List<PlanDeConsumo> consumos = planificarConsumoParaIngrediente(
        ingrediente,
        reservadoPorTimer
      );
      if (consumos != null) {
        plan.put(ingrediente, consumos);
      }
    }
    return plan;
  }

  private List<PlanDeConsumo> planificarConsumoParaIngrediente(
    DetallePedidoIngrediente ingrediente,
    Map<Long, Integer> reservadoPorTimer
  ) {
    List<Timer> timersActivos = servicioTimer.obtenerTimersActivosConStockPorProducto(
      ingrediente.getProducto().getId()
    );

    List<PlanDeConsumo> consumos = new ArrayList<>();
    int necesario = ingrediente.getCantidad();

    for (Timer timer : timersActivos) {
      if (necesario == 0) break;

      int yaReservado = reservadoPorTimer.getOrDefault(timer.getId(), 0);
      int disponibleReal = timer.getCantidadProducto() - yaReservado;
      if (disponibleReal <= 0) continue;

      int aConsumir = Math.min(necesario, disponibleReal);
      consumos.add(new PlanDeConsumo(timer, aConsumir));
      necesario -= aConsumir;

      // Registra la reserva para las próximas planificaciones
      reservadoPorTimer.merge(timer.getId(), aConsumir, Integer::sum);
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
    servicioTimer.descontarStock(consumo.timer.getId(), consumo.cantidad);
    registrarConsumoEnIngrediente(ingrediente, consumo);
  }

  private void registrarConsumoEnIngrediente(
    DetallePedidoIngrediente ingrediente,
    PlanDeConsumo consumo
  ) {
    ConsumoTimer registro = new ConsumoTimer();
    registro.setDetallePedidoIngrediente(ingrediente);
    registro.setTimer(consumo.timer);
    registro.setCantidadConsumida(consumo.cantidad);
    ingrediente.getConsumos().add(registro);
  }
}
