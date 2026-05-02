package com.tallerwebi.dominio;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private Boolean esta_activo;
    @ManyToMany
    @JoinTable(name = "productos_categoria", joinColumns = @JoinColumn(name = "id_producto"), inverseJoinColumns = @JoinColumn(name = "id_categoria"))
    private List<Categoria> categorias = new ArrayList<>();


    public Boolean getEsta_activo() {
        return esta_activo;
    }

    public void setEsta_activo(Boolean esta_activo) {
        this.esta_activo = esta_activo;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}