package br.com.isiflix.fakeerp.repository;

import br.com.isiflix.fakeerp.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    /**
     * Recupera todos os pedidos filtrando pelo ano e mês de order_date_time.
     */
    @Query("""
            select o from OrderEntity o
            where extract(year from o.orderDateTime) = :year
              and extract(month from o.orderDateTime) = :month
            order by o.orderDateTime
            """)
    List<OrderEntity> findByYearAndMonth(@Param("year") int year, @Param("month") int month);
}
