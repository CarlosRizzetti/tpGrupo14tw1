package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.tallerwebi.dominio.utils.ValidadorPassword;
import org.junit.jupiter.api.Test;

public class ValidadorPasswordTest {

  @Test
  public void siLaPasswordCumpleTodosLosRequisitosDeberiaSerValida() {
    String passwordValida = "Contra1"; // 7 chars, uppercase, number
    boolean resultado = ValidadorPassword.esValida(passwordValida);
    assertThat(resultado, is(true));
  }

  @Test
  public void siLaPasswordEsMuyCortaDeberiaSerInvalida() {
    String passwordCorta = "Con1"; // 4 chars
    boolean resultado = ValidadorPassword.esValida(passwordCorta);
    assertThat(resultado, is(false));
  }

  @Test
  public void siLaPasswordEsMuyLargaDeberiaSerInvalida() {
    String passwordLarga = "ContrasenaMuyLarga123"; // > 10 chars
    boolean resultado = ValidadorPassword.esValida(passwordLarga);
    assertThat(resultado, is(false));
  }

  @Test
  public void siLaPasswordNoTieneMayusculaDeberiaSerInvalida() {
    String passwordSinMayuscula = "contra1";
    boolean resultado = ValidadorPassword.esValida(passwordSinMayuscula);
    assertThat(resultado, is(false));
  }

  @Test
  public void siLaPasswordNoTieneNumeroDeberiaSerInvalida() {
    String passwordSinNumero = "Contra";
    boolean resultado = ValidadorPassword.esValida(passwordSinNumero);
    assertThat(resultado, is(false));
  }

  @Test
  public void siLaPasswordEsNullDeberiaSerInvalida() {
    boolean resultado = ValidadorPassword.esValida(null);
    assertThat(resultado, is(false));
  }

  @Test
  public void siLaPasswordEstaVaciaDeberiaSerInvalida() {
    boolean resultado = ValidadorPassword.esValida("");
    assertThat(resultado, is(false));
  }

  @Test
  public void siLaPasswordTieneEspaciosDeberiaSerValidaSiCumpleDemásRequisitos() {
    // El validador actual no prohibe espacios, solo pide largo, mayuscula y numero.
    String passwordConEspacio = "Valida 1";
    boolean resultado = ValidadorPassword.esValida(passwordConEspacio);
    assertThat(resultado, is(true));
  }

  @Test
  public void siLaPasswordTieneCaracteresEspecialesDeberiaSerValidaSiCumpleDemásRequisitos() {
    String passwordEspecial = "Valida!1";
    boolean resultado = ValidadorPassword.esValida(passwordEspecial);
    assertThat(resultado, is(true));
  }
}
