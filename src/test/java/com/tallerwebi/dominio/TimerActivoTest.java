package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.repositorio.config.HibernateInfraestructuraTestConfig;
import java.time.OffsetDateTime;
import javax.transaction.Transactional;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
@ActiveProfiles("test")
public class TimerActivoTest {

  @Autowired
  SessionFactory sessionFactory;

  @Transactional
  @Test
  public void queAlNoTenerGroupIdSeGenereUnoAutomaticamente() {
    OffsetDateTime fechaCreacion = OffsetDateTime.now();
    OffsetDateTime fechaVencimiento = fechaCreacion.plusDays(3);

    Timer timer = new Timer(fechaCreacion, fechaVencimiento);
    sessionFactory.getCurrentSession().save(timer);
    assertNotNull(timer.getGroupId());
  }

  @Transactional
  @Test
  public void queAlAsignarUnGroupIdVacioSeGenereUnoAutomaticamente() {
    OffsetDateTime fechaCreacion = OffsetDateTime.now();
    OffsetDateTime fechaVencimiento = fechaCreacion.plusDays(3);
    Categoria categoria = new Categoria("mccafe.png", true, "mccafe");
    Producto producto = new Producto();
    ReglaVencimiento regla = new ReglaVencimiento();
    Timer timer = new Timer(fechaCreacion, fechaVencimiento, "", producto, categoria, regla);
    sessionFactory.getCurrentSession().save(timer);
    assertNotNull(timer.getGroupId());
    assertNotEquals("", timer.getGroupId());
  }

  @Transactional
  @Test
  public void queAlAsignarUnGroupIdNoSeGenereUnoAutomaticamente() {
    OffsetDateTime fechaCreacion = OffsetDateTime.now();
    OffsetDateTime fechaVencimiento = fechaCreacion.plusDays(3);
    Categoria categoria = new Categoria("mccafe.png", true, "mccafe");
    Producto producto = new Producto();
    ReglaVencimiento regla = new ReglaVencimiento();
    Timer timer = new Timer(fechaCreacion, fechaVencimiento, "1AF34", producto, categoria, regla);
    sessionFactory.getCurrentSession().save(timer);
    assertNotNull(timer.getGroupId());
    assertEquals("1AF34", timer.getGroupId());
  }
}
