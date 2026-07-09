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

  @Autowired
  public ServicioProductoFinalImpl(RepositorioProductoFinal repositorioProductoFinal) {
    this.repositorioProductoFinal = repositorioProductoFinal;
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
}
