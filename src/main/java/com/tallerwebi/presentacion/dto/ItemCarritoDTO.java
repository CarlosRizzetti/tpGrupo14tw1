package com.tallerwebi.presentacion.dto;

import com.tallerwebi.dominio.utils.ItemCarrito;
import com.tallerwebi.dominio.utils.ItemCarritoIngrediente;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemCarritoDTO {

  private Long idLinea;
  private String nombre;
  private BigDecimal precio;
  private List<IngredienteEnItemDto> ingredientes;

  public ItemCarritoDTO(Long idLinea, ItemCarrito item) {
    this.idLinea = idLinea;
    this.nombre = item.getProductoFinal().getNombre();
    this.precio = item.getProductoFinal().getPrecio();
    this.ingredientes =
      item.getIngredientes().stream().map(IngredienteEnItemDto::new).collect(Collectors.toList());
  }

  @Getter
  @Setter
  public static class IngredienteEnItemDto {

    private Long productoId;
    private String nombre;
    private Integer cantidad;

    public IngredienteEnItemDto(ItemCarritoIngrediente ing) {
      this.productoId = ing.getProducto().getId();
      this.nombre = ing.getProducto().getNombre();
      this.cantidad = ing.getCantidadActual();
    }
  }
}
