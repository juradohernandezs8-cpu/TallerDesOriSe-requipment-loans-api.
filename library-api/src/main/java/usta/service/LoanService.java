package usta.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import usta.dto.LoanDTO;
import usta.model.Equipment;
import usta.model.EquipmentRepository;
import usta.model.Loan;
import usta.model.LoanRepository;
import usta.model.Student;
import usta.model.StudentRepository;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class LoanService {
    @Inject
    LoanRepository repository;

    @Inject
    EquipmentRepository equipmentRepository;

    @Inject
    StudentRepository studentRepository;

    // Lista todos
    public List<Loan> findAll() {
        return repository.listAllLoans();
    }

    // Busca por id
    public Loan findById(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Préstamo no encontrado"));
    }

    @Transactional
    // Registra un préstamo: valida existencia de equipo/estudiante y descuenta stock
    public Loan create(LoanDTO dto) {
        Equipment equipment = equipmentRepository.findByIdOptional(dto.equipmentId())
                .orElseThrow(() -> new NotFoundException("Equipo no encontrado"));
        Student student = studentRepository.findByIdOptional(dto.studentId())
                .orElseThrow(() -> new NotFoundException("Estudiante no encontrado"));

        if (equipment.getStock() == null || equipment.getStock() < dto.quantity()) {
            throw new BadRequestException("Sin stock disponible para el equipo: " + equipment.getName());
        }

        equipment.setStock(equipment.getStock() - dto.quantity());
        equipmentRepository.persist(equipment);

        Loan loan = new Loan(LocalDate.now(), dto.quantity(), equipment, student);
        repository.persist(loan);
        return loan;
    }

    @Transactional
    // Marca el préstamo como devuelto y repone el stock del equipo
    public Loan returnLoan(Long id) {
        Loan loan = findById(id);
        if (loan.isReturned()) {
            throw new BadRequestException("Este préstamo ya fue devuelto");
        }
        loan.setReturnDate(LocalDate.now());
        repository.persist(loan);

        Equipment equipment = loan.getEquipment();
        equipment.setStock(equipment.getStock() + loan.getQuantity());
        equipmentRepository.persist(equipment);

        return loan;
    }

    @Transactional
    // Elimina el préstamo (si estaba activo, repone el stock antes de borrar)
    public void delete(Long id) {
        Loan loan = findById(id);
        if (!loan.isReturned()) {
            Equipment equipment = loan.getEquipment();
            equipment.setStock(equipment.getStock() + loan.getQuantity());
            equipmentRepository.persist(equipment);
        }
        repository.delete(loan);
    }
}
