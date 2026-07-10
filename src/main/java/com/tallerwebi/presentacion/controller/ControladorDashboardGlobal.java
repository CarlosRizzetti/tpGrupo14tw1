package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.dominio.interfaces.ServicioTimer;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.TimerDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
/**
 * Controlador para la vista global del dashboard.
 * Muestra las categorías y los timers activos por categoría.
 */
public class ControladorDashboardGlobal {

  private final ServicioCategoria servicioCategoria;
  private final ServicioTimer servicioDashboard;

  @Autowired
  public ControladorDashboardGlobal(
    ServicioCategoria servicioCategoria,
    ServicioTimer servicioDashboard
  ) {
    this.servicioCategoria = servicioCategoria;
    this.servicioDashboard = servicioDashboard;
  }

  @GetMapping("/dashboard/global")
  public ModelAndView dashboardGlobal(
    org.springframework.security.core.Authentication authentication
  ) {
    try {
      ModelAndView mav = new ModelAndView("dashboard/global");

      List<CategoriaDto> categorias = servicioCategoria.obtenerLasCategoriasParaElMenu();

      if (categorias == null || categorias.isEmpty()) {
        mav.addObject("error", "No hay categorías disponibles");
        return mav;
      }

      Map<Long, List<TimerDTO>> timersPorCategoria = new HashMap<>();

      for (CategoriaDto categoria : categorias) {
        List<TimerDTO> timers = servicioDashboard.obtenerTimersActivos(categoria.getId());
        timersPorCategoria.put(categoria.getId(), timers);
      }

      boolean anyTimers = timersPorCategoria
        .values()
        .stream()
        .anyMatch(timers -> timers != null && !timers.isEmpty());

      boolean isAdmin =
        authentication != null &&
        authentication.isAuthenticated() &&
        authentication
          .getAuthorities()
          .stream()
          .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
      List<Long> categoriasAsignadasIds = new java.util.ArrayList<>();
      if (authentication != null && authentication.isAuthenticated()) {
        String email = authentication.getName();
        List<CategoriaDto> categoriasUsuario = servicioCategoria.obtenerCategoriasPorUsuario(email);
        for (CategoriaDto c : categoriasUsuario) {
          categoriasAsignadasIds.add(c.getId());
        }
      }

      mav.addObject("categorias", categorias);
      mav.addObject("timersPorCategoria", timersPorCategoria); // 👈 SIEMPRE
      mav.addObject("categoriasAsignadasIds", categoriasAsignadasIds);
      mav.addObject("isAdmin", isAdmin);

      if (!anyTimers) {
        mav.addObject("error", "No hay timers activos");
      }

      return mav;
    } catch (Exception e) {
      ModelAndView mav = new ModelAndView("dashboard/global");
      mav.addObject("error", "Error al cargar el dashboard global");
      return mav;
    }
  }
}
