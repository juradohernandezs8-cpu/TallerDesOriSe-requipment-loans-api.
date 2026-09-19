package usta.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.*;

import java.time.LocalDate;

/**
 * MODEL - Representa el préstamo de una cantidad de un equipo a un estudiante.
 * Relación "Uno a Muchos": un equipo tiene muchos préstamos, un estudiante tiene muchos préstamos.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_date", nullable = false)
    private LocalDate loanDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    public Loan(LocalDate loanDate, Integer quantity, Equipment equipment, Student student) {
        this.loanDate = loanDate;
        this.quantity = quantity;
        this.equipment = equipment;
        this.student = student;
    }

    // Indica si el préstamo ya fue devuelto
    public boolean isReturned() {
        return returnDate != null;
    }

    @Override
    // Compara por id
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Loan other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    // Hash de la clase (usado por colecciones como HashSet)
    public int hashCode() {
        return getClass().hashCode();
    }
}
