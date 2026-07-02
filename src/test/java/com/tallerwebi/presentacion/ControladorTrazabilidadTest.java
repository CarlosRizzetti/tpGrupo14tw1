package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Trazabilidad;
import com.tallerwebi.dominio.interfaces.ServicioProduccion;
import com.tallerwebi.presentacion.controller.ControladorTrazabilidad;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorTrazabilidadTest {

  private ControladorTrazabilidad controladorTrazabilidad;
  private ServicioProduccion servicioProduccionMock;

  @BeforeEach
  public void init() {
    servicioProduccionMock = mock(ServicioProduccion.class);
    controladorTrazabilidad = new ControladorTrazabilidad(servicioProduccionMock);
  }

  @Test
  public void verTrazabilidadDeberiaRetornarVistaHistorial() {
    List<Trazabilidad> listaEsperada = Arrays.asList(new Trazabilidad());
    when(servicioProduccionMock.obtenerTrazabilidadCompleta()).thenReturn(listaEsperada);

    ModelAndView mav = controladorTrazabilidad.verTrazabilidad();

    assertThat(mav.getViewName(), equalToIgnoringCase("trazabilidad/historial"));
    assertThat(mav.getModel().get("historial"), equalTo(listaEsperada));
  }
}
