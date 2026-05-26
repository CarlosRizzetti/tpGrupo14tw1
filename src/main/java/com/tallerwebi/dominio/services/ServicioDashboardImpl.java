package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.dominio.interfaces.ServicioDashboard;
import com.tallerwebi.presentacion.dto.TimerDTO;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("ServicioDashboard")
@Transactional
public class ServicioDashboardImpl implements ServicioDashboard {

  public RepositorioTimer repositorioTimer;
  public RepositorioCategoria repositorioCategoria;

  @Autowired
  public ServicioDashboardImpl(
    RepositorioTimer repositorioTimer,
    RepositorioCategoria repositorioCategoria
  ) {
    this.repositorioTimer = repositorioTimer;
    this.repositorioCategoria = repositorioCategoria;
  }

  @Override
  public List<TimerDTO> obtenerTimersActivos(Long id) {
    List<Timer> timers = this.repositorioTimer.obtenerTimersSegunEstado(id, "activo");
    List<TimerDTO> timerDTOS = new ArrayList<>();

    for (Timer timer : timers) {
      Long timerId = timer.getId();
      String nombre = (timer.getProducto() != null)
        ? timer.getProducto().getNombre()
        : "Producto Desconocido";
      String groupId = timer.getGroupId();
      String fechaCreacionISO = (timer.getFechaCreacion() != null)
        ? timer.getFechaCreacion().toString()
        : "";
      String fechaVencimientoISO = (timer.getFechaVencimiento() != null)
        ? timer.getFechaVencimiento().toString()
        : "";
      String ubicacion = (timer.getReglaVencimiento() != null)
        ? timer.getReglaVencimiento().getUbicacion()
        : "General";

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

  @Override
  public void eliminarTimer(Long timerId) {
    Timer timer = repositorioTimer.buscarPorId(timerId);
    if (timer != null) {
      if (timer.getFechaVencimiento().isBefore(OffsetDateTime.now())) {
        timer.setEstado("vencido");
      } else {
        timer.setEstado("eliminado");
      }
      timer.setEstaActivo(false);
      repositorioTimer.guardar(timer);
    }
  }

  @Override
  public void importarTimer(Long timerId, Long categoriaId) {
    Timer original = repositorioTimer.buscarPorId(timerId);
    if (original == null) {
      throw new IllegalArgumentException("El timer no existe");
    }
    Categoria categoriaDestino = repositorioCategoria.buscarPorId(categoriaId);

    if (categoriaDestino == null) {
      throw new IllegalArgumentException("La categoría no existe");
    }

    // Crear clon reutilizando el MISMO groupId
    Timer clon = new Timer(
      original.getFechaCreacion(),
      original.getFechaVencimiento(),
      original.getGroupId(),
      original.getProducto(),
      categoriaDestino,
      original.getReglaVencimiento()
    );

    repositorioTimer.guardar(clon);
  }
}
