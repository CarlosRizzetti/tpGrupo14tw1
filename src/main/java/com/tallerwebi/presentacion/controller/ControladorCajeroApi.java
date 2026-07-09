package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.Cliente;
import com.tallerwebi.dominio.entity.Pedido;
import com.tallerwebi.dominio.entity.ProductoFinal;
import com.tallerwebi.dominio.interfaces.ServicioCliente;
import com.tallerwebi.dominio.interfaces.ServicioPedido;
import com.tallerwebi.dominio.interfaces.ServicioProductoFinal;
import com.tallerwebi.dominio.utils.CarritoPedido;
import com.tallerwebi.dominio.utils.ItemCarrito;
import com.tallerwebi.presentacion.dto.CarritoDTO;
import com.tallerwebi.presentacion.dto.ProductoFinalDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cajero")
public class ControladorCajeroApi {

  private static final String ATRIBUTO_SESION_CARRITO = "carrito";

  private final ServicioProductoFinal servicioProductoFinal;
  private final ServicioPedido servicioPedido;
  private final ServicioCliente servicioCliente;

  @Autowired
  public ControladorCajeroApi(
    ServicioProductoFinal servicioProductoFinal,
    ServicioPedido servicioPedido,
    ServicioCliente servicioCliente
  ) {
    this.servicioProductoFinal = servicioProductoFinal;
    this.servicioPedido = servicioPedido;
    this.servicioCliente = servicioCliente;
  }

  @GetMapping("/productos")
  public List<ProductoFinalDTO> productosPorCategoria(@RequestParam Long idCategoria) {
    return servicioProductoFinal
      .listarPorCategoria(idCategoria)
      .stream()
      .map(ProductoFinalDTO::new)
      .collect(Collectors.toList());
  }

  @GetMapping("/carrito")
  public CarritoDTO verCarrito(HttpSession session) {
    return new CarritoDTO(obtenerCarritoDeSesion(session));
  }

  @PostMapping("/carrito/items")
  public CarritoDTO agregarItem(@RequestBody AgregarItemRequest request, HttpSession session) {
    ProductoFinal productoFinal = servicioProductoFinal.buscarPorId(request.getIdProductoFinal());
    ItemCarrito item = new ItemCarrito(productoFinal);

    if (request.getIngredientesRetiradosIds() != null) {
      for (Long idProducto : request.getIngredientesRetiradosIds()) {
        item.retirarIngrediente(idProducto);
      }
    }

    CarritoPedido carrito = obtenerCarritoDeSesion(session);
    carrito.agregarItem(item);
    return new CarritoDTO(carrito);
  }

  @DeleteMapping("/carrito/items/{idLinea}")
  public ResponseEntity<CarritoDTO> eliminarItem(@PathVariable int idLinea, HttpSession session) {
    CarritoPedido carrito = obtenerCarritoDeSesion(session);
    boolean eliminado = carrito.eliminarItem(idLinea);
    if (!eliminado) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(new CarritoDTO(carrito));
  }

  @PostMapping("/buscar-cliente")
  public Map<String, Object> buscarCliente(@RequestParam(required = false) String documento) {
    Cliente cliente = servicioCliente.buscarPorDocumento(documento);
    Map<String, Object> respuesta = new HashMap<>();
    if (cliente != null) {
      Map<String, Object> data = new HashMap<>();
      data.put("id", cliente.getId());
      data.put("nombre", cliente.getNombre());
      data.put("documento", cliente.getDocumento());
      respuesta.put("cliente", data);
    } else {
      respuesta.put("cliente", null);
    }
    return respuesta;
  }

  // POST /api/cajero/cobrar?documento=40123456 -> ejecuta el cobro y devuelve el ID del pedido
  @PostMapping("/cobrar")
  public ResponseEntity<Map<String, Object>> cobrar(
    @RequestParam(required = false) String documento,
    HttpSession session
  ) {
    CarritoPedido carrito = obtenerCarritoDeSesion(session);

    if (carrito.estaVacio()) {
      Map<String, Object> error = new HashMap<>();
      error.put("error", "El carrito está vacío");
      return ResponseEntity.badRequest().body(error);
    }

    Cliente cliente = servicioCliente.buscarPorDocumento(documento);
    Pedido pedido = servicioPedido.cobrarPedido(carrito, cliente);
    session.removeAttribute(ATRIBUTO_SESION_CARRITO);

    Map<String, Object> respuesta = new HashMap<>();
    respuesta.put("idPedido", pedido.getId());
    respuesta.put("precioFinal", pedido.getPrecioFinal());
    respuesta.put("clienteAnonimo", cliente == null);
    respuesta.put(
      "documentoNoEncontrado",
      documento != null && !documento.trim().isEmpty() && cliente == null
    );
    return ResponseEntity.ok(respuesta);
  }

  private CarritoPedido obtenerCarritoDeSesion(HttpSession session) {
    CarritoPedido carrito = (CarritoPedido) session.getAttribute(ATRIBUTO_SESION_CARRITO);
    if (carrito == null) {
      carrito = new CarritoPedido();
      session.setAttribute(ATRIBUTO_SESION_CARRITO, carrito);
    }
    return carrito;
  }

  public static class AgregarItemRequest {

    private Long idProductoFinal;
    private List<Long> ingredientesRetiradosIds;

    public Long getIdProductoFinal() {
      return idProductoFinal;
    }

    public void setIdProductoFinal(Long idProductoFinal) {
      this.idProductoFinal = idProductoFinal;
    }

    public List<Long> getIngredientesRetiradosIds() {
      return ingredientesRetiradosIds;
    }

    public void setIngredientesRetiradosIds(List<Long> ingredientesRetiradosIds) {
      this.ingredientesRetiradosIds = ingredientesRetiradosIds;
    }
  }
}
