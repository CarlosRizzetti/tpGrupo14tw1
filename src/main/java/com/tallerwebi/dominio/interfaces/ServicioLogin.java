package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioInactivo;

public interface ServicioLogin {
  Usuario consultarUsuario(String email, String password) throws UsuarioInactivo;
  void registrar(Usuario usuario) throws UsuarioExistente;
}
