package com.tallerwebi.presentacion.dto;

import com.tallerwebi.dominio.entity.Comanda;
import com.tallerwebi.dominio.entity.ComandaSector;
import com.tallerwebi.dominio.entity.DetallePedido;
import com.tallerwebi.dominio.entity.DetallePedidoIngrediente;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ComandaCocinaDTO {

  private Long idSector;
  private Long id;
  private Long idPedido;
  private String horaCobro;
  private List<LineaDto> lineas = new ArrayList<>();

  public ComandaCocinaDTO(ComandaSector sector) {
    this(sector.getComanda());
    this.idSector = sector.getId();
  }

  public ComandaCocinaDTO(Comanda comanda) {
    this.id = comanda.getId();
    this.idPedido = comanda.getPedido().getId();
    this.horaCobro =
      comanda.getPedido().getHoraCobro() != null
        ? comanda.getPedido().getHoraCobro().toString()
        : null;

    for (DetallePedido detalle : comanda.getPedido().getDetalles()) {
      this.lineas.add(new LineaDto(detalle));
    }
  }

  @Getter
  @Setter
  public static class LineaDto {

    private String nombre;
    private boolean tieneIngredientes;
    private List<IngredienteLineaDto> ingredientes = new ArrayList<>();

    public LineaDto(DetallePedido detalle) {
      this.nombre = detalle.getProductoFinal().getNombre();
      this.tieneIngredientes = !detalle.getIngredientes().isEmpty();
      for (DetallePedidoIngrediente ing : detalle.getIngredientes()) {
        this.ingredientes.add(new IngredienteLineaDto(ing));
      }
    }
  }

  @Getter
  @Setter
  public static class IngredienteLineaDto {

    private String nombre;
    private Integer cantidad;

    public IngredienteLineaDto(DetallePedidoIngrediente ing) {
      this.nombre = ing.getProducto().getNombre();
      this.cantidad = ing.getCantidad();
    }
  }
}
