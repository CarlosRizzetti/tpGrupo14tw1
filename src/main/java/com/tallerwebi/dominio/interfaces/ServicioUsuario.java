package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.excepcion.PasswordInvalida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.presentacion.dto.UsuarioDto;
import java.util.List;

public interface ServicioUsuario {
  List<Usuario> listarUsuarios();
  void crearUsuario(UsuarioDto dto) throws UsuarioExistente, PasswordInvalida;
  Usuario obtenerUsuarioPorId(Long id);
  void editarUsuario(Long id, UsuarioDto dto) throws PasswordInvalida;
  void darDeBaja(Long id);
  void reactivar(Long id);
}
