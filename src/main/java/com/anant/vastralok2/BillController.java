package com.anant.vastralok2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
@Cacheable("bills")
@RequestMapping("/api/bill")
public class BillController {

    @Value("${fast2sms.api.key}") // Get from application.properties
    private String apiKey;

    @PostMapping("/generate")
    public ResponseEntity<String> generateBill(
            @RequestParam String customerName,
            @RequestParam String phoneNumber,
            @RequestParam double amount) {

        String billDetails = generateBillDetails(customerName, amount);

        try {
            String message = "Thank you for shopping with us!\n" + billDetails;

            // Proper Fast2SMS API request
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
        }

        return ResponseEntity.ok(billDetails);
    }

    private String generateBillDetails(String customerName, double amount) {
        return "Bill for " + customerName +
                "\nTotal Price: =₹ " + String.format("%.2f", amount + (amount * 0.10)) +
                "\nDiscount:    =₹ " + String.format("%.2f", amount * 0.10) +
                "\nFinal Price: =₹ " + String.format("%.2f", amount) +
                "\n\nThank you for Visiting!\nAnant Vestralok, Maksi";
    }
}