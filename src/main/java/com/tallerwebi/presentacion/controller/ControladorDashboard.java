package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.dominio.interfaces.ServicioDashboard;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.ResponseDTO;
import com.tallerwebi.presentacion.dto.TimerDTO;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
  public RepositorioTimer repositorioTimer;

  @Autowired
  public ControladorDashboard(ServicioDashboard servicioDashboard, RepositorioTimer repositorioTimer) {
    this.servicioDashboard = servicioDashboard;
    this.repositorioTimer = repositorioTimer;
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

  @GetMapping("/timers/{timerId}/categories")
  public ResponseEntity<Map<String, Object>> getTimerCategories(@PathVariable Long timerId) {
    Timer timer = repositorioTimer.buscarPorId(timerId);

    if (timer == null) {
      return ResponseEntity.notFound().build();
    }

    CategoriaDto categoriaDto = new CategoriaDto(timer.getCategoria());
    Map<String, Object> response = Map.of("categoria", categoriaDto);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/import-timer/{timerId}/{categoryId}")
  public ResponseEntity<ResponseDTO> importTimer(
    @PathVariable Long timerId,
    @PathVariable Long categoryId
  ) {
    ResponseDTO response = new ResponseDTO();

    try {
      Timer timer = repositorioTimer.buscarPorId(timerId);
      if (timer == null) {
        response.setSuccess(false);
        response.setMessage("El timer no existe");
        return ResponseEntity.badRequest().body(response);
      }

      if (timer.getCategoria().getId().equals(categoryId)) {
        response.setSuccess(false);
        response.setMessage("El timer ya pertenece a esta categoría");
        return ResponseEntity.badRequest().body(response);
      }

      servicioDashboard.importarTimer(timerId, categoryId);

      response.setSuccess(true);
      response.setMessage("Timer importado correctamente");

      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      response.setSuccess(false);
      response.setMessage(e.getMessage());

      return ResponseEntity.badRequest().body(response);
    } catch (Exception e) {
      response.setSuccess(false);
      response.setMessage("Error al importar el timer");

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }
}
