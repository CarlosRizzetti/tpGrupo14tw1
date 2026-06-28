package com.tallerwebi.presentacion.dto;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioListadoDto {

  private Long id;
  private String nombre;
  private String email;
  private String rol;
  private String estado;

  // Nombres de categorías (lo que usa Thymeleaf)
  private List<String> categorias;
}
