package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.entity.enums.TipoMovimientoStock;
import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import com.tallerwebi.dominio.interfaces.RepositorioProducto;
import com.tallerwebi.dominio.interfaces.RepositorioReglaVencimiento;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.dominio.interfaces.ServicioControlStock;
import com.tallerwebi.dominio.interfaces.ServicioProducto;
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
  private final RepositorioReglaVencimiento repositorioReglaVencimiento;
  private final ServicioControlStock servicioControlStock;

  @Autowired
  public ServicioProductoImpl(
    RepositorioProducto repositorioProducto,
    RepositorioTimer repositorioTimer,
    RepositorioCategoria repositorioCategoria,
    RepositorioReglaVencimiento repositorioReglaVencimiento,
    ServicioControlStock servicioControlStock
  ) {
    this.repositorioProducto = repositorioProducto;
    this.repositorioTimer = repositorioTimer;
    this.repositorioCategoria = repositorioCategoria;
    this.repositorioReglaVencimiento = repositorioReglaVencimiento;
    this.servicioControlStock = servicioControlStock;
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
    producto.setCantidad(datos.getCantidad() != null ? datos.getCantidad() : 0);
    repositorioProducto.guardar(producto);

    ReglaVencimiento regla = new ReglaVencimiento();
    regla.setUbicacion(datos.getUbicacion());
    regla.setDuracionMinutos(datos.getDuracionMinutos());
    regla.setTieneDescongelamiento(datos.getTieneDescongelamiento());
    regla.setDescongelamientoMinutos(datos.getDescongelamientoMinutos());
    regla.setProducto(producto);
    repositorioReglaVencimiento.guardar(regla);
  }

  @Override
  public List<Producto> listarProductos(Long categoriaId) {
    if (categoriaId != null) {
      return repositorioProducto.obtenerProductosPorCategoria(categoriaId);
    }
    return repositorioProducto.obtenerTodos();
  }

  @Override
  public void agregarStock(Long productoId, Integer cantidad) {
    if (cantidad == null || cantidad <= 0) {
      throw new IllegalArgumentException("La cantidad a agregar debe ser mayor a 0");
    }
    Producto producto = repositorioProducto.obtenerProductoPorId(productoId);
    if (producto == null) {
      throw new IllegalArgumentException("Producto no encontrado");
    }
    producto.setCantidad(producto.getCantidad() + cantidad);
    repositorioProducto.actualizar(producto);
    servicioControlStock.registrarMovimiento(producto, null, cantidad, TipoMovimientoStock.INGRESO);
  }

  @Override
  public void quitarStock(Long productoId, Integer cantidad) {
    if (cantidad == null || cantidad <= 0) {
      throw new IllegalArgumentException("La cantidad a quitar debe ser mayor a 0");
    }
    Producto producto = repositorioProducto.obtenerProductoPorId(productoId);
    if (producto == null) {
      throw new IllegalArgumentException("Producto no encontrado");
    }
    if (producto.getCantidad() < cantidad) {
      throw new IllegalArgumentException(
        "Stock insuficiente. Disponible: " + producto.getCantidad() + ", solicitado: " + cantidad
      );
    }
    producto.setCantidad(producto.getCantidad() - cantidad);
    repositorioProducto.actualizar(producto);
    servicioControlStock.registrarMovimiento(producto, null, cantidad, TipoMovimientoStock.EGRESO);
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
    return this.repositorioProducto.obtenerProductoConReglasYCategorias(id);
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

  @Override
  public void descontarStock(Producto producto, Integer cantidad) {
    if (producto.getCantidad() < cantidad) {
      throw new IllegalArgumentException(
        "Stock insuficiente. Disponible: " + producto.getCantidad() + ", solicitado: " + cantidad
      );
    }
    producto.setCantidad(producto.getCantidad() - cantidad);
    repositorioProducto.actualizar(producto);
  }

  private CategoriaDto mapearCategoriaConDisponibilidad(Categoria categoria, String groupId) {
    boolean estaPresente =
      this.repositorioTimer.existeTimerActivoEnCategoriaYGrupo(categoria.getId(), groupId);

    CategoriaDto categoriaDto = new CategoriaDto(categoria);
    categoriaDto.setEstaPresente(estaPresente);
    return categoriaDto;
  }

  private void validarProducto(ProductoDto datos) {
    validarNombre(datos);
    validarCategorias(datos);
    validarCantidad(datos);
  }

  private void validarNombre(ProductoDto datos) {
    if (datos.getNombre() == null || datos.getNombre().isBlank()) {
      throw new IllegalArgumentException("El nombre del producto es obligatorio");
    }
  }

  private void validarCategorias(ProductoDto datos) {
    if (datos.getCategoriasIds() == null || datos.getCategoriasIds().isEmpty()) {
      throw new IllegalArgumentException("Debe seleccionar al menos una categoría");
    }
  }

  private void validarCantidad(ProductoDto datos) {
    if (datos.getCantidad() != null && datos.getCantidad() < 0) {
      throw new IllegalArgumentException("La cantidad no puede ser negativa");
    }
  }
}
