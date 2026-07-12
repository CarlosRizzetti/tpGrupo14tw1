package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.ConsumoLote;
import com.tallerwebi.dominio.entity.Lote;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.enums.EstadoLote;
import com.tallerwebi.dominio.excepcion.SinStockSuficienteException;
import com.tallerwebi.dominio.interfaces.RepositorioConsumoLote;
import com.tallerwebi.dominio.interfaces.RepositorioLote;
import com.tallerwebi.dominio.interfaces.RepositorioProducto;
import com.tallerwebi.dominio.interfaces.ServicioLote;
import com.tallerwebi.presentacion.dto.NotificacionVencimientoDto;
import com.tallerwebi.presentacion.dto.StockProductoDTO;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioLote")
@Transactional
public class ServicioLoteImpl implements ServicioLote {

  private static final int URGENCIA_ALTA_DIAS = 3;
  private static final int URGENCIA_MEDIA_DIAS = 7;
  private static final int URGENCIA_BAJA_DIAS = 10;

  private final RepositorioLote repositorioLote;
  private final RepositorioConsumoLote repositorioConsumoLote;
  private final RepositorioProducto repositorioProducto;

  @Autowired
  public ServicioLoteImpl(
    RepositorioLote repositorioLote,
    RepositorioConsumoLote repositorioConsumoLote,
    RepositorioProducto repositorioProducto
  ) {
    this.repositorioLote = repositorioLote;
    this.repositorioConsumoLote = repositorioConsumoLote;
    this.repositorioProducto = repositorioProducto;
  }

  @Override
  public Lote registrarLote(Lote lote) {
    if (lote.getCantidadInicial() == null || lote.getCantidadInicial() <= 0) {
      throw new IllegalArgumentException("La cantidad inicial del lote debe ser mayor a 0");
    }
    lote.setCantidadDisponible(lote.getCantidadInicial());
    lote.setEstado(EstadoLote.DISPONIBLE);
    repositorioLote.guardar(lote);

    reevaluarFifo(lote.getProducto());
    return lote;
  }

  @Override
  public List<ConsumoLote> consumirCantidad(
    Producto producto,
    Integer cantidadNecesaria,
    Timer timer
  ) {
    validarCantidadPositiva(cantidadNecesaria);

    List<Lote> disponibles = repositorioLote.listarConsumiblesDeProducto(producto.getId());
    ResultadoConsumo resultado = consumirDeLotes(disponibles, cantidadNecesaria, timer);

    validarQueSeCubrioTodoElStock(resultado.restante, producto);

    reevaluarFifo(producto);
    return resultado.consumos;
  }

  @Override
  public Integer stockDisponibleDe(Producto producto) {
    return repositorioLote
      .listarConsumiblesDeProducto(producto.getId())
      .stream()
      .mapToInt(Lote::getCantidadDisponible)
      .sum();
  }

  @Override
  public List<Lote> obtenerTodosLosLotes() {
    return repositorioLote.listarTodos();
  }

  @Override
  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  public List<StockProductoDTO> obtenerStockAgrupado() {
    Map<Producto, List<Lote>> lotesPorProducto = repositorioLote
      .listarTodos()
      .stream()
      .collect(Collectors.groupingBy(Lote::getProducto));

    List<StockProductoDTO> resultado = new ArrayList<>();
    for (Producto producto : repositorioProducto.obtenerTodos()) {
      List<Lote> lotes = lotesPorProducto.getOrDefault(producto, new ArrayList<>());
      resultado.add(construirStockDeProducto(producto, lotes));
    }
    return resultado;
  }

  private StockProductoDTO construirStockDeProducto(Producto producto, List<Lote> lotes) {
    Lote enUso = lotes
      .stream()
      .filter(l -> l.getEstado() == EstadoLote.EN_USO)
      .findFirst()
      .orElse(null);

    List<Lote> disponibles = lotes
      .stream()
      .filter(l -> l.getEstado() == EstadoLote.DISPONIBLE)
      .sorted(Comparator.comparing(Lote::getFechaDeVencimiento))
      .collect(Collectors.toList());

    int stockTotal = lotes
      .stream()
      .filter(l -> l.getEstado() == EstadoLote.DISPONIBLE || l.getEstado() == EstadoLote.EN_USO)
      .mapToInt(Lote::getCantidadDisponible)
      .sum();

    return new StockProductoDTO(producto.getNombre(), stockTotal, enUso, disponibles);
  }

  @Override
  public List<NotificacionVencimientoDto> obtenerNotificacionesVencimiento() {
    List<NotificacionVencimientoDto> notificaciones = new ArrayList<>();

    for (Lote lote : repositorioLote.listarTodos()) {
      agregarNotificacionSiCorresponde(lote, notificaciones);
    }

    notificaciones.sort(Comparator.comparingLong(NotificacionVencimientoDto::getDiasRestantes));
    return notificaciones;
  }

  private void agregarNotificacionSiCorresponde(
    Lote lote,
    List<NotificacionVencimientoDto> notificaciones
  ) {
    if (!esCandidatoANotificar(lote)) {
      return;
    }
    long dias = ChronoUnit.DAYS.between(
      LocalDate.now(),
      lote.getFechaDeVencimiento().toLocalDate()
    );
    if (dias > URGENCIA_BAJA_DIAS) {
      return;
    }
    notificaciones.add(new NotificacionVencimientoDto(lote, dias, calcularUrgencia(dias)));
  }

  private boolean esCandidatoANotificar(Lote lote) {
    boolean tieneStock = lote.getCantidadDisponible() != null && lote.getCantidadDisponible() > 0;
    boolean estadoUsable =
      lote.getEstado() == EstadoLote.DISPONIBLE || lote.getEstado() == EstadoLote.EN_USO;
    return tieneStock && estadoUsable && lote.getFechaDeVencimiento() != null;
  }

  private String calcularUrgencia(long dias) {
    if (dias <= URGENCIA_ALTA_DIAS) return "ALTA";
    if (dias <= URGENCIA_MEDIA_DIAS) return "MEDIA";
    return "BAJA";
  }

  private void validarCantidadPositiva(Integer cantidad) {
    if (cantidad == null || cantidad <= 0) {
      throw new IllegalArgumentException("La cantidad a consumir debe ser mayor a 0");
    }
  }

  private ResultadoConsumo consumirDeLotes(
    List<Lote> disponibles,
    int cantidadNecesaria,
    Timer timer
  ) {
    List<ConsumoLote> consumos = new ArrayList<>();
    int restante = cantidadNecesaria;

    for (Lote lote : disponibles) {
      if (restante <= 0) {
        break;
      }

      int aConsumir = calcularCantidadAConsumir(lote, restante);
      if (aConsumir <= 0) {
        continue;
      }

      descontarDeLote(lote, aConsumir);
      consumos.add(registrarConsumo(lote, timer, aConsumir));
      restante -= aConsumir;
    }

    return new ResultadoConsumo(consumos, restante);
  }

  private int calcularCantidadAConsumir(Lote lote, int restante) {
    return Math.min(lote.getCantidadDisponible(), restante);
  }

  private void descontarDeLote(Lote lote, int cantidad) {
    lote.setCantidadDisponible(lote.getCantidadDisponible() - cantidad);
    if (lote.getCantidadDisponible() == 0) {
      lote.setEstado(EstadoLote.CONSUMIDO);
    }
    repositorioLote.actualizar(lote);
  }

  private ConsumoLote registrarConsumo(Lote lote, Timer timer, int cantidad) {
    ConsumoLote consumo = new ConsumoLote();
    consumo.setLote(lote);
    consumo.setTimer(timer);
    consumo.setCantidadConsumida(cantidad);
    repositorioConsumoLote.guardar(consumo);
    return consumo;
  }

  private void validarQueSeCubrioTodoElStock(int restante, Producto producto) {
    if (restante > 0) {
      throw new SinStockSuficienteException(
        "No hay stock suficiente en los lotes de " +
        producto.getNombre() +
        ". Faltan " +
        restante +
        " unidades."
      );
    }
  }

  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  private void reevaluarFifo(Producto producto) {
    List<Lote> consumibles = repositorioLote.listarConsumiblesDeProducto(producto.getId());
    if (consumibles.isEmpty()) {
      return;
    }

    Lote candidatoAUso = consumibles.get(0); // ya viene ordenado por fechaDeVencimiento asc

    for (Lote lote : consumibles) {
      boolean esElCandidato = lote.getId().equals(candidatoAUso.getId());
      if (esElCandidato && lote.getEstado() != EstadoLote.EN_USO) {
        lote.setEstado(EstadoLote.EN_USO);
        repositorioLote.actualizar(lote);
      } else if (!esElCandidato && lote.getEstado() == EstadoLote.EN_USO) {
        lote.setEstado(EstadoLote.DISPONIBLE);
        repositorioLote.actualizar(lote);
      }
    }
  }

  private static final class ResultadoConsumo {

    private final List<ConsumoLote> consumos;
    private final int restante;

    private ResultadoConsumo(List<ConsumoLote> consumos, int restante) {
      this.consumos = consumos;
      this.restante = restante;
    }
  }
}
