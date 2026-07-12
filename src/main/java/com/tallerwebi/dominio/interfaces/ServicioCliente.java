package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Cliente;

public interface ServicioCliente {
  Cliente buscarPorDocumento(String documento);
  Cliente buscarPorEmail(String email);
  void registrarCliente(Cliente cliente) throws Exception;
  void guardar(Cliente cliente);
  void actualizarDatosCliente(Cliente cliente, String documento, String telefono, String nombre)
    throws Exception;
}
