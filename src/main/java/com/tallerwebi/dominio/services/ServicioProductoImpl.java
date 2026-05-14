package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.interfaces.RepositorioProducto;
import com.tallerwebi.dominio.interfaces.ServicioProducto;
import com.tallerwebi.presentacion.dto.ProductoDto;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioProducto")
@Transactional
public class ServicioProductoImpl implements ServicioProducto {

  private final RepositorioProducto repositorioProducto;

  @Autowired
  public ServicioProductoImpl(RepositorioProducto repositorioProducto) {
    this.repositorioProducto = repositorioProducto;
  }

  @Override
  public void crearProducto(ProductoDto datos) {
    validar(datos);

    // Traer categorías desde la BD
    List<Categoria> categorias = repositorioProducto.obtenerCategoriasPorIds(
      datos.getCategoriasIds()
    );

    // Armar el producto
    Producto producto = new Producto();
    producto.setNombre(datos.getNombre());
    producto.setEstaActivo(true);
    producto.setCategorias(categorias);

    // Armar la regla de vencimiento
    ReglaVencimiento regla = new ReglaVencimiento();
    regla.setUbicacion(datos.getUbicacion());
    regla.setDuracionMinutos(datos.getDuracionMinutos());
    regla.setTieneDescongelamiento(datos.getTieneDescongelamiento());
    regla.setDescongelamientoMinutos(datos.getDescongelamientoMinutos());
    regla.setProducto(producto);

    producto.setReglaVencimiento(regla);

    // Se guarda el producto (la regla se persiste por CascadeType.ALL)
    repositorioProducto.guardar(producto);
  }

  @Override
  public List<Producto> obtenerProductosPorCategoria(Long categoriaId) {
    return repositorioProducto.obtenerProductosPorCategoria(categoriaId);
  }

  private void validar(ProductoDto datos) {
    validarProducto(datos);
    validarReglaVencimiento(datos);
  }

  private void validarProducto(ProductoDto datos) {
    if (datos.getNombre() == null || datos.getNombre().isBlank()) {
      throw new IllegalArgumentException("El nombre del producto es obligatorio");
    }
    if (datos.getCategoriasIds() == null || datos.getCategoriasIds().isEmpty()) {
      throw new IllegalArgumentException("Debe seleccionar al menos una categoría");
    }
  }

  private void validarReglaVencimiento(ProductoDto datos) {
    if (datos.getUbicacion() == null || datos.getUbicacion().isBlank()) {
      throw new IllegalArgumentException("La ubicación es obligatoria");
    }
    if (datos.getDuracionMinutos() == null || datos.getDuracionMinutos() <= 0) {
      throw new IllegalArgumentException("La duración debe ser mayor a 0");
    }
    validarDescongelamiento(datos);
  }

  private void validarDescongelamiento(ProductoDto datos) {
    if (Boolean.TRUE.equals(datos.getTieneDescongelamiento())) {
      if (datos.getDescongelamientoMinutos() == null || datos.getDescongelamientoMinutos() <= 0) {
        throw new IllegalArgumentException("Los minutos de descongelamiento son obligatorios");
      }
    }
  }
}
