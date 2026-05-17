package com.tallerwebi.presentacion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
public class TimerDTO {
    private Long id;
    private String nombre;
    private String groupId;
    private String fechaCreacion;
    private String fechaVencimiento;
    private String ubicacion;

}
