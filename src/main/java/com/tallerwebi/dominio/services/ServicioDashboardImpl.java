package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.excepcion.TimerMapeadoException;
import com.tallerwebi.dominio.excepcion.ValidacionException;
import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.dominio.interfaces.ServicioDashboard;
import com.tallerwebi.dominio.utils.ValidacionHelper;
import com.tallerwebi.presentacion.dto.TimerDTO;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
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
    ValidacionHelper.validarId(id);

    List<Timer> timers = ValidacionHelper.queLaListaNoSeaNull(
      repositorioTimer.obtenerTimersSegunEstado(id, "activo"),
      "obtenerTimersSegunEstado"
    );

    return timers.stream().map(this::mapearATimerDTO).collect(Collectors.toList());
  }

  private TimerDTO mapearATimerDTO(Timer timer) {
    try {
      if (timer == null) {
        throw new TimerMapeadoException("Se encontró un timer nulo en la lista", null);
      }
      return new TimerDTO(
        timer.getId(),
        obtenerNombreProducto(timer),
        timer.getGroupId(),
        formatearFecha(timer.getFechaCreacion()),
        formatearFecha(timer.getFechaVencimiento()),
        obtenerUbicacion(timer)
      );
    } catch (TimerMapeadoException e) {
      throw e;
    } catch (ValidacionException e) {
      throw e;
    } catch (Exception e) {
      throw new TimerMapeadoException("Error al mapear el timer con ID: " + timer.getId(), e);
    }
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
