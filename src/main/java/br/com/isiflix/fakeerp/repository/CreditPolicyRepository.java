package br.com.isiflix.fakeerp.repository;

import br.com.isiflix.fakeerp.entity.CreditPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreditPolicyRepository extends JpaRepository<CreditPolicyEntity, Long> {

    /**
     * Política vigente (mais recente) de um segmento.
     */
    Optional<CreditPolicyEntity> findFirstBySegmentIgnoreCaseOrderByUpdatedAtDesc(String segment);

    boolean existsByPolicyVersion(String policyVersion);
}
