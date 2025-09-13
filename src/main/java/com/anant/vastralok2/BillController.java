package com.anant.vastralok2;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/bill")
public class BillController {
    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generateBill(
            @RequestParam String customerName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam double amount,
            @RequestParam(required = false, defaultValue = "true") boolean sendSms) {

        Bill bill = billService.saveBill(customerName, phoneNumber, amount);
        String billDetails = billService.generateBillDetails(customerName, amount);

        if (sendSms && phoneNumber != null && !phoneNumber.isEmpty()) {
            try {
                billService.sendSmsNotification(phoneNumber, billDetails);
            } catch (Exception e) {
                return ResponseEntity.ok()
                        .header("Content-Type", "text/plain")
                        .body(billDetails + "\n\nNote: SMS could not be sent. Error: " + e.getMessage());
            }
        }

        return ResponseEntity.ok()
                .header("Content-Type", "text/plain")
                .body(billDetails);
    }

    @GetMapping("/view")
    public ModelAndView viewBills() {
        ModelAndView modelAndView = new ModelAndView("bills-view");
        modelAndView.addObject("yearlyGroupedBills", billService.getGroupedBills());
        return modelAndView;
    }

    @GetMapping("/view/{date}")
    public ModelAndView viewBillsByDate(@PathVariable LocalDate date) {
        ModelAndView modelAndView = new ModelAndView("bills-view");
        modelAndView.addObject("bills", billService.getBillsByDate(date));
        modelAndView.addObject("totalAmount", billService.getTotalAmountByDate(date));
        modelAndView.addObject("date", date);
        return modelAndView;
    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteBill(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long dailySequence) {
        try {
            BillId billId = new BillId();
            billId.setDate(date);
            billId.setDailySequence(dailySequence);
            billService.deleteBill(billId);
            return ResponseEntity.ok("Bill deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error deleting bill: " + e.getMessage());
        }
    }
}
