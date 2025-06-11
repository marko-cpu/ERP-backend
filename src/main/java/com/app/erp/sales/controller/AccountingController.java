package com.app.erp.sales.controller;


import com.app.erp.entity.accounting.Accounting;
import com.app.erp.sales.service.AccountingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.util.List;

@RestController
@RequestMapping("/api/accountings")
@PreAuthorize("hasAnyAuthority('ADMIN', 'ACCOUNTANT')")
public class AccountingController {

    private final AccountingService accountingService;

    @Autowired
    public AccountingController(AccountingService accountingService) {
        this.accountingService = accountingService;
    }


    @GetMapping
    public Page<Accounting> getAllAccountings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) Integer status) {

        return accountingService.getAllAccountings(page, size, status);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccounting(@PathVariable Long id) {
        accountingService.deleteAccounting(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/state-two")
    public List<Accounting> findAccountingsByStateTwo() {
        return accountingService.findAccountingsByStateTwo();
    }


    @PutMapping("/{id}")
    public ResponseEntity<Accounting> updateAccounting(
            @PathVariable Long id,
            @RequestBody Accounting updatedAccounting) {

        Accounting accounting = accountingService.updateAccounting(id, updatedAccounting);
        return ResponseEntity.ok(accounting);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Accounting> getAccountingById(@PathVariable Long id) {
        return ResponseEntity.ok(accountingService.getAccountingById(id));
    }

}
