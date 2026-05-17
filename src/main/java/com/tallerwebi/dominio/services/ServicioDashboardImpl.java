package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.dominio.interfaces.ServicioDashboard;

import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;

import com.tallerwebi.presentacion.dto.TimerDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("ServicioDashboard")
@Transactional
public class ServicioDashboardImpl implements ServicioDashboard {

  public RepositorioTimer repositorioTimer;

  @Autowired
  public ServicioDashboardImpl(RepositorioTimer repositorioTimer) {
    this.repositorioTimer = repositorioTimer;
  }

  @Override
  public List<TimerDTO> obtenerTimersActivos(Long id) {
    List<Timer> timers = this.repositorioTimer.obtenerTimersSegunEstado(id, "activo");
    List<TimerDTO> timerDTOS = new ArrayList<>();

    for(Timer timer : timers) {
      Long timerId = timer.getId();
      String nombre = (timer.getProducto() != null) ? timer.getProducto().getNombre() : "Producto Desconocido";
      String groupId = timer.getGroupId();
      String fechaCreacionISO = (timer.getFechaCreacion() != null) ? timer.getFechaCreacion().toString() : "";
      String fechaVencimientoISO = (timer.getFechaVencimiento() != null) ? timer.getFechaVencimiento().toString() : "";
      String ubicacion = (timer.getReglaVencimiento() != null) ? timer.getReglaVencimiento().getUbicacion() : "General";

      TimerDTO timerDTO = new TimerDTO(
              timerId,
              nombre,
              groupId,
              fechaCreacionISO,
              fechaVencimientoISO,
              ubicacion
      );

      timerDTOS.add(timerDTO);
    }
    return timerDTOS;
  }

}
