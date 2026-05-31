package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import com.tallerwebi.dominio.interfaces.RepositorioProducto;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.dominio.interfaces.ServicioProducto;
import com.tallerwebi.dominio.interfaces.ServicioReglaVencimiento;
import com.tallerwebi.dominio.utils.ValidacionHelper;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.ProductoDto;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioProducto")
@Transactional
public class ServicioProductoImpl implements ServicioProducto {

  private final RepositorioProducto repositorioProducto;
  private final RepositorioTimer repositorioTimer;
  private final RepositorioCategoria repositorioCategoria;
  private final ServicioReglaVencimiento servicioReglaVencimiento;

  @Autowired
  public ServicioProductoImpl(
    RepositorioProducto repositorioProducto,
    RepositorioTimer repositorioTimer,
    RepositorioCategoria repositorioCategoria,
    ServicioReglaVencimiento servicioReglaVencimiento
  ) {
    this.repositorioProducto = repositorioProducto;
    this.repositorioTimer = repositorioTimer;
    this.repositorioCategoria = repositorioCategoria;
    this.servicioReglaVencimiento = servicioReglaVencimiento;
  }

  @Override
  public void crearProducto(ProductoDto datos) {
    validarProducto(datos);

    Set<Categoria> categorias = repositorioCategoria.obtenerCategoriasPorIds(
      datos.getCategoriasIds()
    );

    Producto producto = new Producto();
    producto.setNombre(datos.getNombre());
    producto.setEstaActivo(true);
    producto.setCategorias(categorias);
    repositorioProducto.guardar(producto);

    ReglaVencimiento regla = new ReglaVencimiento();
    regla.setUbicacion(datos.getUbicacion());
    regla.setDuracionMinutos(datos.getDuracionMinutos());
    regla.setTieneDescongelamiento(datos.getTieneDescongelamiento());
    regla.setDescongelamientoMinutos(datos.getDescongelamientoMinutos());
    regla.setProducto(producto);
    servicioReglaVencimiento.guardarReglaVencimiento(regla);
  }

  @Override
  public List<Producto> obtenerProductosPorCategoria(Long categoriaId) {
    return repositorioProducto.obtenerProductosPorCategoria(categoriaId);
  }

  @Override
  public Producto obtenerProductoPorId(Long id) {
    return repositorioProducto.obtenerProductoPorId(id);
  }

  @Override
  public Producto obtenerProductoConReglas(Long id) {
    Producto producto = this.repositorioProducto.obtenerProductoConReglasYCategorias(id);
    if (producto != null) return producto;
    return producto;
  }

  @Override
  public List<CategoriaDto> obtenerCategoriasDeUnProducto(Long idProducto) {
    ValidacionHelper.validarId(idProducto);

    Producto producto = this.repositorioProducto.obtenerProductoConReglasYCategorias(idProducto);
    ValidacionHelper.queNoSeaNull(producto, "producto");

    Set<Categoria> categorias = producto.getCategorias();
    ValidacionHelper.queElSetNoSeaNull(categorias, "categorias del producto");

    return categorias.stream().map(CategoriaDto::new).collect(Collectors.toList());
  }

  @Override
  public List<CategoriaDto> obtenerCategoriasDeUnProductoDisponiblesParaImportar(
    Long idProducto,
    String groupId
  ) {
    ValidacionHelper.validarId(idProducto);
    ValidacionHelper.validarCampoSeguro(groupId, "groupId");

    Producto producto = this.repositorioProducto.obtenerProductoConReglasYCategorias(idProducto);
    ValidacionHelper.queNoSeaNull(producto, "producto");

    Set<Categoria> categorias = producto.getCategorias();
    ValidacionHelper.queElSetNoSeaNull(categorias, "categorias del producto");

    return categorias
      .stream()
      .map(categoria -> mapearCategoriaConDisponibilidad(categoria, groupId))
      .collect(Collectors.toList());
  }

  private CategoriaDto mapearCategoriaConDisponibilidad(Categoria categoria, String groupId) {
    boolean estaPresente =
      this.repositorioTimer.existeTimerActivoEnCategoriaYGrupo(categoria.getId(), groupId);

    CategoriaDto categoriaDto = new CategoriaDto(categoria);
    categoriaDto.setEstaPresente(estaPresente);
    return categoriaDto;
  }

  private void validarProducto(ProductoDto datos) {
    if (datos.getNombre() == null || datos.getNombre().isBlank()) {
      throw new IllegalArgumentException("El nombre del producto es obligatorio");
    }
    if (datos.getCategoriasIds() == null || datos.getCategoriasIds().isEmpty()) {
      throw new IllegalArgumentException("Debe seleccionar al menos una categoría");
    }
  }
}
