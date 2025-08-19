package com.anant.vastralok2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Cacheable("bills")
@RequestMapping("/api/bill")
public class BillController {

    @Value("${fast2sms.api.key}")
    private String apiKey;

    private final BillRepository billRepository;

    public BillController(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generateBill(
            @RequestParam String customerName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam double amount) {

        // Only validate if phone number is provided
        if (phoneNumber != null && !phoneNumber.isEmpty() && !phoneNumber.matches("\\d{10}")) {
            return ResponseEntity.badRequest().body("Invalid phone number. Must be 10 digits or empty.");
        }

        // Save to database
        Bill bill = new Bill();
        bill.setCustomerName(customerName);
        bill.setPhoneNumber(phoneNumber);
        bill.setAmount(amount);
        bill.setDate(LocalDate.now());
        billRepository.save(bill);

        String billDetails = generateBillDetails(customerName, amount);

        // Only send SMS if phone number is provided
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            try {
                String message = "Thank you for shopping with us!\n" + billDetails;

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

            } catch (Exception e) {
                System.out.println("Error sending SMS: " + e.getMessage());
                return ResponseEntity.internalServerError().body("Bill generated but failed to send SMS: " + e.getMessage());
            }
        }

        return ResponseEntity.ok()
                .header("Content-Type", "text/plain")
                .body(billDetails);
    }

    @GetMapping("/view")
    public ModelAndView viewBills() {
        ModelAndView modelAndView = new ModelAndView("bills-view");
        List<Bill> allBills = billRepository.findAllByOrderByDateDesc();

        // Group bills by date with total amount
        Map<LocalDate, Double> dailyTotals = allBills.stream()
                .collect(Collectors.groupingBy(
                        Bill::getDate,
                        Collectors.summingDouble(Bill::getAmount)
                ));

        modelAndView.addObject("allBills", allBills);
        modelAndView.addObject("dailyTotals", dailyTotals);
        modelAndView.addObject("grandTotal", allBills.stream().mapToDouble(Bill::getAmount).sum());

        return modelAndView;
    }

    @GetMapping("/view/{date}")
    public ModelAndView viewBillsByDate(@PathVariable LocalDate date) {
        ModelAndView modelAndView = new ModelAndView("bills-view");
        List<Bill> billsForDate = billRepository.findByDate(date);
        Double totalAmount = billRepository.getTotalAmountByDate(date);

        modelAndView.addObject("bills", billsForDate);
        modelAndView.addObject("totalAmount", totalAmount != null ? totalAmount : 0.0);
        modelAndView.addObject("date", date);

        return modelAndView;
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<String> deleteBill(@PathVariable Long id) {
        try {
            billRepository.deleteById(id);
            return ResponseEntity.ok("Bill deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error deleting bill: " + e.getMessage());
        }
    }

    private String generateBillDetails(String customerName, double amount) {
        return "Bill for " + customerName +
                "\nTotal Price: =₹ " + String.format("%.2f", amount + (amount * 0.10)) +
                "\nDiscount:    =₹ " + String.format("%.2f", amount * 0.10) +
                "\nFinal Price: =₹ " + String.format("%.2f", amount) +
                "\n\nThank you for Visiting!\nAnant Vestralok, Maksi";
    }
}