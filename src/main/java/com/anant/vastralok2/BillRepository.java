package com.anant.vastralok2;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByDate(LocalDate date);

    @Query("SELECT SUM(b.amount) FROM Bill b WHERE b.date = :date")
    Double getTotalAmountByDate(LocalDate date);

    List<Bill> findAllByOrderByDateDesc();

}