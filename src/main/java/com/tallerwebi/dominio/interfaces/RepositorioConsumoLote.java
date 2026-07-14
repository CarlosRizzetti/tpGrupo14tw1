package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.ConsumoLote;
import com.tallerwebi.dominio.entity.Pedido;
import java.util.List;

public interface RepositorioConsumoLote {
  void guardar(ConsumoLote consumo);
  List<Pedido> obtenerPedidosPorLote(Long idLote);
  List<ConsumoLote> listarPorTimer(Long idTimer);
}
