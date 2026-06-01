package com.tallerwebi.repositorio;

import static org.junit.jupiter.api.Assertions.*;

import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.interfaces.RepositorioReglaVencimiento;
import com.tallerwebi.repositorio.config.HibernateInfraestructuraTestConfig;
import javax.transaction.Transactional;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
@ActiveProfiles("test")
public class RepositorioReglaVencimientoTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioReglaVencimiento repositorioReglaVencimiento;

  @BeforeEach
  public void init() {
    repositorioReglaVencimiento = new RepositorioReglaVencimientoImpl(sessionFactory);
  }

  // ===================== guardar =====================

  @Test
  @Transactional
  @Rollback
  @DisplayName("HP-01 | guardar | Guarda una regla de vencimiento y se puede recuperar por id")
  public void guardar_deberiaGuardarUnaReglaCorrectamente() {
    ReglaVencimiento regla = ReglaVencimiento
      .builder()
      .ubicacion("Heladera")
      .duracionMinutos(1080)
      .tieneDescongelamiento(false)
      .build();

    repositorioReglaVencimiento.guardar(regla);
    sessionFactory.getCurrentSession().flush();

    ReglaVencimiento resultado = repositorioReglaVencimiento.obtenerReglaVencimientoPorId(
      regla.getId()
    );

    assertNotNull(resultado);
    assertEquals("Heladera", resultado.getUbicacion());
    assertEquals(1080, resultado.getDuracionMinutos());
  }

  // ===================== obtenerReglaVencimientoPorId =====================

  @Test
  @Transactional
  @Rollback
  @DisplayName("NEG-01 | obtenerReglaVencimientoPorId | Retorna null cuando el id no existe")
  public void obtenerReglaVencimientoPorId_deberiaRetornarNullCuandoNoExiste() {
    ReglaVencimiento resultado = repositorioReglaVencimiento.obtenerReglaVencimientoPorId(999L);

    assertNull(resultado);
  }
}
