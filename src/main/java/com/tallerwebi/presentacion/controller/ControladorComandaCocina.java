package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.dominio.interfaces.ServicioComanda;
import com.tallerwebi.dominio.utils.AuthenticationUtils;
import com.tallerwebi.presentacion.dto.CategoriaComandasDTO;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/cocina/comandas")
public class ControladorComandaCocina {

  private final ServicioCategoria servicioCategoria;
  private final ServicioComanda servicioComanda;

  @Autowired
  public ControladorComandaCocina(
    ServicioCategoria servicioCategoria,
    ServicioComanda servicioComanda
  ) {
    this.servicioCategoria = servicioCategoria;
    this.servicioComanda = servicioComanda;
  }

  @RequestMapping(path = "", method = RequestMethod.GET)
  public ModelAndView mostrarBotonera() {
    List<CategoriaDto> categoriasDelUsuario = obtenerCategoriasDelUsuarioLogueado();

    List<CategoriaComandasDTO> categoriasConBadge = categoriasDelUsuario
      .stream()
      .map(c -> new CategoriaComandasDTO(c, servicioComanda.contarPendientesPorCategoria(c.getId()))
      )
      .collect(Collectors.toList());

    ModelAndView mav = new ModelAndView("comandas/comandas-categorias");
    mav.addObject("categorias", categoriasConBadge);
    return mav;
  }

  @RequestMapping(path = "/categoria/{idCategoria}", method = RequestMethod.GET)
  public ModelAndView mostrarComandasDeCategoria(@PathVariable Long idCategoria) {
    CategoriaDto categoria = servicioCategoria.obtenerCategoriaPorId(idCategoria);

    ModelAndView mav = new ModelAndView("comandas/comandas");
    mav.addObject("categoria", categoria);
    return mav;
  }

  private List<CategoriaDto> obtenerCategoriasDelUsuarioLogueado() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String email = AuthenticationUtils.obtenerEmailDeAutenticacion(auth);

    boolean esAdmin = auth
      .getAuthorities()
      .stream()
      .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

    return esAdmin
      ? servicioCategoria.obtenerLasCategoriasParaElMenu()
      : servicioCategoria.obtenerCategoriasPorUsuario(email);
  }
}
