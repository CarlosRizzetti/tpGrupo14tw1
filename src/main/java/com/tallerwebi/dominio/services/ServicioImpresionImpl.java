package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.interfaces.ServicioImpresion;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ServicioImpresionImpl implements ServicioImpresion {

  @Value("${impresora.nombre:POS-58 11.3.0.1}")
  private String nombreImpresora;

  private static final Logger log = LoggerFactory.getLogger(ServicioImpresionImpl.class);

  @Override
  public void imprimirTicketVencimiento(
    Producto producto,
    ReglaVencimiento regla,
    OffsetDateTime fechaElaboracion,
    OffsetDateTime fechaVencimiento,
    OffsetDateTime fechaDescongelamiento
  ) {
    try {
      PrintService impresora = buscarImpresora(nombreImpresora);
      if (impresora == null) {
        throw new ImpresionException("No se encontró la impresora con nombre " + nombreImpresora);
      }

      byte[] comandos = construirComandosEscPos(
        producto,
        regla,
        fechaElaboracion,
        fechaVencimiento,
        fechaDescongelamiento
      );

      DocPrintJob trabajo = impresora.createPrintJob();
      Doc documento = new SimpleDoc(comandos, DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
      trabajo.print(documento, null);
    } catch (Exception e) {
      logger.error("No se pudo imprimir el ticket", e);
    }
  }

  private byte[] construirComandosEscPos(
    Producto producto,
    ReglaVencimiento regla,
    OffsetDateTime fechaElaboracion,
    OffsetDateTime fechaVencimiento,
    OffsetDateTime fechaDescongelamiento
  ) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Inicializar impresora
    buffer.write(ESC_INIT);

    // A. NOMBRE — centrado, tamaño doble
    buffer.write(CENTRADO);
    buffer.write(TEXTO_DOBLE);
    buffer.write((producto.getNombre().toUpperCase(Locale.ROOT) + "\n").getBytes(CHARSET));

    // B. UBICACIÓN — tamaño normal, negrita
    buffer.write(TEXTO_NORMAL);
    buffer.write(NEGRITA_ON);
    for (String linea : cortarTexto(regla.getUbicacion(), 30)) {
      buffer.write(("(" + linea.trim() + ")\n").getBytes(CHARSET));
    }
    buffer.write(NEGRITA_OFF);
    buffer.write("--------------------------------\n".getBytes());

    // C. TIEMPOS — alineado izquierda
    buffer.write(IZQUIERDA);
    buffer.write(("Retirado:   " + fechaElaboracion.format(formato) + "\n").getBytes(CHARSET));

    if (regla.getDescongelamientoMinutos() > 0) {
      buffer.write(
        ("Descongela: " + fechaDescongelamiento.format(formato) + "\n").getBytes(CHARSET)
      );
    }

    // D. VENCIMIENTO — negrita
    buffer.write(NEGRITA_ON);
    buffer.write(("Vencimiento:" + fechaVencimiento.format(formato) + "\n").getBytes(CHARSET));
    buffer.write(NEGRITA_OFF);

    // Cierre
    buffer.write(AVANCE_3_LINEAS);
    buffer.write(CORTE);

    return buffer.toByteArray();
  }

  private PrintService buscarImpresora(String nombre) {
    for (PrintService ps : PrintServiceLookup.lookupPrintServices(null, null)) {
      if (ps.getName().equalsIgnoreCase(nombre)) return ps;
    }
    return null;
  }

  private List<String> cortarTexto(String texto, int anchoMaximo) {
    List<String> lineas = new ArrayList<>();
    String[] palabras = texto.split(" ");
    StringBuilder lineaActual = new StringBuilder();

    for (String palabra : palabras) {
      if (lineaActual.length() + palabra.length() + 1 > anchoMaximo) {
        lineas.add(lineaActual.toString());
        lineaActual = new StringBuilder(palabra);
      } else {
        if (lineaActual.length() > 0) lineaActual.append(" ");
        lineaActual.append(palabra);
      }
    }
    if (lineaActual.length() > 0) {
      lineas.add(lineaActual.toString());
    }
    return lineas;
  }

  // Constantes ESC/POS
  private static final byte[] ESC_INIT = { 0x1B, 0x40 };
  private static final byte[] CENTRADO = { 0x1B, 0x61, 0x01 };
  private static final byte[] IZQUIERDA = { 0x1B, 0x61, 0x00 };
  private static final byte[] TEXTO_DOBLE = { 0x1D, 0x21, 0x11 };
  private static final byte[] TEXTO_NORMAL = { 0x1D, 0x21, 0x00 };
  private static final byte[] NEGRITA_ON = { 0x1B, 0x45, 0x01 };
  private static final byte[] NEGRITA_OFF = { 0x1B, 0x45, 0x00 };
  private static final byte[] AVANCE_3_LINEAS = { 0x1B, 0x64, 0x03 };
  private static final byte[] CORTE = { 0x1D, 0x56, 0x41, 0x00 };

  private final Charset CHARSET = Charset.forName("CP437");
}
