package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Comanda;
import java.util.List;

public interface RepositorioComanda {
  Comanda buscarPorId(Long id);

  List<Comanda> listarPendientes();

  void actualizar(Comanda comanda);
}
