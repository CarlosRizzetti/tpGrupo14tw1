package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.ProductoDto;
import java.util.List;

public interface ServicioProducto {
  void crearProducto(ProductoDto datos);
  List<Producto> listarProductos(Long categoriaId);
  Producto obtenerProductoPorId(Long id);
  List<Producto> obtenerTodosLosProductos();
  List<Producto> obtenerProductosPorCategoria(Long categoriaId);
  Producto obtenerProductoConReglas(Long id);
  List<CategoriaDto> obtenerCategoriasDeUnProducto(Long idProducto);
  List<CategoriaDto> obtenerCategoriasDeUnProductoDisponiblesParaImportar(Long id, String groupId);
}
