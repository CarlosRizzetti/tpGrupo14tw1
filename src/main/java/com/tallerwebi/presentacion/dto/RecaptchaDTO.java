package com.tallerwebi.presentacion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RecaptchaDTO {

  private boolean success;

  @JsonProperty("error-codes")
  private String[] errorCodes;

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }
}
