package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.ProductoFinal;
import com.tallerwebi.dominio.interfaces.RepositorioProductoFinal;
import com.tallerwebi.dominio.interfaces.ServicioProductoFinal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioProductoFinal")
@Transactional
public class ServicioProductoFinalImpl implements ServicioProductoFinal {

  private final RepositorioProductoFinal repositorioProductoFinal;
  private final com.tallerwebi.dominio.interfaces.RepositorioCategoria repositorioCategoria;
  private final com.tallerwebi.dominio.interfaces.RepositorioProducto repositorioProducto;

  @Autowired
  public ServicioProductoFinalImpl(
    RepositorioProductoFinal repositorioProductoFinal,
    com.tallerwebi.dominio.interfaces.RepositorioCategoria repositorioCategoria,
    com.tallerwebi.dominio.interfaces.RepositorioProducto repositorioProducto
  ) {
    this.repositorioProductoFinal = repositorioProductoFinal;
    this.repositorioCategoria = repositorioCategoria;
    this.repositorioProducto = repositorioProducto;
  }

  @Override
  public List<ProductoFinal> listarTodos() {
    return repositorioProductoFinal.listarTodos();
  }

  @Override
  public ProductoFinal buscarPorId(Long id) {
    return repositorioProductoFinal.buscarPorId(id);
  }

  @Override
  public List<ProductoFinal> listarPorCategoria(Long idCategoria) {
    return repositorioProductoFinal.listarPorCategoria(idCategoria);
  }

  @Override
  public void guardarProductoFinal(
    ProductoFinal productoFinal,
    Long idCategoria,
    List<Long> idIngredientes,
    List<Integer> cantidades
  ) {
    com.tallerwebi.dominio.entity.Categoria categoria = repositorioCategoria.buscarPorId(
      idCategoria
    );
    if (categoria != null) {
      productoFinal.getCategorias().add(categoria);
    }

    if (
      idIngredientes != null && cantidades != null && idIngredientes.size() == cantidades.size()
    ) {
      for (int i = 0; i < idIngredientes.size(); i++) {
        com.tallerwebi.dominio.entity.Producto producto = repositorioProducto.obtenerProductoPorId(
          idIngredientes.get(i)
        );
        if (producto != null) {
          productoFinal.agregarIngrediente(producto, cantidades.get(i));
        }
      }
    }

    repositorioProductoFinal.guardar(productoFinal);
  }
}
