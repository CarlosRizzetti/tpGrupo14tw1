package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Cliente;
import com.tallerwebi.dominio.interfaces.RepositorioCliente;
import com.tallerwebi.dominio.interfaces.ServicioCliente;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioCliente")
@Transactional
public class ServicioClienteImpl implements ServicioCliente {

  private final RepositorioCliente repositorioCliente;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Autowired
  public ServicioClienteImpl(RepositorioCliente repositorioCliente) {
    this.repositorioCliente = repositorioCliente;
  }

  @Override
  public Cliente buscarPorDocumento(String documento) {
    if (documento == null || documento.trim().isEmpty()) return null;
    return repositorioCliente.buscarPorDocumento(documento.trim());
  }

  @Override
  public Cliente buscarPorEmail(String email) {
    if (email == null || email.trim().isEmpty()) return null;
    return repositorioCliente.buscarPorEmail(email.trim().toLowerCase(Locale.ROOT));
  }

  @Override
  public void registrarCliente(Cliente cliente) throws Exception {
    validarFormatoDocumentoYTelefono(cliente.getDocumento(), cliente.getTelefono());
    validarEmailYFormatear(cliente);
    validarDocumento(cliente);
    encriptarPasswordSiExiste(cliente);
    repositorioCliente.guardar(cliente);
  }

  private void validarFormatoDocumentoYTelefono(String documento, String telefono)
    throws Exception {
    validarFormatoDocumento(documento);
    validarFormatoTelefono(telefono);
  }

  private void validarFormatoDocumento(String documento) throws Exception {
    if (documento == null || documento.trim().isEmpty()) {
      throw new Exception("El número de documento (DNI) y el teléfono son obligatorios.");
    }
    if (!documento.trim().matches("^[0-9]{8}$")) {
      throw new Exception("El DNI debe tener exactamente 8 dígitos numéricos.");
    }
  }

  private void validarFormatoTelefono(String telefono) throws Exception {
    if (telefono == null || telefono.trim().isEmpty()) {
      throw new Exception("El número de documento (DNI) y el teléfono son obligatorios.");
    }
    if (!telefono.trim().matches("^[0-9]{10}$")) {
      throw new Exception("El teléfono debe tener exactamente 10 dígitos numéricos.");
    }
  }

  private void validarEmailYFormatear(Cliente cliente) throws Exception {
    if (cliente.getEmail() != null) {
      cliente.setEmail(cliente.getEmail().trim().toLowerCase(Locale.ROOT));
      if (buscarPorEmail(cliente.getEmail()) != null) {
        throw new Exception("El correo electrónico ya se encuentra registrado.");
      }
    }
  }

  private void validarDocumento(Cliente cliente) throws Exception {
    if (cliente.getDocumento() != null && !cliente.getDocumento().trim().isEmpty()) {
      if (buscarPorDocumento(cliente.getDocumento()) != null) {
        throw new Exception("El número de documento (DNI) ya se encuentra registrado.");
      }
    }
  }

  private void encriptarPasswordSiExiste(Cliente cliente) {
    if (cliente.getPassword() != null && !cliente.getPassword().isEmpty()) {
      cliente.setPassword(passwordEncoder.encode(cliente.getPassword()));
    }
  }

  @Override
  public void guardar(Cliente cliente) {
    repositorioCliente.guardar(cliente);
  }

  @Override
  @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.NullAssignment" })
  public void actualizarDatosCliente(
    Cliente cliente,
    String documento,
    String telefono,
    String nombre
  ) throws Exception {
    validarFormatoDocumentoYTelefono(documento, telefono);
    actualizarDocumento(cliente, documento);
    actualizarTelefonoYNombre(cliente, telefono, nombre);
    repositorioCliente.guardar(cliente);
  }

  @SuppressWarnings("PMD.NullAssignment")
  private void actualizarDocumento(Cliente cliente, String documento) throws Exception {
    if (documento != null && !documento.trim().isEmpty()) {
      Cliente otro = buscarPorDocumento(documento.trim());
      if (otro != null && !otro.getId().equals(cliente.getId())) {
        throw new Exception(
          "El número de documento (DNI) ya se encuentra registrado por otra cuenta."
        );
      }
      cliente.setDocumento(documento.trim());
    } else {
      cliente.setDocumento(null);
    }
  }

  @SuppressWarnings("PMD.NullAssignment")
  private void actualizarTelefonoYNombre(Cliente cliente, String telefono, String nombre) {
    if (telefono != null && !telefono.trim().isEmpty()) {
      cliente.setTelefono(telefono.trim());
    } else {
      cliente.setTelefono(null);
    }
    if (nombre != null && !nombre.trim().isEmpty()) {
      cliente.setNombre(nombre.trim());
    }
  }
}
