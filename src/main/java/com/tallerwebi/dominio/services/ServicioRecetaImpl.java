package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Articulos;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.Receta;
import com.tallerwebi.dominio.entity.RecetaDetalle;
import com.tallerwebi.dominio.interfaces.RepositorioArticulo;
import com.tallerwebi.dominio.interfaces.RepositorioReceta;
import com.tallerwebi.dominio.interfaces.ServicioReceta;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioReceta")
@Transactional
public class ServicioRecetaImpl implements ServicioReceta {

  private final RepositorioReceta repositorioReceta;
  private final RepositorioArticulo repositorioArticulo;

  @Autowired
  public ServicioRecetaImpl(
    RepositorioReceta repositorioReceta,
    RepositorioArticulo repositorioArticulo
  ) {
    this.repositorioReceta = repositorioReceta;
    this.repositorioArticulo = repositorioArticulo;
  }

  @Override
  public void guardarReceta(Producto producto, List<Long> articulosIds, List<Double> cantidades) {
    Receta receta = repositorioReceta.buscarPorProducto(producto);
    if (receta == null) {
      receta = new Receta();
      receta.setProducto(producto);
    } else {
      if (receta.getIngredientes() != null) {
        receta.getIngredientes().clear(); // Limpiamos ingredientes viejos
      }
    }

    List<RecetaDetalle> nuevosIngredientes = new ArrayList<>();
    if (articulosIds != null && cantidades != null && articulosIds.size() == cantidades.size()) {
      for (int i = 0; i < articulosIds.size(); i++) {
        Articulos articulo = repositorioArticulo.buscarPorId(articulosIds.get(i));
        if (articulo != null && cantidades.get(i) > 0) {
          RecetaDetalle detalle = new RecetaDetalle();
          detalle.setArticulo(articulo);
          detalle.setCantidad(cantidades.get(i));
          detalle.setReceta(receta);
          nuevosIngredientes.add(detalle);
        }
      }
    }
    receta.setIngredientes(nuevosIngredientes);
    repositorioReceta.guardar(receta);
  }

  @Override
  public Receta buscarPorProducto(Producto producto) {
    return repositorioReceta.buscarPorProducto(producto);
  }

  @Override
  public List<Receta> obtenerTodas() {
    return repositorioReceta.obtenerTodas();
  }
}
