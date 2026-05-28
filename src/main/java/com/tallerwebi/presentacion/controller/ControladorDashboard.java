package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.excepcion.IdInvalido;
import com.tallerwebi.dominio.excepcion.ValidacionException;
import com.tallerwebi.dominio.interfaces.ServicioDashboard;
import com.tallerwebi.dominio.services.ServicioTimer;
import com.tallerwebi.dominio.utils.ValidacionHelper;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.ResponseDTO;
import com.tallerwebi.presentacion.dto.TimerDTO;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorDashboard {

  public ServicioDashboard servicioDashboard;
  public ServicioTimer servicioTimer;

  @Autowired
  public ControladorDashboard(ServicioDashboard servicioDashboard, ServicioTimer servicioTimer) {
    this.servicioDashboard = servicioDashboard;
    this.servicioTimer = servicioTimer;
  }

  @GetMapping("/dashboard")
  public ModelAndView index(HttpSession session) {
    CategoriaDto categoria = (CategoriaDto) session.getAttribute("categoria");

    if (categoria == null) {
      return new ModelAndView("redirect:/home");
    }

    ModelAndView mav = new ModelAndView("dashboard");
    mav.addObject("categoria", categoria);
    List<TimerDTO> timersActivos = servicioDashboard.obtenerTimersActivos(categoria.getId());

    if (timersActivos.isEmpty()) {
      mav.addObject("error", "No hay timers activos");
    } else {
      mav.addObject("timers", timersActivos);
    }

    return mav;
  }

  @DeleteMapping("/active-timers/{timerId}/{categoryId}")
  public ResponseEntity<String> eliminarTimer(
    @PathVariable Long timerId,
    @PathVariable Long categoryId
  ) {
    try {
      servicioDashboard.eliminarTimer(timerId);
      return ResponseEntity.ok("Timer eliminado correctamente");
    } catch (IllegalArgumentException e) {
      return ResponseEntity
        .status(org.springframework.http.HttpStatus.BAD_REQUEST)
        .body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity
        .status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
        .body("Error al eliminar el timer");
    }
  }

  @PostMapping("/import-timer/{timerId}/{categoryId}")
  public ResponseEntity<ResponseDTO> importarTimer(
    @PathVariable Long timerId,
    @PathVariable Long categoryId
  ) {
    try {
      ValidacionHelper.validarId(timerId);
      ValidacionHelper.validarId(categoryId);

      Timer timer = servicioTimer.buscarPorId(timerId);
      ValidacionHelper.queNoSeaNull(timer, "timer");

      ValidacionHelper.queNoSeaNull(timer.getCategoria(), "categoria del timer");

      if (timer.getCategoria().getId().equals(categoryId)) {
        throw new ValidacionException("El timer ya pertenece a esta categoría");
      }

      servicioDashboard.importarTimer(timerId, categoryId);

      ResponseDTO response = new ResponseDTO();
      response.setSuccess(true);
      response.setMessage("Timer importado correctamente");
      return ResponseEntity.ok(response);
    } catch (IdInvalido | ValidacionException e) {
      ResponseDTO response = new ResponseDTO();
      response.setSuccess(false);
      response.setMessage(e.getMessage());
      return ResponseEntity.badRequest().body(response);
    } catch (Exception e) {
      ResponseDTO response = new ResponseDTO();
      response.setSuccess(false);
      response.setMessage("Error al importar el timer");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }
}
