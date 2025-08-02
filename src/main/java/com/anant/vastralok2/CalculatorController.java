package com.anant.vastralok2;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CalculatorController {

    @PostMapping("/calculate")
    public ResponseEntity<String> calculate(
            @RequestParam("number") double number) {

        double result = (number / 2) * 0.9;
        return ResponseEntity.ok(String.format("Result: %.2f", result));
    }
}