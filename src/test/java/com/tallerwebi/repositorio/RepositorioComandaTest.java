package com.tallerwebi.repositorio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Comanda;
import com.tallerwebi.dominio.entity.DetallePedido;
import com.tallerwebi.dominio.entity.Pedido;
import com.tallerwebi.dominio.entity.ProductoFinal;
import com.tallerwebi.dominio.entity.enums.EstadoComanda;
import com.tallerwebi.dominio.interfaces.RepositorioComanda;
import com.tallerwebi.repositorio.config.HibernateInfraestructuraTestConfig;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import javax.transaction.Transactional;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
public class RepositorioComandaTest {

    @Autowired
    private SessionFactory sessionFactory;

    private RepositorioComanda repositorioComanda;

    @BeforeEach
    public void init() {
        repositorioComanda = new RepositorioComandaImpl(sessionFactory);
    }

    @Test
    @Transactional
    @Rollback
    public void queSePuedaGuardarYBuscarComanda() {
        Comanda comanda = new Comanda();
        comanda.setEstado(EstadoComanda.PENDIENTE);
        
        sessionFactory.getCurrentSession().save(comanda);
        
        Comanda obtenida = repositorioComanda.buscarPorId(comanda.getId());
        
        assertThat(obtenida, notNullValue());
        assertThat(obtenida.getEstado(), equalTo(EstadoComanda.PENDIENTE));
    }

    @Test
    @Transactional
    @Rollback
    public void queSePuedaListarComandasPendientes() {
        Pedido pedido = new Pedido();
        pedido.setHoraCobro(OffsetDateTime.now());
        sessionFactory.getCurrentSession().save(pedido);

        Comanda comanda = new Comanda();
        comanda.setEstado(EstadoComanda.PENDIENTE);
        comanda.setPedido(pedido);
        sessionFactory.getCurrentSession().save(comanda);

        List<Comanda> pendientes = repositorioComanda.listarPendientes();
        
        assertThat(pendientes.size(), is(1));
    }

    @Test
    @Transactional
    @Rollback
    public void queSePuedaListarPendientesPorCategoria() {
        Categoria cat = new Categoria();
        cat.setNombre("Cocina");
        sessionFactory.getCurrentSession().save(cat);
        
        ProductoFinal pf = new ProductoFinal();
        pf.setNombre("Papas");
        pf.setCategorias(new HashSet<>(Arrays.asList(cat)));
        sessionFactory.getCurrentSession().save(pf);
        
        Pedido pedido = new Pedido();
        pedido.setHoraCobro(OffsetDateTime.now());
        sessionFactory.getCurrentSession().save(pedido);
        
        DetallePedido detalle = new DetallePedido();
        detalle.setPedido(pedido);
        detalle.setProductoFinal(pf);
        sessionFactory.getCurrentSession().save(detalle);
        
        Comanda comanda = new Comanda();
        comanda.setEstado(EstadoComanda.PENDIENTE);
        comanda.setPedido(pedido);
        sessionFactory.getCurrentSession().save(comanda);
        
        List<Comanda> pendientes = repositorioComanda.listarPendientesPorCategoria(cat.getId());
        
        assertThat(pendientes.size(), is(1));
    }

    @Test
    @Transactional
    @Rollback
    public void queSePuedaActualizarComanda() {
        Comanda comanda = new Comanda();
        comanda.setEstado(EstadoComanda.PENDIENTE);
        sessionFactory.getCurrentSession().save(comanda);
        
        comanda.setEstado(EstadoComanda.COMPLETADA);
        repositorioComanda.actualizar(comanda);
        
        Comanda obtenida = repositorioComanda.buscarPorId(comanda.getId());
        assertThat(obtenida.getEstado(), equalTo(EstadoComanda.COMPLETADA));
    }
}
