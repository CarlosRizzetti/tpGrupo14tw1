package com.tallerwebi.repositorio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tallerwebi.dominio.entity.Cliente;
import com.tallerwebi.dominio.entity.Pedido;
import com.tallerwebi.dominio.entity.enums.EstadoPedido;
import com.tallerwebi.dominio.interfaces.RepositorioCliente;
import com.tallerwebi.dominio.interfaces.RepositorioPedido;
import com.tallerwebi.repositorio.config.HibernateInfraestructuraTestConfig;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
@ActiveProfiles("test")
@Transactional
public class RepositorioPedidoTest {

  private static final Clock CLOCK = Clock.fixed(
    Instant.now().truncatedTo(ChronoUnit.SECONDS),
    ZoneOffset.ofHours(-3)
  );

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioPedido repositorioPedido;
  private RepositorioCliente repositorioCliente;

  @BeforeEach
  public void init() {
    repositorioPedido = new RepositorioPedidoImpl(sessionFactory);
    repositorioCliente = new RepositorioClienteImpl(sessionFactory);
  }

  // ---------- helpers ----------

  private Cliente crearYGuardarCliente(String nombre, String documento, String email) {
    Cliente cliente = new Cliente();
    cliente.setNombre(nombre);
    cliente.setDocumento(documento);
    cliente.setTelefono("1122334455");
    cliente.setEmail(email);
    repositorioCliente.guardar(cliente);
    return cliente;
  }

  private Pedido crearPedido(Cliente cliente, OffsetDateTime horaCobro, EstadoPedido estado) {
    Pedido pedido = new Pedido();
    pedido.setCliente(cliente);
    pedido.setHoraCobro(horaCobro);
    pedido.setEstado(estado);
    pedido.setPrecioFinal(new BigDecimal("100.00"));
    return pedido;
  }

  private Pedido crearYGuardarPedido(
    Cliente cliente,
    OffsetDateTime horaCobro,
    EstadoPedido estado
  ) {
    Pedido pedido = crearPedido(cliente, horaCobro, estado);
    repositorioPedido.guardar(pedido);
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();
    return pedido;
  }

  // ========================================================
  // guardar
  // ========================================================

  @Test
  @DisplayName("HP-01 | guardar | Persiste un pedido nuevo y le asigna un id")
  public void guardarPedidoNuevoDeberiaPersistirloYAsignarleId() {
    Cliente cliente = crearYGuardarCliente("Juan Pérez", "40123456", "juan@mail.com");
    Pedido pedido = crearPedido(cliente, OffsetDateTime.now(CLOCK), EstadoPedido.EN_COCINA);

    repositorioPedido.guardar(pedido);
    sessionFactory.getCurrentSession().flush();

    assertNotNull(pedido.getId());
  }

  @Test
  @DisplayName(
    "HP-02 | guardar | Persiste correctamente el cliente, el estado, el precio y la hora de cobro"
  )
  public void guardarPedidoDeberiaPersistirTodosSusDatos() {
    Cliente cliente = crearYGuardarCliente("Juan Pérez", "40123456", "juan@mail.com");
    OffsetDateTime horaCobro = OffsetDateTime.now(CLOCK);
    Pedido pedido = crearYGuardarPedido(cliente, horaCobro, EstadoPedido.ENTREGADO);

    Pedido resultado = sessionFactory.getCurrentSession().get(Pedido.class, pedido.getId());

    assertEquals(cliente.getId(), resultado.getCliente().getId());
    assertEquals(EstadoPedido.ENTREGADO, resultado.getEstado());
    assertEquals(0, new BigDecimal("100.00").compareTo(resultado.getPrecioFinal()));
    assertEquals(horaCobro.toInstant(), resultado.getHoraCobro().toInstant());
  }

  // ========================================================
  // buscarPorId
  // ========================================================

  @Test
  @DisplayName("HP-03 | buscarPorId | Devuelve el pedido correcto cuando existe")
  public void buscarPorIdDeberiaDevolverElPedidoCorrecto() {
    Cliente cliente = crearYGuardarCliente("Juan Pérez", "40123456", "juan@mail.com");
    Pedido pedido = crearYGuardarPedido(cliente, OffsetDateTime.now(CLOCK), EstadoPedido.EN_COCINA);

    Pedido resultado = repositorioPedido.buscarPorId(pedido.getId());

    assertNotNull(resultado);
    assertEquals(pedido.getId(), resultado.getId());
  }

  @Test
  @DisplayName("NEG-01 | buscarPorId | Devuelve null cuando no existe un pedido con ese id")
  public void buscarPorIdDeberiaDevolverNullSiNoExiste() {
    Pedido resultado = repositorioPedido.buscarPorId(999999L);

    assertNull(resultado);
  }

  // ========================================================
  // listarTodos
  // ========================================================

  @Test
  @DisplayName(
    "HP-04 | listarTodos | Devuelve todos los pedidos ordenados por hora de cobro descendente"
  )
  public void listarTodosDeberiaDevolverLosPedidosOrdenadosPorHoraCobroDesc() {
    Cliente cliente = crearYGuardarCliente("Juan Pérez", "40123456", "juan@mail.com");
    Pedido masViejo = crearYGuardarPedido(
      cliente,
      OffsetDateTime.now(CLOCK).minusHours(2),
      EstadoPedido.ENTREGADO
    );
    Pedido masNuevo = crearYGuardarPedido(
      cliente,
      OffsetDateTime.now(CLOCK),
      EstadoPedido.EN_COCINA
    );

    List<Pedido> resultado = repositorioPedido.listarTodos();

    assertEquals(2, resultado.size());
    assertEquals(masNuevo.getId(), resultado.get(0).getId());
    assertEquals(masViejo.getId(), resultado.get(1).getId());
  }

  @Test
  @DisplayName("EDGE-01 | listarTodos | Devuelve una lista vacía si no hay pedidos cargados")
  public void listarTodosDeberiaDevolverListaVaciaSiNoHayPedidos() {
    List<Pedido> resultado = repositorioPedido.listarTodos();

    assertTrue(resultado.isEmpty());
  }

  // ========================================================
  // listarPorCliente
  // ========================================================

  @Test
  @DisplayName("HP-05 | listarPorCliente | Devuelve solo los pedidos del cliente indicado")
  public void listarPorClienteDeberiaDevolverSoloLosPedidosDeEseCliente() {
    Cliente clienteUno = crearYGuardarCliente("Juan Pérez", "40123456", "juan@mail.com");
    Cliente clienteDos = crearYGuardarCliente("Ana Gómez", "40123457", "ana@mail.com");
    Pedido pedidoDeJuan = crearYGuardarPedido(
      clienteUno,
      OffsetDateTime.now(CLOCK),
      EstadoPedido.ENTREGADO
    );
    crearYGuardarPedido(clienteDos, OffsetDateTime.now(CLOCK), EstadoPedido.EN_COCINA);

    List<Pedido> resultado = repositorioPedido.listarPorCliente(clienteUno.getId());

    assertEquals(1, resultado.size());
    assertEquals(pedidoDeJuan.getId(), resultado.get(0).getId());
  }

  @Test
  @DisplayName(
    "HP-06 | listarPorCliente | Devuelve los pedidos del cliente ordenados por hora de cobro descendente"
  )
  public void listarPorClienteDeberiaDevolverlosOrdenadosPorHoraCobroDesc() {
    Cliente cliente = crearYGuardarCliente("Juan Pérez", "40123456", "juan@mail.com");
    Pedido masViejo = crearYGuardarPedido(
      cliente,
      OffsetDateTime.now(CLOCK).minusHours(2),
      EstadoPedido.ENTREGADO
    );
    Pedido masNuevo = crearYGuardarPedido(
      cliente,
      OffsetDateTime.now(CLOCK),
      EstadoPedido.EN_COCINA
    );

    List<Pedido> resultado = repositorioPedido.listarPorCliente(cliente.getId());

    assertEquals(2, resultado.size());
    assertEquals(masNuevo.getId(), resultado.get(0).getId());
    assertEquals(masViejo.getId(), resultado.get(1).getId());
  }

  @Test
  @DisplayName(
    "EDGE-02 | listarPorCliente | Devuelve una lista vacía si el cliente no tiene pedidos"
  )
  public void listarPorClienteDeberiaDevolverListaVaciaSiElClienteNoTienePedidos() {
    Cliente cliente = crearYGuardarCliente("Juan Pérez", "40123456", "juan@mail.com");

    List<Pedido> resultado = repositorioPedido.listarPorCliente(cliente.getId());

    assertTrue(resultado.isEmpty());
  }
}
