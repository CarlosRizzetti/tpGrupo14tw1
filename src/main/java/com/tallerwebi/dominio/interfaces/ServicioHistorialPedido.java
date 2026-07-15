package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.presentacion.dto.HistorialPedidoDTO;
import java.time.OffsetDateTime;
import java.util.List;

public interface ServicioHistorialPedido {
  List<HistorialPedidoDTO> buscarHistorial(
    OffsetDateTime desde,
    OffsetDateTime hasta,
    Long numeroDeLote,
    String clienteNombre
  );

  List<HistorialPedidoDTO> buscarPorCliente(Long idCliente);
}
