package com.tallerwebi.presentacion;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.interfaces.ServicioArticulo;
import com.tallerwebi.dominio.interfaces.ServicioProducto;
import com.tallerwebi.dominio.interfaces.ServicioReceta;
import com.tallerwebi.presentacion.controller.ControladorReceta;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class ControladorRecetaIntegrationTest {

  private MockMvc mockMvc;

  @Mock
  private ServicioReceta servicioReceta;

  @Mock
  private ServicioProducto servicioProducto;

  @Mock
  private ServicioArticulo servicioArticulo;

  @InjectMocks
  private ControladorReceta controladorReceta;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(controladorReceta).build();
  }

  @Test
  public void testGuardarRecetaConMultiplesArticulos() throws Exception {
    Producto p = new Producto();
    p.setId(1L);
    when(servicioProducto.obtenerProductoPorId(1L)).thenReturn(p);

    mockMvc
      .perform(
        post("/admin/recetas/guardar")
          .param("productoId", "1")
          .param("articulosIds", "10")
          .param("articulosIds", "20")
          .param("cantidades", "5.5")
          .param("cantidades", "10.0")
      )
      .andExpect(status().is3xxRedirection());

    verify(servicioReceta)
      .guardarReceta(eq(p), eq(Arrays.asList(10L, 20L)), eq(Arrays.asList(5.5, 10.0)));
  }
}
