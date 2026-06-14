package com.tallerwebi.presentacion.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorAdminPanel {

    @RequestMapping(path = "/admin", method = RequestMethod.GET)
    public ModelAndView panelDeControl(HttpSession session) {
        if (!esAdministrador(session)) {
            return new ModelAndView("redirect:/acceso-denegado");
        }

        ModelMap model = new ModelMap();
        String email = (String) session.getAttribute("EMAIL");
        model.put("email", email);

        return new ModelAndView("funcionalidadesAdmin/panel", model);
    }

    private boolean esAdministrador(HttpSession session) {
        Object rol = session.getAttribute("ROL");
        if (rol == null) {
            return false;
        }
        return "ADMIN".equalsIgnoreCase(rol.toString());
    }

    @RequestMapping(path = "/acceso-denegado", method = RequestMethod.GET)
    public ModelAndView accesoDenegado() {
        return new ModelAndView("acceso-denegado");
    }
}
