package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.presentacion.dto.ProductoDto;
import java.util.List;

public interface ServicioProducto {
  void crearProducto(ProductoDto datos);
  Producto obtenerProductoPorId(Long id);
  List<Producto> obtenerProductosPorCategoria(Long categoriaId);
  Timer generarVencimiento(
    Producto producto,
    Categoria categoria,
    Long reglaId,
    Integer offsetMinutos
  );
  Producto obtenerProductoConReglas(Long id);
}
