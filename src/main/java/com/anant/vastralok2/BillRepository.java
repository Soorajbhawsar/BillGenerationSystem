package com.anant.vastralok2;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, BillId> {

    @Query("SELECT b FROM Bill b WHERE b.id.date = ?1")
    List<Bill> findByDate(LocalDate date);

    @Query("SELECT SUM(b.amount) FROM Bill b WHERE b.id.date = ?1")
    Double getTotalAmountByDate(LocalDate date);

    @Query("SELECT b FROM Bill b ORDER BY b.id.date DESC, b.id.dailySequence DESC")
    List<Bill> findAllByOrderByDateDesc();

    @Query("SELECT MAX(b.id.dailySequence) FROM Bill b WHERE b.id.date = ?1")
    Optional<Long> findMaxDailySequenceByDate(LocalDate date);
}
