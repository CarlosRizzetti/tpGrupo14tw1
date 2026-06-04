package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.ControlStock;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.enums.TipoMovimientoStock;
import com.tallerwebi.dominio.interfaces.RepositorioControlStock;
import com.tallerwebi.dominio.interfaces.ServicioControlStock;
import java.time.Clock;
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioControlStock")
@Transactional
public class ServicioControlStockImpl implements ServicioControlStock {

  private final RepositorioControlStock repositorioControlStock;
  private final Clock clock;

  @Autowired
  public ServicioControlStockImpl(RepositorioControlStock repositorioControlStock, Clock clock) {
    this.repositorioControlStock = repositorioControlStock;
    this.clock = clock;
  }

  @Override
  public void registrarMovimiento(
    Producto producto,
    Timer timer,
    Integer cantidad,
    TipoMovimientoStock tipo
  ) {
    ControlStock movimiento = ControlStock
      .builder()
      .producto(producto)
      .timer(timer)
      .cantidad(cantidad)
      .tipo(tipo)
      .fecha(OffsetDateTime.now(clock))
      .build();
    repositorioControlStock.guardar(movimiento);
  }
}
