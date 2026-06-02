package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.excepcion.IdInvalido;
import com.tallerwebi.dominio.excepcion.ValidacionException;
import com.tallerwebi.dominio.interfaces.ServicioProducto;
import com.tallerwebi.dominio.interfaces.ServicioTimer;
import com.tallerwebi.dominio.utils.ValidacionHelper;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.ResponseDTO;
import com.tallerwebi.presentacion.dto.TimerDTO;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorDashboard {

  private ServicioProducto servicioProducto;
  public ServicioTimer servicioTimer;

  @Autowired
  public ControladorDashboard(ServicioTimer servicioTimer, ServicioProducto servicioProducto) {
    this.servicioTimer = servicioTimer;
    this.servicioProducto = servicioProducto;
  }

  @GetMapping("/dashboard")
  public ModelAndView index(HttpSession session) {
    CategoriaDto categoria = (CategoriaDto) session.getAttribute("categoria");

    if (categoria == null) {
      return new ModelAndView("redirect:/home");
    }

    ModelAndView mav = new ModelAndView("dashboard");
    mav.addObject("categoria", categoria);
    List<TimerDTO> timersActivos = servicioTimer.obtenerTimersActivos(categoria.getId());

    if (timersActivos.isEmpty()) {
      mav.addObject("error", "No hay timers activos");
    } else {
      mav.addObject("timers", timersActivos);
    }

    return mav;
  }

  @DeleteMapping("/active-timers/{timerId}")
  public ResponseEntity<String> eliminarTimer(@PathVariable Long timerId) {
    try {
      ValidacionHelper.validarId(timerId);
      servicioTimer.modificarEstadoAEliminado(timerId);
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
    @PathVariable Long categoryId,
    HttpSession session
  ) {
    try {
      ValidacionHelper.validarId(timerId);
      ValidacionHelper.validarId(categoryId);

      CategoriaDto categoriaDestino = servicioTimer.importarTimer(timerId, categoryId);

      ResponseDTO response = new ResponseDTO();
      response.setSuccess(true);
      response.setMessage("Timer importado correctamente");
      session.setAttribute("categoria", categoriaDestino);
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

  @GetMapping("/timers/{timerId}/categories")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> obtenerCategoriasDeUnProducto(
    @PathVariable Long timerId
  ) {
    try {
      ValidacionHelper.validarId(timerId);
      Timer timer = servicioTimer.buscarPorId(timerId);
      ValidacionHelper.queNoSeaNull(timer, "timer");
      Producto producto = timer.getProducto();
      ValidacionHelper.queNoSeaNull(producto, "producto");
      String groupId = timer.getGroupId();
      List<CategoriaDto> categorias =
        servicioProducto.obtenerCategoriasDeUnProductoDisponiblesParaImportar(
          producto.getId(),
          groupId
        );
      ValidacionHelper.queLaListaNoSeaNull(categorias, "categorias del producto");
      Map<String, Object> response = Map.of("categorias", categorias);
      return ResponseEntity.ok(response);
    } catch (IdInvalido e) {
      return ResponseEntity.badRequest().build();
    } catch (ValidacionException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
