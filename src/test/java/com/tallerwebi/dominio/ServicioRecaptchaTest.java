package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.services.ServicioRecaptcha;
import com.tallerwebi.presentacion.dto.RecaptchaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class ServicioRecaptchaTest {

  private RestTemplate restTemplateMock;

  private ServicioRecaptcha servicio;
  private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
  private static final String SECRET_KEY = "clave-secreta-test";

  @BeforeEach
  public void setUp() {
    this.restTemplateMock = mock(RestTemplate.class);
    this.servicio = new ServicioRecaptcha(restTemplateMock);
    ReflectionTestUtils.setField(servicio, "secretKey", SECRET_KEY);
  }

  @Test
  public void queRetorneFalseSiElTokenEsNull() {
    boolean resultado = servicio.verificar(null);
    assertFalse(resultado);
  }

  @Test
  public void queRetorneFalseSiElTokenEstaVacio() {
    boolean resultado = servicio.verificar("");

    assertFalse(resultado);
  }

  @Test
  public void queRetorneFalseSiGoogleRetornaNull() {
    String token = "token-valido";
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("secret", SECRET_KEY);
    body.add("response", token);

    when(restTemplateMock.postForObject(eq(VERIFY_URL), eq(body), eq(RecaptchaDTO.class)))
      .thenReturn(null);

    boolean resultado = servicio.verificar(token);

    assertFalse(resultado);
    verify(restTemplateMock).postForObject(eq(VERIFY_URL), eq(body), eq(RecaptchaDTO.class));
  }

  @Test
  public void queRetorneFalseSiGoogleVerificaQueEsUnRobot() {
    String token = "token-sospechoso";
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("secret", SECRET_KEY);
    body.add("response", token);

    RecaptchaDTO respuestaMock = new RecaptchaDTO();
    respuestaMock.setSuccess(false);

    when(restTemplateMock.postForObject(eq(VERIFY_URL), eq(body), eq(RecaptchaDTO.class)))
      .thenReturn(respuestaMock);

    boolean resultado = servicio.verificar(token);

    assertFalse(resultado);
  }

  @Test
  public void queRetorneTrueSiGoogleValidaExitosamente() {
    String token = "token-perfecto";

    RecaptchaDTO respuestaMock = new RecaptchaDTO();
    respuestaMock.setSuccess(true);

    when(restTemplateMock.postForObject(eq(VERIFY_URL), any(), eq(RecaptchaDTO.class)))
      .thenReturn(respuestaMock);

    boolean resultado = servicio.verificar(token);

    assertTrue(resultado);

    ArgumentCaptor<MultiValueMap<String, String>> captor = ArgumentCaptor.forClass(
      MultiValueMap.class
    );

    verify(restTemplateMock)
      .postForObject(eq(VERIFY_URL), captor.capture(), eq(RecaptchaDTO.class));

    MultiValueMap<String, String> cuerpoEnviado = captor.getValue();
    assertEquals(SECRET_KEY, cuerpoEnviado.getFirst("secret"));
    assertEquals(token, cuerpoEnviado.getFirst("response"));
  }
}
