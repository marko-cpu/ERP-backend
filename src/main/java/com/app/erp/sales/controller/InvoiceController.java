package com.app.erp.sales.controller;


import com.app.erp.entity.invoice.Invoice;
import com.app.erp.dto.order.ProductsSoldStatsDTO;
import com.app.erp.sales.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/invoices")
@PreAuthorize("hasAnyAuthority('ADMIN', 'ACCOUNTANT', 'SALES_MANAGER')")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @Autowired
    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping("getAllInvoice")
    public Page<Invoice> getAllInvoices(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return invoiceService.getAllInvoices(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Long id) {
        Optional<Invoice> invoice = invoiceService.getInvoiceById(id);
        return invoice.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Invoice> updateInvoice(@PathVariable Long id, @RequestBody Invoice updatedInvoice) {
        Invoice invoice = invoiceService.updateInvoice(id, updatedInvoice);
        return invoice != null ? ResponseEntity.ok(invoice) : ResponseEntity.notFound().build();
    }


    @GetMapping("/pay-date")
    public List<Invoice> findInvoicesByPayDate(@RequestParam("payDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate payDate) {
        return invoiceService.findInvoicesByPayDate(payDate);
    }

    @GetMapping("/products-sold-stats")
    public ResponseEntity<ProductsSoldStatsDTO> getProductsSoldStats() {
        return ResponseEntity.ok(invoiceService.getProductsSoldStatistics());
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generateInvoicePdf(@PathVariable Long id) {
        byte[] pdfBytes = invoiceService.generateInvoicePdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
