package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.presentacion.dto.CalculoVencimientoDto;
import com.tallerwebi.presentacion.dto.ProductoDto;
import java.util.List;

public interface ServicioProducto {
  void crearProducto(ProductoDto datos);
  Producto obtenerProductoPorId(Long id);
  List<Producto> obtenerProductosPorCategoria(Long categoriaId);
  CalculoVencimientoDto calcularVencimiento(Producto producto, Integer offsetMinutos);
}
