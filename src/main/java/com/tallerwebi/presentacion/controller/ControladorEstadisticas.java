package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.interfaces.ServicioEstadistica;
import com.tallerwebi.presentacion.dto.EstadisticasDTO;
import com.tallerwebi.presentacion.dto.PuntoEstadisticoDTO;
import java.io.InputStream;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controlador de la sección de estadísticas.
 * Solo delega en {@link ServicioEstadistica}; no accede a repositorios.
 */
@Controller
public class ControladorEstadisticas {

  private static final int DIAS_POR_DEFECTO = 30;

  private final ServicioEstadistica servicioEstadistica;

  @Autowired
  public ControladorEstadisticas(ServicioEstadistica servicioEstadistica) {
    this.servicioEstadistica = servicioEstadistica;
  }

  @GetMapping("/admin/estadisticas")
  public ModelAndView index() {
    ModelAndView mav = new ModelAndView("estadisticas/estadisticas");
    mav.addObject("dias", DIAS_POR_DEFECTO);
    return mav;
  }

  @GetMapping("/admin/estadisticas/datos")
  @ResponseBody
  public ResponseEntity<EstadisticasDTO> obtenerDatos(
    @RequestParam(name = "dias", defaultValue = "30") int dias
  ) {
    try {
      EstadisticasDTO estadisticas = servicioEstadistica.obtenerEstadisticas(dias);
      return ResponseEntity.ok(estadisticas);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/admin/estadisticas/exportar/excel")
  public void exportarExcel(
    @RequestParam(name = "dias", defaultValue = "30") int dias,
    HttpServletResponse response
  ) {
    try {
      response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
      response.setHeader(
        "Content-Disposition",
        "attachment; filename=\"reporte_estadisticas_" + dias + "_dias.xlsx\""
      );

      try (
        InputStream plantillaIn = getClass().getResourceAsStream("/plantilla_estadisticas.xlsx")
      ) {
        if (plantillaIn == null) {
          throw new RuntimeException(
            "ERROR CRÍTICO: No se encontró plantilla_estadisticas.xlsx en resources"
          );
        }

        try (Workbook workbook = new XSSFWorkbook(plantillaIn)) {
          EstadisticasDTO estadisticas = servicioEstadistica.obtenerEstadisticas(dias);

          llenarHojaEstadistica(
            workbook,
            "Productos Más Utilizados",
            "Nombre del Producto",
            "Cantidad",
            estadisticas.getProductosMasUtilizados()
          );
          llenarHojaEstadistica(
            workbook,
            "Vencimientos por Estado",
            "Estado del Timer",
            "Cantidad",
            estadisticas.getVencimientosPorEstado()
          );
          llenarHojaEstadistica(
            workbook,
            "Vencimientos por Día",
            "Fecha",
            "Cantidad",
            estadisticas.getVencimientosPorDia()
          );
          llenarHojaEstadistica(
            workbook,
            "Modificaciones Stock",
            "Fecha",
            "Movimientos",
            estadisticas.getModificacionesStockPorDia()
          );
          llenarHojaEstadistica(
            workbook,
            "Demanda por Día",
            "Día",
            "Egresos",
            estadisticas.getDemandaPorDiaSemana()
          );
          llenarHojaEstadistica(
            workbook,
            "Demanda por Hora",
            "Hora",
            "Egresos",
            estadisticas.getDemandaPorHora()
          );

          workbook.setForceFormulaRecalculation(true);
          workbook.write(response.getOutputStream());
        }
      }
    } catch (IllegalArgumentException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Método de soporte para rellenar las pestañas inyectando los datos del DTO.
   */
  private void llenarHojaEstadistica(
    Workbook workbook,
    String nombreHoja,
    String col1,
    String col2,
    List<PuntoEstadisticoDTO> datos
  ) {
    Sheet sheet = workbook.getSheet(nombreHoja);

    if (sheet == null) {
      sheet = workbook.createSheet(nombreHoja);
    }

    CellStyle estiloCabecera = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    estiloCabecera.setFont(font);

    Row header = sheet.createRow(0);
    header.createCell(0).setCellValue(col1);
    header.getCell(0).setCellStyle(estiloCabecera);
    header.createCell(1).setCellValue(col2);
    header.getCell(1).setCellStyle(estiloCabecera);

    for (int i = 0; i < datos.size(); i++) {
      Row row = sheet.createRow(i + 1);
      PuntoEstadisticoDTO punto = datos.get(i);
      row.createCell(0).setCellValue(punto.getEtiqueta());
      row.createCell(1).setCellValue(punto.getValor());
    }

    sheet.autoSizeColumn(0);
    sheet.autoSizeColumn(1);
  }
}
