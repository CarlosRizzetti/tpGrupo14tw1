package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.ConsumoLote;
import com.tallerwebi.dominio.entity.Lote;
import com.tallerwebi.dominio.entity.Pedido;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.presentacion.dto.LoteConsumidoDTO;
import com.tallerwebi.presentacion.dto.NotificacionVencimientoDto;
import com.tallerwebi.presentacion.dto.StockProductoDTO;
import java.util.List;

public interface ServicioLote {
  Lote registrarLote(Lote lote);

  List<Lote> obtenerTodosLosLotes();

  List<StockProductoDTO> obtenerStockAgrupado();

  List<NotificacionVencimientoDto> obtenerNotificacionesVencimiento();

  List<ConsumoLote> consumirCantidad(Producto producto, Integer cantidadNecesaria, Timer timer);

  Integer stockDisponibleDe(Producto producto);

  Lote buscarPorId(Long id);

  //Lote -> ConsumoLote -> Timer -> ConsumoTimer -> Pedido.
  List<Pedido> obtenerPedidosQueUsaronLote(Long idLote);

  List<LoteConsumidoDTO> obtenerLotesConsumidosPorTimer(Long idTimer);
}
