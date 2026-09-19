package usta.model;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class LoanRepository implements PanacheRepository<Loan> {
    // Lista todos
    public List<Loan> listAllLoans() {
        return listAll();
    }

    // Busca por id
    public Optional<Loan> findByIdOptional(Long id) {
        return find("id", id).firstResultOptional();
    }

    // Guarda
    public Loan save(Loan loan) {
        persist(loan);
        return loan;
    }

    // Elimina por id
    public boolean delete(Long id) {
        return deleteById(id);
    }
}
