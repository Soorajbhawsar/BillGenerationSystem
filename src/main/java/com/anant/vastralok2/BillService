package com.anant.vastralok2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BillService {
    @Value("${fast2sms.api.key}")
    private String apiKey;

    private final BillRepository billRepository;

    public BillService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    public Bill saveBill(String customerName, String phoneNumber, double amount) {
        validatePhoneNumber(phoneNumber);

        LocalDate today = LocalDate.now();
        Long nextSequence = billRepository.findMaxDailySequenceByDate(today)
                .orElse(0L) + 1L;

        BillId billId = new BillId(today, nextSequence);
        Bill bill = new Bill();
        bill.setId(billId);
        bill.setCustomerName(customerName);
        bill.setPhoneNumber(phoneNumber);
        bill.setAmount(amount);

        return billRepository.save(bill);
    }

    public void deleteBill(BillId billId) {
        billRepository.deleteById(billId);
    }

    public List<Bill> getBillsByDate(LocalDate date) {
        return billRepository.findByDate(date);
    }

    public Double getTotalAmountByDate(LocalDate date) {
        return billRepository.getTotalAmountByDate(date);
    }

    public List<Bill> getAllBillsOrderedByDate() {
        return billRepository.findAllByOrderByDateDesc();
    }

    public String generateBillDetails(String customerName, double amount) {
        double discount = amount * 0.10;
        double discountedAmount = amount - discount;
        double cgst = discountedAmount * 0.09;
        double sgst = discountedAmount * 0.09;
        double finalAmount = discountedAmount + cgst + sgst;

        return String.format(
                "Bill Details\n----------------\n" +
                        "Customer: %s\nBill Date: %s\n\n" +
                        "Original Price: ₹%.2f\nDiscount (10%%): -₹%.2f\n" +
                        "Subtotal: ₹%.2f\nCGST (9%%): +₹%.2f\n" +
                        "SGST (9%%): +₹%.2f\nFinal Amount: ₹%.2f\n\n" +
                        "Thank you for visiting!\nAnant Vastralok, Maksi",
                customerName, LocalDate.now(), amount, discount,
                discountedAmount, cgst, sgst, finalAmount
        );
    }

    public void sendSmsNotification(String phoneNumber, String message) throws Exception {
        String requestBody = "sender_id=FSTSMS" +
                "&message=" + URLEncoder.encode(message, "UTF-8") +
                "&language=english" +
                "&route=q" +
                "&numbers=" + phoneNumber;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.fast2sms.com/dev/bulkV2"))
                .header("authorization", apiKey)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        System.out.println("SMS API Response: " + response.body());
    }

    private void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.isEmpty() && !phoneNumber.matches("\\d{10}")) {
            throw new IllegalArgumentException("Invalid phone number. Must be 10 digits or empty.");
        }
    }

    public Map<Integer, YearlyData> getGroupedBills() {
        List<Bill> allBills = getAllBillsOrderedByDate();
        return groupBillsHierarchically(allBills);
    }

    private Map<Integer, YearlyData> groupBillsHierarchically(List<Bill> bills) {
        return bills.stream()
                .collect(Collectors.groupingBy(
                        bill -> bill.getDate().getYear(),
                        Collectors.collectingAndThen(Collectors.toList(), this::createYearlyData)
                ));
    }

    private YearlyData createYearlyData(List<Bill> yearlyBills) {
        YearlyData yearlyData = new YearlyData();
        yearlyData.total = yearlyBills.stream().mapToDouble(Bill::getAmount).sum();
        yearlyData.months = yearlyBills.stream()
                .collect(Collectors.groupingBy(
                        bill -> bill.getDate().getMonth(),
                        Collectors.collectingAndThen(Collectors.toList(), this::createMonthlyData)
                ));
        return yearlyData;
    }

    private MonthlyData createMonthlyData(List<Bill> monthlyBills) {
        MonthlyData monthlyData = new MonthlyData();
        monthlyData.total = monthlyBills.stream().mapToDouble(Bill::getAmount).sum();
        monthlyData.monthName = monthlyBills.get(0).getDate().getMonth()
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        monthlyData.days = monthlyBills.stream()
                .collect(Collectors.groupingBy(
                        Bill::getDate,
                        Collectors.collectingAndThen(Collectors.toList(), this::createDailyData)
                ));
        return monthlyData;
    }

    private DailyData createDailyData(List<Bill> dailyBills) {
        DailyData dailyData = new DailyData();
        dailyData.total = dailyBills.stream().mapToDouble(Bill::getAmount).sum();
        dailyData.bills = dailyBills;
        return dailyData;
    }

    public static class YearlyData {
        public double total;
        public Map<Month, MonthlyData> months;
    }

    public static class MonthlyData {
        public String monthName;
        public double total;
        public Map<LocalDate, DailyData> days;
    }

    public static class DailyData {
        public double total;
        public List<Bill> bills;
    }
}
