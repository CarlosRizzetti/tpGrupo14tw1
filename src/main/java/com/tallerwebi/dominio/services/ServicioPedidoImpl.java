package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Cliente;
import com.tallerwebi.dominio.entity.Comanda;
import com.tallerwebi.dominio.entity.DetallePedido;
import com.tallerwebi.dominio.entity.DetallePedidoIngrediente;
import com.tallerwebi.dominio.entity.Pedido;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ProductoFinal;
import com.tallerwebi.dominio.entity.enums.EstadoComanda;
import com.tallerwebi.dominio.entity.enums.EstadoPedido;
import com.tallerwebi.dominio.interfaces.RepositorioPedido;
import com.tallerwebi.dominio.interfaces.ServicioComanda;
import com.tallerwebi.dominio.interfaces.ServicioPedido;
import com.tallerwebi.dominio.utils.CarritoPedido;
import com.tallerwebi.dominio.utils.ItemCarrito;
import com.tallerwebi.dominio.utils.ItemCarritoIngrediente;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioPedido")
public class ServicioPedidoImpl implements ServicioPedido {

  private final RepositorioPedido repositorioPedido;
  private final ServicioComanda servicioComanda;

  @Autowired
  public ServicioPedidoImpl(RepositorioPedido repositorioPedido, ServicioComanda servicioComanda) {
    this.repositorioPedido = repositorioPedido;
    this.servicioComanda = servicioComanda;
  }

  @Override
  @Transactional
  public Pedido cobrarPedido(CarritoPedido carrito, Cliente cliente) {
    Pedido pedido = crearCabeceraPedido(cliente, carrito);
    agregarDetallesAlPedido(pedido, carrito);
    generarComanda(pedido);
    persistirYLimpiar(pedido, carrito);
    servicioComanda.crearSectoresDeComanda(pedido.getComanda());
    return pedido;
  }

  // ========================================================
  // Listado de pedidos de un cliente
  // ========================================================

  @Override
  @Transactional(readOnly = true)
  public List<Pedido> listarPedidosDeCliente(Cliente cliente) {
    if (cliente == null) {
      return Collections.emptyList();
    }
    List<Pedido> pedidos = repositorioPedido.buscarPorCliente(cliente);
    pedidos.forEach(this::inicializarDetallesCompletos);
    return pedidos;
  }

  private void inicializarDetallesCompletos(Pedido pedido) {
    if (pedido.getDetalles() == null) {
      return;
    }
    pedido.getDetalles().size();
    pedido.getDetalles().forEach(this::inicializarDetalleCompleto);
  }

  private void inicializarDetalleCompleto(DetallePedido detalle) {
    inicializarProductoFinal(detalle.getProductoFinal());
    inicializarIngredientesConProducto(detalle.getIngredientes());
  }

  private void inicializarProductoFinal(ProductoFinal productoFinal) {
    if (productoFinal != null) {
      productoFinal.getNombre();
    }
  }

  private void inicializarIngredientesConProducto(List<DetallePedidoIngrediente> ingredientes) {
    if (ingredientes == null) {
      return;
    }
    ingredientes.size();
    ingredientes.forEach(this::inicializarProductoDelIngrediente);
  }

  private void inicializarProductoDelIngrediente(DetallePedidoIngrediente ingrediente) {
    Producto producto = ingrediente.getProducto();
    if (producto != null) {
      producto.getNombre();
    }
  }

  // ========================================================
  // Búsqueda por id
  // ========================================================

  @Override
  @Transactional(readOnly = true)
  public Pedido buscarPedidoPorId(Long id) {
    if (id == null) {
      return null;
    }
    Pedido pedidoEncontrado = repositorioPedido.buscarPorId(id);
    inicializarIngredientesDelPedido(pedidoEncontrado);
    return pedidoEncontrado;
  }

  private void inicializarIngredientesDelPedido(Pedido pedido) {
    if (pedido == null || pedido.getDetalles() == null) {
      return;
    }
    pedido.getDetalles().size();
    pedido.getDetalles().forEach(this::inicializarIngredientesDelDetalle);
  }

  private void inicializarIngredientesDelDetalle(DetallePedido detalle) {
    if (detalle.getIngredientes() != null) {
      detalle.getIngredientes().size();
    }
  }

  // ========================================================
  // Reclamos
  // ========================================================

  @Override
  @Transactional
  public void marcarPedidoComoReportado(Long idPedido) {
    marcarPedidoComoReportado(idPedido, null, null);
  }

  @Override
  @Transactional
  public void marcarPedidoComoReportado(Long idPedido, String motivo, String comentario) {
    if (idPedido == null) {
      return;
    }
    Pedido pedidoEncontrado = repositorioPedido.buscarPorId(idPedido);
    if (pedidoEncontrado == null) {
      return;
    }
    aplicarReclamo(pedidoEncontrado, motivo, comentario);
    repositorioPedido.guardar(pedidoEncontrado);
  }

  private void aplicarReclamo(Pedido pedido, String motivo, String comentario) {
    pedido.setReportado(true);
    asignarSiNoEsVacio(motivo, pedido::setMotivoReclamo);
    asignarSiNoEsVacio(comentario, pedido::setComentarioReclamo);
  }

  private void asignarSiNoEsVacio(String valor, Consumer<String> setter) {
    if (valor != null && !valor.isEmpty()) {
      setter.accept(valor);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<Pedido> listarPedidosReportados() {
    return repositorioPedido.buscarPedidosReportados();
  }

  @Override
  @Transactional(readOnly = true)
  public int contarPedidosReportadosActivos() {
    List<Pedido> pedidosReportados = listarPedidosReportados();
    return pedidosReportados != null ? pedidosReportados.size() : 0;
  }

  @Override
  @Transactional
  public void resolverReclamoPedido(Long idPedido) {
    if (idPedido == null) {
      return;
    }
    Pedido pedidoEncontrado = repositorioPedido.buscarPorId(idPedido);
    if (pedidoEncontrado == null) {
      return;
    }
    pedidoEncontrado.setReportado(false);
    pedidoEncontrado.setEstado(EstadoPedido.RESUELTO);
    repositorioPedido.guardar(pedidoEncontrado);
  }

  // ========================================================
  // Armado del pedido a partir del carrito
  // ========================================================

  private Pedido crearCabeceraPedido(Cliente cliente, CarritoPedido carrito) {
    Pedido pedido = new Pedido();
    pedido.setCliente(cliente);
    pedido.setHoraCobro(OffsetDateTime.now());
    pedido.setPrecioFinal(carrito.calcularTotal());
    pedido.setEstado(EstadoPedido.EN_COCINA);
    return pedido;
  }

  private void agregarDetallesAlPedido(Pedido pedido, CarritoPedido carrito) {
    List<DetallePedido> detalles = carrito
      .getItems()
      .stream()
      .map(itemCarrito -> crearDetalleDesdeItem(itemCarrito, pedido))
      .collect(Collectors.toList());
    pedido.getDetalles().addAll(detalles);
  }

  private DetallePedido crearDetalleDesdeItem(ItemCarrito itemCarrito, Pedido pedido) {
    DetallePedido detalle = new DetallePedido();
    detalle.setPedido(pedido);
    detalle.setProductoFinal(itemCarrito.getProductoFinal());
    detalle.getIngredientes().addAll(crearIngredientesDelItem(itemCarrito, detalle));
    return detalle;
  }

  private List<DetallePedidoIngrediente> crearIngredientesDelItem(
    ItemCarrito itemCarrito,
    DetallePedido detalle
  ) {
    return itemCarrito
      .getIngredientes()
      .stream()
      .map(ingredienteCarrito -> crearIngredienteSiCorresponde(ingredienteCarrito, detalle))
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  private DetallePedidoIngrediente crearIngredienteSiCorresponde(
    ItemCarritoIngrediente ingredienteCarrito,
    DetallePedido detalle
  ) {
    if (ingredienteCarrito.fueRetiradoDelTodo()) {
      return null;
    }
    DetallePedidoIngrediente ingrediente = new DetallePedidoIngrediente();
    ingrediente.setDetallePedido(detalle);
    ingrediente.setProducto(ingredienteCarrito.getProducto());
    ingrediente.setCantidad(ingredienteCarrito.getCantidadActual());
    return ingrediente;
  }

  private void generarComanda(Pedido pedido) {
    Comanda comanda = new Comanda();
    comanda.setPedido(pedido);
    comanda.setEstado(EstadoComanda.PENDIENTE);
    pedido.setComanda(comanda);
  }

  private void persistirYLimpiar(Pedido pedido, CarritoPedido carrito) {
    repositorioPedido.guardar(pedido);
    carrito.vaciar();
  }

  @Override
  @Transactional
  public void actualizarPedido(Pedido pedido) {
    if (pedido != null) {
      repositorioPedido.guardar(pedido);
    }
  }
}
