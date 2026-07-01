package br.com.creditengine.repositories;

import br.com.creditengine.entities.Receivable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceivableRepository extends JpaRepository<Receivable, Long> {
}
