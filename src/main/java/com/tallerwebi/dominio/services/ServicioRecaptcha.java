package com.tallerwebi.dominio.services;

import com.tallerwebi.presentacion.dto.RecaptchaDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class ServicioRecaptcha {

  @Value("${recaptcha.secret-key}")
  private String secretKey;

  private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
  private final RestTemplate restTemplate;

  // 2. Lo inyectamos por constructor
  public ServicioRecaptcha(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public boolean verificar(String recaptchaResponse) {
    if (recaptchaResponse == null || recaptchaResponse.isEmpty()) {
      return false;
    }

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("secret", secretKey);
    body.add("response", recaptchaResponse);

    // Hace el POST a Google y mapea la respuesta
    RecaptchaDTO apiResponse = restTemplate.postForObject(VERIFY_URL, body, RecaptchaDTO.class);

    return apiResponse != null && apiResponse.isSuccess();
  }
}
