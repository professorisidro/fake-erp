package br.com.isiflix.fakeerp.repository;

import br.com.isiflix.fakeerp.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<CompanyEntity, String> {
}
