package com.anant.vastralok2;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "bills")
public class Bill {
    @EmbeddedId
    private BillId id;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = true)
    private String phoneNumber;

    @Column(nullable = false)
    private double amount;

    public LocalDate getDate() {
        return id != null ? id.getDate() : null;
    }

    public BillId getId() {
        return id;
    }

    public void setId(BillId id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
