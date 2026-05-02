package com.tallerwebi.dominio;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
public class TimerActivo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String groupId;

    private OffsetDateTime fechaCreacion = OffsetDateTime.now();
    private OffsetDateTime fechaVencimiento;
    private Boolean estaActivo;
    private String estado;

    @ManyToOne
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "id_regla_vencimiento")
    private ReglaVencimiento reglaVencimiento;

    @PrePersist
    public void setGroupId(){
        if(this.groupId == null || this.groupId.isEmpty()){
            this.groupId = UUID.randomUUID().toString();
        }
    }
}