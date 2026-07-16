package com.tallerwebi.dominio.evento;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class InteraccionChatEvent {

  private final Long pedidoId;
  private final String accionUsuario;
  private String respuestaSistema;
  private List<String> opcionesDisponibles = new ArrayList<>();

  public InteraccionChatEvent(Long pedidoId, String accionUsuario) {
    this.pedidoId = pedidoId;
    this.accionUsuario = accionUsuario;
  }
}
