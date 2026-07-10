package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.tallerwebi.dominio.entity.ProductoFinal;
import com.tallerwebi.dominio.entity.Receta;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

public class ProductoFinalTest {

  @Test
  public void queSePuedanManejarPropiedades() {
    ProductoFinal pf = new ProductoFinal();
    pf.setId(10L);
    pf.setNombre("Milanesa");
    pf.setPrecio(BigDecimal.valueOf(1500));

    assertThat(pf.getId(), equalTo(10L));
    assertThat(pf.getNombre(), equalTo("Milanesa"));
    assertThat(pf.getPrecio(), equalTo(BigDecimal.valueOf(1500)));
  }
}
