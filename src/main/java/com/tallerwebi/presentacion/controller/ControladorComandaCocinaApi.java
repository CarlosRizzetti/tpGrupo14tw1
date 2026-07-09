package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.excepcion.IngredientesNoDisponiblesException;
import com.tallerwebi.dominio.interfaces.ServicioComanda;
import com.tallerwebi.presentacion.dto.ComandaCocinaDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cocina/comandas")
public class ControladorComandaCocinaApi {

  private final ServicioComanda servicioComanda;

  @Autowired
  public ControladorComandaCocinaApi(ServicioComanda servicioComanda) {
    this.servicioComanda = servicioComanda;
  }

  @GetMapping
  public List<ComandaCocinaDTO> listarPorCategoria(@RequestParam Long idCategoria) {
    return servicioComanda.listarPendientesPorCategoria(idCategoria);
  }

  @PostMapping("/{id}/sacar")
  public ResponseEntity<Map<String, Object>> sacar(@PathVariable Long id) {
    try {
      servicioComanda.sacarComanda(id);
      Map<String, Object> respuesta = new HashMap<>();
      respuesta.put("ok", true);
      return ResponseEntity.ok(respuesta);
    } catch (IngredientesNoDisponiblesException ex) {
      Map<String, Object> error = new HashMap<>();
      error.put("error", "faltan_timers");
      error.put("mensaje", ex.getMessage());
      error.put(
        "productos",
        ex
          .getFaltantes()
          .stream()
          .map(p -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("nombre", p.getNombre());
            return item;
          })
          .collect(Collectors.toList())
      );
      return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
  }
}
