package com.app.erp.sales.service;


import com.app.erp.entity.Customer;
import com.app.erp.entity.Invoice;
import com.app.erp.entity.OrderProduct;
import com.app.erp.entity.ProductsSoldStatsDTO;
import com.app.erp.sales.repository.InvoiceRepository;
import com.itextpdf.io.image.ImageDataFactory;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
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

        // Inicijalizacija svih mjeseci za tekuću godinu
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

            // Add logo
//            try {
//                ClassPathResource logoResource = new ClassPathResource("static/images/logo.png");
//                Image logo = new Image(ImageDataFactory.create(logoResource.getURL()))
//                        .setWidth(100)
//                        .setHorizontalAlignment(HorizontalAlignment.LEFT);
//                document.add(logo);
//            } catch (MalformedURLException e) {
//                logger.warn("Company logo not found or cannot be loaded: {}", e.getMessage());
//            }

            // Header Section
            Paragraph header = new Paragraph()
                    .add(new Text("INVOICE\n").setFontSize(24).setBold().setFontColor(new DeviceRgb(0, 102, 204)))
                    .add(new Text("Your Company Name\n").setFontSize(12).setBold())
                    .add(new Text("123 Business Street, City, Country\n"))
                    .add(new Text("VAT: XX123456789 | Tel: +381 123 4567\n"))
                    .add(new Text("Email: info@company.com | Web: www.company.com"))
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(20);
            document.add(header);

            // Seller & Buyer Info Table
            Customer customer = invoice.getAccounting().getOrder().getCustomer();
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{45, 10, 45}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(30);

            // Seller Info
            Cell sellerCell = new Cell()
                    .add(new Paragraph("From:\n" +
                            "Your Company Name\n" +
                            "123 Business Street\n" +
                            "City, Country\n" +
                            "VAT: XX123456789"))
                    .setBorder(Border.NO_BORDER);
            infoTable.addCell(sellerCell);

            infoTable.addCell(new Cell().setBorder(Border.NO_BORDER)); // Empty spacer cell

            // Buyer Info
            Cell buyerCell = new Cell()
                    .add(new Paragraph("Bill To:\n" +
                            customer.getFirstName() + "\n" +
                            customer.getAddress() + "\n" +
                            "VAT: " + customer.getId()))
                    .setBorder(Border.NO_BORDER);
            infoTable.addCell(buyerCell);

            document.add(infoTable);

            // Invoice Details Table
            Table invoiceDetails = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(20);

            invoiceDetails.addCell(createDetailCell("Invoice Number:", true));
            invoiceDetails.addCell(createDetailCell(invoice.getInvoiceNumber(), false));
            invoiceDetails.addCell(createDetailCell("Invoice Date:", true));
            invoiceDetails.addCell(createDetailCell(invoice.getPayDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), false));
            invoiceDetails.addCell(createDetailCell("Due Date:", true));
            invoiceDetails.addCell(createDetailCell(invoice.getPayDate().plusDays(30).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), false));

            document.add(invoiceDetails);

            // Items Table
            Table table = new Table(UnitValue.createPercentArray(new float[]{35, 10, 15, 10, 15, 15}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(30);

            // Table Header
            String[] headers = {"Description", "Qty", "Unit Price", "VAT %", "VAT Amount", "Total"};
            for (String headerText : headers) {
                table.addHeaderCell(createCell(headerText, true));
            }

            double subtotal = 0;
            double totalVat = 0;

            // Table Rows
            for (OrderProduct op : invoice.getAccounting().getOrder().getProductList()) {
                double itemTotal = op.getPricePerUnit() * op.getQuantity();
                double itemVat = op.getPdv();

                table.addCell(createCell(op.getProduct().getProductName(), false));
                table.addCell(createCell(String.valueOf(op.getQuantity()), false));
                table.addCell(createCell(formatCurrency(op.getPricePerUnit()), false));
                table.addCell(createCell(String.format("%.0f%%", op.getPdv()), false));
                table.addCell(createCell(formatCurrency(itemVat), false));
                table.addCell(createCell(formatCurrency(op.getTotalPrice()), false));

                subtotal += itemTotal;
                totalVat += itemVat;
            }

            document.add(table);

            // Totals Table
            Table totalsTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                    .setWidth(UnitValue.createPercentValue(50))
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT);

            addTotalRow(totalsTable, "Subtotal:", subtotal);
            addTotalRow(totalsTable, String.format("VAT (%d%%):", (int)invoice.getAccounting().getOrder().getProductList().get(0).getPdv()), totalVat);
            addTotalRow(totalsTable, "Total Due:", invoice.getTotalPrice());

            document.add(totalsTable);

            // Payment Instructions
            Paragraph paymentInstructions = new Paragraph()
                    .add(new Text("\nPayment Information:\n").setBold())
                    .add(new Text("Bank: Example Bank\n"))
                    .add(new Text("Account: XX00 1234 5678 9012 3456 7890\n"))
                    .add(new Text("SWIFT/BIC: EXMPLXXX\n\n"))
                    .add(new Text("Please make payment within 30 days of invoice date.").setItalic())
                    .setFontColor(new DeviceRgb(100, 100, 100))
                    .setMarginTop(20);
            document.add(paymentInstructions);

            // Footer
            Paragraph footer = new Paragraph()
                    .add(new Text("Thank you for your business!\n"))
                    .add(new Text("www.company.com | Tel: +381 123 4567 | Email: info@company.com"))
                    .setFontSize(10)
                    .setFontColor(new DeviceRgb(150, 150, 150))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    // Helper methods
    private Cell createCell(String content, boolean isHeader) {
        Paragraph p = new Paragraph(content);
        Cell cell = new Cell().add(p);

        if (isHeader) {
            cell.setBackgroundColor(new DeviceRgb(240, 240, 240))
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8)
                    .setBorderBottom(new SolidBorder(1.5f));
        } else {
            cell.setPadding(8)
                    .setBorderBottom(new SolidBorder(0.5f));
        }

        if (content.startsWith("€") || content.matches("\\d+(\\.\\d+)?")) {
            p.setTextAlignment(TextAlignment.RIGHT);
        }

        return cell;
    }

    private Cell createDetailCell(String content, boolean isLabel) {
        Paragraph p = new Paragraph(content);
        Cell cell = new Cell().add(p)
                .setBorder(Border.NO_BORDER)
                .setPadding(2);

        if (isLabel) {
            p.setBold().setTextAlignment(TextAlignment.RIGHT);
        }
        return cell;
    }

    private void addTotalRow(Table table, String label, double value) {
        table.addCell(new Cell()
                .add(new Paragraph(label))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT));

        table.addCell(new Cell()
                .add(new Paragraph(formatCurrency(value)))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT));
    }

    private String formatCurrency(double amount) {
        return String.format("€%,.2f", amount);
    }
}
