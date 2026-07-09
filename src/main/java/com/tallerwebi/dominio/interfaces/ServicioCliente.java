package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Cliente;

public interface ServicioCliente {
  Cliente buscarPorDocumento(String documento);
}
