package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Cliente;
import com.tallerwebi.dominio.entity.DetallePedido;
import com.tallerwebi.dominio.entity.DetallePedidoIngrediente;
import com.tallerwebi.dominio.entity.Pedido;
import com.tallerwebi.dominio.interfaces.RepositorioLote;
import com.tallerwebi.dominio.interfaces.RepositorioPedido;
import com.tallerwebi.dominio.interfaces.ServicioHistorialPedido;
import com.tallerwebi.presentacion.dto.HistorialPedidoDTO;
import com.tallerwebi.presentacion.dto.IngredienteUsadoDTO;
import com.tallerwebi.presentacion.dto.ItemPedidoDTO;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioHistorialPedido")
@Transactional(readOnly = true)
public class ServicioHistorialPedidoImpl implements ServicioHistorialPedido {

  private final RepositorioPedido repositorioPedido;
  private final RepositorioLote repositorioLote;

  @Autowired
  public ServicioHistorialPedidoImpl(
    RepositorioPedido repositorioPedido,
    RepositorioLote repositorioLote
  ) {
    this.repositorioPedido = repositorioPedido;
    this.repositorioLote = repositorioLote;
  }

  @Override
  public List<HistorialPedidoDTO> buscarHistorial(
    OffsetDateTime desde,
    OffsetDateTime hasta,
    Long numeroDeLote,
    String clienteNombre
  ) {
    return repositorioPedido
      .listarTodos()
      .stream()
      .filter(pedido -> coincideConFecha(pedido, desde, hasta))
      .filter(pedido -> coincideConCliente(pedido, clienteNombre))
      .filter(pedido -> coincideConNumeroDeLote(pedido, numeroDeLote))
      .map(this::mapearPedido)
      .collect(Collectors.toList());
  }

  @Override
  public HistorialPedidoDTO buscarPorId(Long idPedido) {
    if (idPedido == null) {
      return null;
    }
    Pedido pedido = repositorioPedido.buscarPorId(idPedido);
    return pedido != null ? mapearPedido(pedido) : null;
  }

  @Override
  public List<HistorialPedidoDTO> buscarPorCliente(Long idCliente) {
    return repositorioPedido
      .listarPorCliente(idCliente)
      .stream()
      .map(this::mapearPedido)
      .collect(Collectors.toList());
  }

  private boolean coincideConFecha(Pedido pedido, OffsetDateTime desde, OffsetDateTime hasta) {
    if (desde == null && hasta == null) {
      return true;
    }
    OffsetDateTime horaCobro = pedido.getHoraCobro();
    if (horaCobro == null) {
      return false;
    }
    if (desde != null && horaCobro.isBefore(desde)) {
      return false;
    }
    return hasta == null || !horaCobro.isAfter(hasta);
  }

  private boolean coincideConCliente(Pedido pedido, String clienteNombre) {
    if (clienteNombre == null || clienteNombre.isBlank()) {
      return true;
    }
    Cliente cliente = pedido.getCliente();
    return (
      cliente != null &&
      cliente.getNombre() != null &&
      cliente.getNombre().toLowerCase(Locale.ROOT).contains(clienteNombre.toLowerCase(Locale.ROOT))
    );
  }

  private boolean coincideConNumeroDeLote(Pedido pedido, Long numeroDeLote) {
    if (numeroDeLote == null) {
      return true;
    }
    return pedido
      .getDetalles()
      .stream()
      .flatMap(detalle -> detalle.getIngredientes().stream())
      .flatMap(ingrediente -> obtenerTimerIdsDeIngrediente(ingrediente).stream())
      .anyMatch(timerId -> timerUsoNumeroDeLote(timerId, numeroDeLote));
  }

  private boolean timerUsoNumeroDeLote(Long timerId, Long numeroDeLote) {
    return repositorioLote
      .obtenerLotesPorTimer(timerId)
      .stream()
      .anyMatch(lote -> numeroDeLote.equals(lote.getNumeroDeLote()));
  }

  private HistorialPedidoDTO mapearPedido(Pedido pedido) {
    Cliente cliente = pedido.getCliente();
    List<ItemPedidoDTO> items = pedido
      .getDetalles()
      .stream()
      .map(this::mapearItem)
      .collect(Collectors.toList());

    return new HistorialPedidoDTO(
      pedido.getId(),
      cliente != null ? cliente.getId() : null,
      cliente != null ? cliente.getNombre() : "Sin cliente",
      pedido.getEstado() != null ? pedido.getEstado().name() : null,
      pedido.getHoraCobro() != null ? pedido.getHoraCobro().toString() : null,
      pedido.getHoraSalida() != null ? pedido.getHoraSalida().toString() : null,
      items
    );
  }

  private ItemPedidoDTO mapearItem(DetallePedido detalle) {
    List<IngredienteUsadoDTO> ingredientes = detalle
      .getIngredientes()
      .stream()
      .map(this::mapearIngrediente)
      .collect(Collectors.toList());

    return new ItemPedidoDTO(
      detalle.getProductoFinal() != null ? detalle.getProductoFinal().getNombre() : "-",
      ingredientes
    );
  }

  private IngredienteUsadoDTO mapearIngrediente(DetallePedidoIngrediente ingrediente) {
    List<Long> timers = obtenerTimerIdsDeIngrediente(ingrediente);
    return new IngredienteUsadoDTO(
      ingrediente.getProducto().getNombre(),
      ingrediente.getCantidad(),
      timers
    );
  }

  private List<Long> obtenerTimerIdsDeIngrediente(DetallePedidoIngrediente ingrediente) {
    return ingrediente
      .getConsumos()
      .stream()
      .map(consumo -> consumo.getTimer().getId())
      .distinct()
      .collect(Collectors.toList());
  }
}
