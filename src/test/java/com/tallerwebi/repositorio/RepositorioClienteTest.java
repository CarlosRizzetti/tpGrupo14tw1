package com.tallerwebi.repositorio;

import static org.junit.jupiter.api.Assertions.*;

import com.tallerwebi.dominio.entity.Cliente;
import com.tallerwebi.dominio.interfaces.RepositorioCliente;
import com.tallerwebi.repositorio.config.HibernateInfraestructuraTestConfig;
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
public class RepositorioClienteTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioCliente repositorioCliente;

  @BeforeEach
  public void init() {
    repositorioCliente = new RepositorioClienteImpl(sessionFactory);
  }

  // ---------- helpers ----------

  private Cliente crearCliente(String nombre, String documento, String telefono, String email) {
    Cliente cliente = new Cliente();
    cliente.setNombre(nombre);
    cliente.setDocumento(documento);
    cliente.setTelefono(telefono);
    cliente.setEmail(email);
    return cliente;
  }

  private Cliente crearYGuardarCliente(
    String nombre,
    String documento,
    String telefono,
    String email
  ) {
    Cliente cliente = crearCliente(nombre, documento, telefono, email);
    repositorioCliente.guardar(cliente);
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();
    return cliente;
  }

  // ========================================================
  // guardar
  // ========================================================

  @Test
  @DisplayName("HP-01 | guardar | Persiste un cliente nuevo y le asigna un id")
  public void guardarClienteNuevoDeberiaPersistirloYAsignarleId() {
    Cliente cliente = crearCliente("Juan Pérez", "40123456", "1122334455", "juan@mail.com");

    repositorioCliente.guardar(cliente);
    sessionFactory.getCurrentSession().flush();

    assertNotNull(cliente.getId());
  }

  @Test
  @DisplayName("HP-02 | guardar | Persiste correctamente todos los datos del cliente")
  public void guardarClienteDeberiaPersistirTodosSusDatos() {
    Cliente cliente = crearYGuardarCliente("Juan Pérez", "40123456", "1122334455", "juan@mail.com");

    Cliente resultado = sessionFactory.getCurrentSession().get(Cliente.class, cliente.getId());

    assertEquals("Juan Pérez", resultado.getNombre());
    assertEquals("40123456", resultado.getDocumento());
    assertEquals("1122334455", resultado.getTelefono());
    assertEquals("juan@mail.com", resultado.getEmail());
  }

  @Test
  @DisplayName(
    "HP-03 | guardar | Actualiza los datos de un cliente ya existente en vez de duplicarlo"
  )
  public void guardarClienteExistenteDeberiaActualizarloEnVezDeDuplicarlo() {
    Cliente cliente = crearYGuardarCliente("Juan Pérez", "40123456", "1122334455", "juan@mail.com");
    Long idOriginal = cliente.getId();

    cliente.setTelefono("1199998888");
    repositorioCliente.guardar(cliente);
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    Cliente resultado = sessionFactory.getCurrentSession().get(Cliente.class, idOriginal);
    assertEquals(idOriginal, resultado.getId());
    assertEquals("1199998888", resultado.getTelefono());
  }

  // ========================================================
  // buscarPorId
  // ========================================================

  @Test
  @DisplayName("HP-04 | buscarPorId | Devuelve el cliente correcto cuando existe")
  public void buscarPorIdDeberiaDevolverElClienteCorrecto() {
    Cliente cliente = crearYGuardarCliente("Juan Pérez", "40123456", "1122334455", "juan@mail.com");

    Cliente resultado = repositorioCliente.buscarPorId(cliente.getId());

    assertNotNull(resultado);
    assertEquals(cliente.getId(), resultado.getId());
    assertEquals("Juan Pérez", resultado.getNombre());
  }

  @Test
  @DisplayName("NEG-01 | buscarPorId | Devuelve null cuando no existe un cliente con ese id")
  public void buscarPorIdDeberiaDevolverNullSiNoExiste() {
    Cliente resultado = repositorioCliente.buscarPorId(999999L);

    assertNull(resultado);
  }

  // ========================================================
  // buscarPorDocumento
  // ========================================================

  @Test
  @DisplayName(
    "HP-05 | buscarPorDocumento | Devuelve el cliente correcto cuando el documento existe"
  )
  public void buscarPorDocumentoDeberiaDevolverElClienteCorrecto() {
    crearYGuardarCliente("Juan Pérez", "40123456", "1122334455", "juan@mail.com");

    Cliente resultado = repositorioCliente.buscarPorDocumento("40123456");

    assertNotNull(resultado);
    assertEquals("Juan Pérez", resultado.getNombre());
  }

  @Test
  @DisplayName(
    "NEG-02 | buscarPorDocumento | Devuelve null cuando no existe un cliente con ese documento"
  )
  public void buscarPorDocumentoDeberiaDevolverNullSiNoExiste() {
    Cliente resultado = repositorioCliente.buscarPorDocumento("00000000");

    assertNull(resultado);
  }

  @Test
  @DisplayName(
    "EDGE-01 | buscarPorDocumento | No confunde clientes distintos con documentos parecidos"
  )
  public void buscarPorDocumentoNoDeberiaConfundirClientesConDocumentosParecidos() {
    Cliente clienteUno = crearYGuardarCliente(
      "Juan Pérez",
      "40123456",
      "1122334455",
      "juan@mail.com"
    );
    crearYGuardarCliente("Ana Gómez", "40123457", "1122334456", "ana@mail.com");

    Cliente resultado = repositorioCliente.buscarPorDocumento("40123456");

    assertEquals(clienteUno.getId(), resultado.getId());
  }

  // ========================================================
  // buscarPorEmail
  // ========================================================

  @Test
  @DisplayName("HP-06 | buscarPorEmail | Devuelve el cliente correcto cuando el email existe")
  public void buscarPorEmailDeberiaDevolverElClienteCorrecto() {
    crearYGuardarCliente("Juan Pérez", "40123456", "1122334455", "juan@mail.com");

    Cliente resultado = repositorioCliente.buscarPorEmail("juan@mail.com");

    assertNotNull(resultado);
    assertEquals("40123456", resultado.getDocumento());
  }

  @Test
  @DisplayName("NEG-03 | buscarPorEmail | Devuelve null cuando no existe un cliente con ese email")
  public void buscarPorEmailDeberiaDevolverNullSiNoExiste() {
    Cliente resultado = repositorioCliente.buscarPorEmail("no-existe@mail.com");

    assertNull(resultado);
  }

  @Test
  @DisplayName(
    "EDGE-02 | buscarPorEmail | Distingue clientes distintos con el mismo id devuelto correctamente"
  )
  public void buscarPorEmailDeberiaDistinguirClientesDistintos() {
    crearYGuardarCliente("Juan Pérez", "40123456", "1122334455", "juan@mail.com");
    Cliente clienteDos = crearYGuardarCliente(
      "Ana Gómez",
      "40123457",
      "1122334456",
      "ana@mail.com"
    );

    Cliente resultado = repositorioCliente.buscarPorEmail("ana@mail.com");

    assertNotEquals("juan@mail.com", resultado.getEmail());
    assertEquals(clienteDos.getId(), resultado.getId());
  }
}
