package br.com.isiflix.fakeerp.repository;

import br.com.isiflix.fakeerp.entity.CreditDecisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreditDecisionRepository extends JpaRepository<CreditDecisionEntity, Long> {

    /**
     * Revisão mais recente da decisão de um CNPJ em um período.
     */
    Optional<CreditDecisionEntity> findFirstByCnpjAndReferenceYearAndReferenceMonthOrderByRevisionDesc(
            String cnpj, int referenceYear, int referenceMonth);

    boolean existsBySupersedesId(Long supersedesId);
}
