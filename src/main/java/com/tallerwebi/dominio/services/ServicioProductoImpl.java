package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import com.tallerwebi.dominio.interfaces.RepositorioProducto;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.dominio.interfaces.ServicioProducto;
import com.tallerwebi.dominio.utils.ValidacionHelper;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.ProductoDto;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.HashSet;
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
  private RepositorioTimer repositorioTimer;
  private RepositorioCategoria repositorioCategoria;
  private Clock clock;

  @Autowired
  public ServicioProductoImpl(
    Clock clock,
    RepositorioProducto repositorioProducto,
    RepositorioTimer repositorioTimer,
    RepositorioCategoria repositorioCategoria
  ) {
    this.clock = clock;
    this.repositorioProducto = repositorioProducto;
    this.repositorioTimer = repositorioTimer;
    this.repositorioCategoria = repositorioCategoria;
  }

  @Override
  public void crearProducto(ProductoDto datos) {
    validar(datos);

    // Traer categorías desde la BD
    Set<Categoria> categorias = repositorioCategoria.obtenerCategoriasPorIds(
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

    Set<ReglaVencimiento> listaReglas = new HashSet<>();
    listaReglas.add(regla);
    producto.setReglas(listaReglas);

    repositorioProducto.guardar(producto);
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
  public Timer generarVencimiento(
    Producto producto,
    Categoria categoria,
    Long reglaId,
    Integer offsetMinutos
  ) {
    ReglaVencimiento regla = producto
      .getReglas()
      .stream()
      .filter(r -> r.getId().equals(reglaId))
      .findFirst()
      .orElse(null);
    if (regla == null) {
      throw new IllegalArgumentException("El producto no tiene regla de vencimiento");
    }

    OffsetDateTime elaboracion = OffsetDateTime.now(clock).minusMinutes(offsetMinutos);
    OffsetDateTime vencimiento = elaboracion.plusMinutes(regla.getDuracionMinutos());
    OffsetDateTime descongelamiento = (Boolean.TRUE.equals(regla.getTieneDescongelamiento()) &&
        regla.getDescongelamientoMinutos() != null)
      ? elaboracion.plusMinutes(regla.getDescongelamientoMinutos())
      : null;

    Timer timer = new Timer(elaboracion, vencimiento, descongelamiento, producto, categoria, regla);
    this.repositorioTimer.guardar(timer);
    return timer;
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
