package com.anant.vastralok2;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class BillId implements java.io.Serializable {
    @Column(name = "bill_date", nullable = false)
    private LocalDate date;

    @Column(name = "daily_sequence", nullable = false)
    private Long dailySequence;

    public BillId() {}

    public BillId(LocalDate date, Long dailySequence) {
        this.date = date;
        this.dailySequence = dailySequence;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getDailySequence() {
        return dailySequence;
    }

    public void setDailySequence(Long dailySequence) {
        this.dailySequence = dailySequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BillId billId = (BillId) o;
        return date.equals(billId.date) && dailySequence.equals(billId.dailySequence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, dailySequence);
    }

    @Override
    public String toString() {
        return date + "-" + dailySequence;
    }
}
