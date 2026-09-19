package usta.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// DTO inmutable para registrar un préstamo (la fecha de préstamo la asigna el servicio)
public record LoanDTO(
        @NotNull(message = "El id del equipo es obligatorio")
        Long equipmentId,

        @NotNull(message = "El id del estudiante es obligatorio")
        Long studentId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a 0")
        Integer quantity
) {}
