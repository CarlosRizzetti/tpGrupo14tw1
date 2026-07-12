package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Cliente;

public interface RepositorioCliente {
  Cliente buscarPorId(Long id);
  Cliente buscarPorDocumento(String documento);
  Cliente buscarPorEmail(String email);
  void guardar(Cliente cliente);
}
