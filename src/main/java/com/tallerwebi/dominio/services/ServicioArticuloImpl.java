package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Articulos;
import com.tallerwebi.dominio.interfaces.RepositorioArticulo;
import com.tallerwebi.dominio.interfaces.ServicioArticulo;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioArticuloImpl implements ServicioArticulo {

  private final RepositorioArticulo repositorioArticulo;

  @Autowired
  public ServicioArticuloImpl(RepositorioArticulo repositorioArticulo) {
    this.repositorioArticulo = repositorioArticulo;
  }

  @Override
  public List<Articulos> obtenerTodosLosArticulos() {
    return repositorioArticulo.obtenerTodos();
  }

  @Override
  public void registrarArticulo(Articulos articulo) {
    repositorioArticulo.guardar(articulo);
  }

  @Override
  public Articulos buscarPorId(Long id) {
    return repositorioArticulo.buscarPorId(id);
  }

  @Override
  public List<Articulos> buscarPorNombre(String nombre) {
    return repositorioArticulo.buscarPorNombre(nombre);
  }

  @Override
  public List<com.tallerwebi.presentacion.dto.StockArticuloDto> obtenerStockAgrupado() {
    return repositorioArticulo.obtenerStockAgrupadoPorNombre();
  }
}
