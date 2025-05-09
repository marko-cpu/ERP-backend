package com.app.erp.sales.service;


import com.app.erp.entity.Customer;
import com.app.erp.entity.invoice.Invoice;
import com.app.erp.entity.order.OrderProduct;
import com.app.erp.dto.order.ProductsSoldStatsDTO;
import com.app.erp.sales.repository.InvoiceRepository;
import com.itextpdf.kernel.color.DeviceRgb;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.border.SolidBorder;
import com.itextpdf.layout.element.*;

import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);

    @Autowired
    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public Page<Invoice> getAllInvoices(Pageable pageable) {
        return invoiceRepository.findAll(pageable);
    }

    public Optional<Invoice> getInvoiceById(Long id) {
        return invoiceRepository.findById(id);
    }

    public Invoice createInvoice(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }

    public Invoice updateInvoice(Long id, Invoice updatedInvoice) {
        Optional<Invoice> optionalInvoice = invoiceRepository.findById(id);
        if (optionalInvoice.isPresent()) {
            Invoice invoice = optionalInvoice.get();
            invoice.setAccounting(updatedInvoice.getAccounting());
            invoice.setTotalPrice(updatedInvoice.getTotalPrice());
            invoice.setPayDate(updatedInvoice.getPayDate());
            return invoiceRepository.save(invoice);
        }
        return null; // Or throw an exception
    }

    public void deleteInvoice(Long id) {
        invoiceRepository.deleteById(id);
    }

    public List<Invoice> findInvoicesByPayDate(LocalDate payDate) {
        return invoiceRepository.findByPayDate(payDate);
    }


    public ProductsSoldStatsDTO getProductsSoldStatistics() {
        List<Invoice> invoices = invoiceRepository.findAllWithOrderProducts();
        Map<String, Integer> monthlySales = new LinkedHashMap<>();

        // Inicialization of monthlySales
        LocalDate now = LocalDate.now();
        Year currentYear = Year.now();
        for (int i = 1; i <= 12; i++) {
            LocalDate date = currentYear.atMonth(i).atDay(1);
            String month = date.format(DateTimeFormatter.ofPattern("MMM"));
            monthlySales.put(month, 0);
        }

        for (Invoice invoice : invoices) {
            if (invoice.getPayDate() != null
                    && invoice.getAccounting() != null
                    && invoice.getAccounting().getOrder() != null
                    && invoice.getAccounting().getOrder().getProductList() != null) {

                String month = invoice.getPayDate().format(DateTimeFormatter.ofPattern("MMM"));
                int quantitySum = invoice.getAccounting().getOrder().getProductList().stream()
                        .mapToInt(OrderProduct::getQuantity)
                        .sum();

                monthlySales.merge(month, quantitySum, Integer::sum);
            }
        }

        // Sortiranje mjeseci po hronologiji
        List<String> sortedMonths = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");

        ProductsSoldStatsDTO dto = new ProductsSoldStatsDTO();
        dto.setMonths(sortedMonths);
        dto.setCounts(sortedMonths.stream()
                .map(month -> monthlySales.getOrDefault(month, 0))
                .collect(Collectors.toList()));

        return dto;
    }

    public byte[] generateInvoicePdf(Long id) {
        Invoice invoice = invoiceRepository.findInvoiceWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(baos));
            Document document = new Document(pdfDoc);
            document.setMargins(40, 40, 40, 40);

            Customer customer = invoice.getAccounting().getOrder().getCustomer();

            // Header
            Paragraph header = new Paragraph("INVOICE")
                    .setFontSize(26)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(new DeviceRgb(0, 102, 204))
                    .setMarginBottom(10);
            document.add(header);

            Paragraph companyInfo = new Paragraph()
                    .add(new Text("ERP Company\n").setBold())
                    .add("123 Business Street\nKragujevac, Serbia\n")
                    .add("Tel: +381 123 4567 | Email: info@company.com")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setFontColor(new DeviceRgb(100, 100, 100))
                    .setMarginBottom(30);
            document.add(companyInfo);

            // Seller & Buyer Info
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{45, 10, 45}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(30);

            infoTable.addCell(new Cell()
                    .add(new Paragraph("From:\nERP Company\n123 Business Street\nKragujevac, Serbia"))
                    .setBorder(Border.NO_BORDER)
                    .setFontSize(10));

            infoTable.addCell(new Cell().setBorder(Border.NO_BORDER));

            infoTable.addCell(new Cell()
                    .add(new Paragraph("Bill To:\n" +
                            customer.getFirstName() + "\n" +
                            customer.getAddress()))
                    .setBorder(Border.NO_BORDER)
                    .setFontSize(10));

            document.add(infoTable);

            // Invoice metadata
            Table invoiceDetails = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                    .setWidth(UnitValue.createPercentValue(50))
                    .setMarginBottom(20);

            invoiceDetails.addCell(createDetailCell("Invoice Number:", true));
            invoiceDetails.addCell(createDetailCell(invoice.getInvoiceNumber(), false));
            invoiceDetails.addCell(createDetailCell("Invoice Date:", true));
            invoiceDetails.addCell(createDetailCell(invoice.getPayDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), false));

            document.add(invoiceDetails);

            // Items Table
            Table itemTable = new Table(UnitValue.createPercentArray(new float[]{30, 10, 15, 15, 15, 20}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(20);

            String[] headers = {"Product Name", "Qty", "Measure Unit", "Unit Price", "PDV", "Total"};
            for (String headerText : headers) {
                itemTable.addHeaderCell(createCell(headerText, true));
            }

            double subtotal = 0;
            double totalPdv = 0;

            for (OrderProduct op : invoice.getAccounting().getOrder().getProductList()) {
                double itemTotal = op.getPricePerUnit() * op.getQuantity();

                itemTable.addCell(createCell(op.getProduct().getProductName(), false));
                itemTable.addCell(createCell(String.valueOf(op.getQuantity()), false));
                itemTable.addCell(createCell(op.getProduct().getMeasureUnit(), false));
                itemTable.addCell(createCell(formatCurrency(op.getPricePerUnit()), false));
                itemTable.addCell(createCell(formatCurrency(op.getPdv()), false));
                itemTable.addCell(createCell(formatCurrency(itemTotal), false));

                subtotal += itemTotal;
                totalPdv += op.getPdv();
            }

            document.add(itemTable);

            // Totals Table
            Table totalsTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                    .setWidth(UnitValue.createPercentValue(40))
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    .setMarginBottom(10);

            addTotalRow(totalsTable, "Subtotal:", subtotal);
            addTotalRow(totalsTable, "PDV:", totalPdv);
            addTotalRow(totalsTable, "Total:", invoice.getTotalPrice());

            document.add(totalsTable);

            // Footer
            Paragraph footer = new Paragraph("Thank you for your business!\nwww.company.com | Tel: +381 123 4567")
                    .setFontSize(9)
                    .setFontColor(new DeviceRgb(120, 120, 120))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(40);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

// --- Helper Methods ---

    private Cell createCell(String content, boolean isHeader) {
        Paragraph p = new Paragraph(content).setFontSize(10);
        Cell cell = new Cell().add(p);

        if (isHeader) {
            cell.setBackgroundColor(new DeviceRgb(220, 230, 245))
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(6)
                    .setBorderBottom(new SolidBorder(new DeviceRgb(150, 150, 150), 1.5f));
        } else {
            cell.setPadding(6)
                    .setBorderBottom(new SolidBorder(new DeviceRgb(200, 200, 200), 0.5f));
        }

        // Automatsko poravnanje za numeričke vrednosti
        boolean isNumericValue = content.startsWith("€") || content.matches("\\d+(\\.\\d+)?");
        if (isNumericValue) {
            p.setTextAlignment(TextAlignment.RIGHT);
        }

        return cell;
    }

    private Cell createDetailCell(String content, boolean isLabel) {
        Paragraph p = new Paragraph(content).setFontSize(10);
        Cell cell = new Cell().add(p)
                .setBorder(Border.NO_BORDER)
                .setPaddingTop(4)
                .setPaddingBottom(4);

        if (isLabel) {
            p.setBold().setTextAlignment(TextAlignment.RIGHT);
        } else {
            p.setTextAlignment(TextAlignment.LEFT);
        }

        return cell;
    }


    private void addTotalRow(Table table, String label, double value) {
        table.addCell(
                new Cell()
                        .add(new Paragraph(label).setFontSize(10))
                        .setBorder(Border.NO_BORDER)
                        .setTextAlignment(TextAlignment.RIGHT)
        );

        table.addCell(
                new Cell()
                        .add(new Paragraph(formatCurrency(value)).setFontSize(10).setBold())
                        .setBorder(Border.NO_BORDER)
                        .setTextAlignment(TextAlignment.RIGHT)
        );
    }


    private String formatCurrency(double amount) {
        return String.format("€%,.2f", amount);
    }

}
