package com.tallerwebi.dominio;

import javax.persistence.*;

@Entity
public class ReglaVencimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_producto")
    private Producto producto;

    private String ubicacion;
    private Integer duracionMinutos;
    private Boolean tieneDescongelamiento;
    private Integer descongelamientoMinutos;

}