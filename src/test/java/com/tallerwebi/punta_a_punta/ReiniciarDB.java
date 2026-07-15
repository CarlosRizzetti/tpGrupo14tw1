package com.tallerwebi.punta_a_punta;

import java.io.IOException;
import java.util.List;

public class ReiniciarDB {

  public static void limpiarBaseDeDatos() {
    try {
      String dbHost = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
      String dbPort = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : "3306";
      String dbName = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "tallerwebi";
      String dbUser = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "user";
      String dbPassword = System.getenv("DB_PASSWORD") != null
        ? System.getenv("DB_PASSWORD")
        : "user";

      String passwordHasheada = "$2a$10$R2lKQIKoYOsk5oWiA.GPXOkHgfyTIW9gceu1W3ne3lHyqWaH3NvkK";

      String sqlCommands =
        "SET FOREIGN_KEY_CHECKS = 0;\n" +
        "DELETE FROM Lote WHERE proveedor = 'Proveedor Test';\n" +
        "DELETE FROM Usuario WHERE email IN ('test@unlam.edu.ar', 'cajero@unlam.edu.ar');;\n" +
        "INSERT INTO Usuario(id, email,estado, password, rol) VALUES(1,'test@unlam.edu.ar', 'ACTIVO', '" +
        passwordHasheada +
        "', 'ADMIN');\n" +
        "INSERT INTO Usuario(id, email,estado, password, rol) VALUES(22,'cajero@unlam.edu.ar', 'ACTIVO', '" +
        passwordHasheada +
        "', 'CAJERO');\n" +
        "INSERT INTO usuarioCategorias(idUsuario, idCategoria) VALUES(22, 1);\n" +
        "SET FOREIGN_KEY_CHECKS = 1;";

      List<String> comando = List.of(
        "docker",
        "exec",
        "mysql-container",
        "mysql",
        "-h",
        dbHost,
        "-P",
        dbPort,
        "-u",
        dbUser,
        "-p" + dbPassword,
        dbName,
        "-e",
        sqlCommands
      );

      ProcessBuilder processBuilder = new ProcessBuilder(comando);
      processBuilder.redirectErrorStream(true);
      Process process = processBuilder.start();

      String salida = new String(process.getInputStream().readAllBytes());
      int exitCode = process.waitFor();

      if (exitCode == 0) {
        System.out.println("Base de datos limpiada exitosamente");
      } else {
        System.err.println("Error al limpiar la base de datos. Exit code: " + exitCode);
        System.err.println(salida);
      }
    } catch (IOException | InterruptedException e) {
      System.err.println("Error ejecutando script de limpieza: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
