package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.*;

import com.tallerwebi.dominio.TimerActivo;
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

    TimerActivo timer = new TimerActivo(fechaCreacion, fechaVencimiento);
    sessionFactory.getCurrentSession().save(timer);
    assertNotNull(timer.getGroupId());
  }

  @Transactional
  @Test
  public void queAlAsignarUnGroupIdVacioSeGenereUnoAutomaticamente() {
    OffsetDateTime fechaCreacion = OffsetDateTime.now();
    OffsetDateTime fechaVencimiento = fechaCreacion.plusDays(3);

    TimerActivo timer = new TimerActivo(fechaCreacion, fechaVencimiento, "");
    sessionFactory.getCurrentSession().save(timer);
    assertNotNull(timer.getGroupId());
    assertNotEquals("", timer.getGroupId());
  }

  @Transactional
  @Test
  public void queAlAsignarUnGroupIdNoSeGenereUnoAutomaticamente() {
    OffsetDateTime fechaCreacion = OffsetDateTime.now();
    OffsetDateTime fechaVencimiento = fechaCreacion.plusDays(3);

    TimerActivo timer = new TimerActivo(fechaCreacion, fechaVencimiento, "1AF34");
    sessionFactory.getCurrentSession().save(timer);
    assertNotNull(timer.getGroupId());
    assertEquals("1AF34", timer.getGroupId());
  }
}
