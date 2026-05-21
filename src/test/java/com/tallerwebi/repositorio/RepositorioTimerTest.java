package com.tallerwebi.repositorio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.repositorio.config.HibernateInfraestructuraTestConfig;
import java.time.OffsetDateTime;
import java.util.List;
import javax.transaction.Transactional;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
@ActiveProfiles("test")
public class RepositorioTimerTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioTimer repositorioTimer;

  @BeforeEach
  public void init() {
    repositorioTimer = new RepositorioTimerImpl(sessionFactory);
  }

  @Test
  @Transactional
  public void obtenerTodosLosTimersActivos() {
    OffsetDateTime fechaCreacion = OffsetDateTime.now();
    OffsetDateTime fechaVencimiento = fechaCreacion.plusDays(3);
    Categoria categoria = new Categoria("mccafe.png", true, "mccafe");
    Producto producto = new Producto();
    ReglaVencimiento regla = new ReglaVencimiento();
    Timer timer = new Timer(fechaCreacion, fechaVencimiento, "1AF34", producto, categoria, regla);
    Timer timer2 = new Timer(fechaCreacion, fechaVencimiento, "1asd4", producto, categoria, regla);
    timer2.setEstado("inactivo");
    sessionFactory.getCurrentSession().save(categoria);
    sessionFactory.getCurrentSession().save(producto);
    sessionFactory.getCurrentSession().save(regla);
    sessionFactory.getCurrentSession().save(timer);
    sessionFactory.getCurrentSession().save(timer2);

    List<Timer> timers =
      this.repositorioTimer.obtenerTimersSegunEstado(categoria.getId(), "activo");
    assertEquals(1, timers.size());
    assertThat(categoria.getNombre(), is(timers.get(0).getCategoria().getNombre()));
  }
}
