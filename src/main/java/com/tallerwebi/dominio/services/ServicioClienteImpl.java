package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Cliente;
import com.tallerwebi.dominio.interfaces.RepositorioCliente;
import com.tallerwebi.dominio.interfaces.ServicioCliente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioCliente")
@Transactional
public class ServicioClienteImpl implements ServicioCliente {

  private final RepositorioCliente repositorioCliente;

  @Autowired
  public ServicioClienteImpl(RepositorioCliente repositorioCliente) {
    this.repositorioCliente = repositorioCliente;
  }

  @Override
  public Cliente buscarPorDocumento(String documento) {
    if (documento == null || documento.trim().isEmpty()) return null;
    return repositorioCliente.buscarPorDocumento(documento.trim());
  }
}
