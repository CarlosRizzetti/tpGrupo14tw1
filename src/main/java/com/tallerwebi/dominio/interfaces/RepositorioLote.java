package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Lote;
import java.time.OffsetDateTime;
import java.util.List;

public interface RepositorioLote {
  Lote buscarPorId(Long id);

  void guardar(Lote lote);

  void actualizar(Lote lote);

  List<Lote> listarTodos();

  List<Lote> listarConsumiblesDeProducto(Long idProducto);

  Lote buscarEnUsoDeProducto(Long idProducto);
  List<OffsetDateTime> obtenerFechasIngresoDesde(OffsetDateTime desde);

  List<Lote> obtenerLotesPorTimer(Long idTimer);

  List<Lote> listarVencidosNoMarcados(OffsetDateTime ahora);
}
