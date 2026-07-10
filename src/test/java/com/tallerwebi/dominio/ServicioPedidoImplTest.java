package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Cliente;
import com.tallerwebi.dominio.entity.Pedido;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ProductoFinal;
import com.tallerwebi.dominio.entity.enums.EstadoComanda;
import com.tallerwebi.dominio.entity.enums.EstadoPedido;
import com.tallerwebi.dominio.interfaces.RepositorioPedido;
import com.tallerwebi.dominio.services.ServicioPedidoImpl;
import com.tallerwebi.dominio.utils.CarritoPedido;
import com.tallerwebi.dominio.utils.ItemCarrito;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioPedidoImplTest {

  private RepositorioPedido repositorioPedido;
  private ServicioPedidoImpl servicioPedido;

  @BeforeEach
  public void setUp() {
    repositorioPedido = mock(RepositorioPedido.class);
    servicioPedido = new ServicioPedidoImpl(repositorioPedido);
  }

  @Test
  public void queSePuedaCobrarUnPedido() {
    CarritoPedido carrito = new CarritoPedido();

    ProductoFinal pf = new ProductoFinal();
    pf.setId(1L);
    pf.setPrecio(BigDecimal.valueOf(500.0));

    ItemCarrito item = new ItemCarrito(pf);

    carrito.agregarItem(item);

    Cliente cliente = new Cliente();
    cliente.setId(100L);

    Pedido pedido = servicioPedido.cobrarPedido(carrito, cliente);

    assertThat(pedido, notNullValue());
    assertThat(pedido.getCliente(), equalTo(cliente));
    assertThat(pedido.getEstado(), equalTo(EstadoPedido.EN_COCINA));
    assertThat(pedido.getPrecioFinal(), equalTo(BigDecimal.valueOf(500.0)));
    assertThat(pedido.getComanda(), notNullValue());
    assertThat(pedido.getComanda().getEstado(), equalTo(EstadoComanda.PENDIENTE));
    assertThat(carrito.estaVacio(), equalTo(true));

    verify(repositorioPedido, times(1)).guardar(pedido);
  }
}
