package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.enums.EstadoTimer;
import com.tallerwebi.dominio.excepcion.ValidacionException;
import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.dominio.interfaces.ServicioReglaVencimiento;
import com.tallerwebi.dominio.interfaces.ServicioTimer;
import com.tallerwebi.dominio.utils.ValidacionHelper;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.TimerDTO;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioTimer")
@Transactional
public class ServicioTimerImpl implements ServicioTimer {

  private final String TIMER = "timer";
  private final ServicioReglaVencimiento servicioReglaVencimiento;
  private RepositorioTimer repositorioTimer;
  private RepositorioCategoria repositorioCategoria;

  @Autowired
  public ServicioTimerImpl(
    RepositorioTimer repositorioTimer,
    RepositorioCategoria repositorioCategoria,
    ServicioReglaVencimiento servicioReglaVencimiento
  ) {
    this.repositorioTimer = repositorioTimer;
    this.repositorioCategoria = repositorioCategoria;
    this.servicioReglaVencimiento = servicioReglaVencimiento;
  }

  @Override
  public List<TimerDTO> obtenerTimersActivos(Long idCategoria) {
    ValidacionHelper.validarId(idCategoria);

    List<Timer> timers = ValidacionHelper.queLaListaNoSeaNull(
      repositorioTimer.obtenerTimersSegunEstado(idCategoria, EstadoTimer.ACTIVO),
      "obtenerTimersSegunEstado"
    );

    return timers.stream().map(this::mapearATimerDTO).collect(Collectors.toList());
  }

  @Override
  public void modificarEstado(Long timerId, EstadoTimer estado) {
    Timer timer = repositorioTimer.buscarPorId(timerId);
    ValidacionHelper.queNoSeaNull(timer, TIMER);

    if (timer.getFechaVencimiento().isBefore(OffsetDateTime.now())) {
      timer.setEstado(EstadoTimer.VENCIDO);
    } else {
      timer.setEstado(estado);
    }

    repositorioTimer.guardar(timer);
  }

  @Override
  public CategoriaDto importarTimer(Long timerId, Long categoriaId) {
    Timer timer = repositorioTimer.buscarPorId(timerId);
    ValidacionHelper.queNoSeaNull(timer, "timer");

    Categoria categoriaDestino = repositorioCategoria.buscarPorId(categoriaId);
    ValidacionHelper.queNoSeaNull(categoriaDestino, "categoria de destino");

    if (timer.getCategoria().getId().equals(categoriaId)) {
      throw new ValidacionException("El timer ya pertenece a esta categoría");
    }

    if (repositorioTimer.existeTimerActivoEnCategoriaYGrupo(categoriaId, timer.getGroupId())) {
      throw new ValidacionException("El timer ya fue importado a esta categoría");
    }

    Timer clon = new Timer(
      timer.getFechaCreacion(),
      timer.getFechaVencimiento(),
      timer.getGroupId(),
      timer.getProducto(),
      categoriaDestino,
      timer.getReglaVencimiento()
    );

    repositorioTimer.guardar(clon);
    return new CategoriaDto(categoriaDestino);
  }

  @Override
  public Timer buscarPorId(Long id) {
    Timer timer = repositorioTimer.buscarPorId(id);
    ValidacionHelper.queNoSeaNull(timer, TIMER);
    return timer;
  }

  @Override
  public TimerDTO renovarTimer(Timer timer) {
    ReglaVencimiento regla = timer.getReglaVencimiento();
    ValidacionHelper.queNoSeaNull(regla, "Regla vencimiento");

    modificarEstado(timer.getId(), EstadoTimer.RENOVADO);
    Timer nuevoTimer = servicioReglaVencimiento.generarVencimiento(
      timer.getProducto(),
      timer.getCategoria(),
      regla.getId(),
      null, 1
    );

    return mapearATimerDTO(nuevoTimer);
  }

  private String obtenerNombreProducto(Timer timer) {
    if (timer.getProducto() == null) return "Producto desconocido";
    String nombre = timer.getProducto().getNombre();
    ValidacionHelper.validarCampoSeguro(nombre, "nombre del producto");
    return nombre;
  }

  private String obtenerUbicacion(Timer timer) {
    if (timer.getReglaVencimiento() == null) return "General";
    String ubicacion = timer.getReglaVencimiento().getUbicacion();
    ValidacionHelper.validarCampoSeguro(ubicacion, "ubicacion del producto");
    return ubicacion;
  }

  private String formatearFecha(Object fecha) {
    return fecha != null ? fecha.toString() : "";
  }

  private TimerDTO mapearATimerDTO(Timer timer) {
    ValidacionHelper.queNoSeaNull(timer, TIMER);
    return new TimerDTO(
      timer.getId(),
      obtenerNombreProducto(timer),
      timer.getGroupId(),
      formatearFecha(timer.getFechaCreacion()),
      formatearFecha(timer.getFechaVencimiento()),
      obtenerUbicacion(timer)
    );
  }
}
