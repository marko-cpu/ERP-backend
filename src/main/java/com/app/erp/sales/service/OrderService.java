package com.app.erp.sales.service;


import com.app.erp.audit.AuditService;
import com.app.erp.entity.*;
import com.app.erp.entity.accounting.Accounting;
import com.app.erp.entity.invoice.Invoice;
import com.app.erp.entity.order.Order;
import com.app.erp.entity.order.OrderProduct;
import com.app.erp.dto.order.OrderRequest;
import com.app.erp.entity.product.Product;
import com.app.erp.entity.user.User;
import com.app.erp.entity.warehouse.ArticleWarehouse;
import com.app.erp.goods.repository.ArticleWarehouseRepository;
import com.app.erp.goods.repository.ProductRepository;
import com.app.erp.goods.repository.ReservationRepository;
import com.app.erp.messaging.ReservationCancellationMessage;
import com.app.erp.messaging.ReservationMessage;
import com.app.erp.messaging.SoldProductMessage;
import com.app.erp.sales.repository.*;
import com.app.erp.user.repository.UserRepository;
import com.app.erp.user.service.NotificationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.app.erp.config.RabbitMQConfig.ORDERS_TOPIC_EXCHANGE_NAME;

@Service
public class OrderService {

    private final RabbitTemplate rabbitTemplate;
    private final OrderProductRepository orderProductRepository;
    private final OrderRepository orderRepository;
    private final ArticleWarehouseRepository articleWarehouseRepository;
    private final AccountingRepository accountingRepository;
    private final ReservationRepository reservationRepository;
    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final OrderProductService orderProductService;

    @PersistenceContext
    private EntityManager entityManager;

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    public OrderService(
            RabbitTemplate rabbitTemplate,
            OrderProductRepository orderProductRepository,
            OrderRepository orderRepository,
            ArticleWarehouseRepository articleWarehouseRepository,
            AccountingRepository accountingRepository,
            ReservationRepository reservationRepository,
            InvoiceRepository invoiceRepository,
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            NotificationService notificationService,
            UserRepository userRepository, AuditService auditService,
            OrderProductService orderProductService) {
        this.rabbitTemplate = rabbitTemplate;
        this.orderProductRepository = orderProductRepository;
        this.orderRepository = orderRepository;
        this.articleWarehouseRepository = articleWarehouseRepository;
        this.accountingRepository = accountingRepository;
        this.reservationRepository = reservationRepository;
        this.invoiceRepository = invoiceRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.orderProductService = orderProductService;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public List<Customer> searchCustomers(String query) {
        return customerRepository.searchCustomers(query.toLowerCase());
    }

    public Customer checkCustomerExists(String email) {
        return customerRepository.findByEmail(email).orElse(null);
    }

    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }


    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public long getOrderCount() {
        return orderRepository.count();
    }


    @Transactional
    public void createOrder(OrderRequest orderRequest) {
        if (orderRequest == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
//        User user = order.getUser();
        User user = userRepository.findById(orderRequest.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user == null) {
            System.out.println(user);
            throw new IllegalArgumentException("Order must have a user");
        }

        Customer customer;
        if (orderRequest.getCustomer().getId() != null) {
            // Existing customer
            customer = customerRepository.findById(orderRequest.getCustomer().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        } else {
            // New customer check email
            if (orderRequest.getCustomer().getEmail() == null) {
                throw new IllegalArgumentException("Email is required for new customers");
            }

            Optional<Customer> existingCustomer = customerRepository.findByEmail(
                    orderRequest.getCustomer().getEmail()
            );

            if (existingCustomer.isPresent()) {
                throw new IllegalArgumentException("Customer with this email already exists");
            }

            // Create new customer
            customer = new Customer(
                    orderRequest.getCustomer().getFirstName(),
                    orderRequest.getCustomer().getLastName(),
                    orderRequest.getCustomer().getAddress(),
                    orderRequest.getCustomer().getCity(),
                    orderRequest.getCustomer().getPostalCode(),
                    orderRequest.getCustomer().getEmail(),
                    orderRequest.getCustomer().getPhone()
            );
            customer = customerRepository.save(customer);
        }
        Order order = new Order();
        order.setUser(user);
        order.setCustomer(customer);
        order.setProductList(orderRequest.getProducts());

        // System.out.println("Creating order for customer: " + order.getCustomerName());

        if (order.getProductList() == null || order.getProductList().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one product");
        }

        if (!isStockAvailable(order.getProductList())) {
            throw new RuntimeException("Not enough stock available for the products in the order");
        }

        this.orderRepository.save(order);


        double totalPrice = 0.0;
        LocalDate dateOfPayment = LocalDate.now().plusDays(5);
        List<OrderProduct> productList = order.getProductList();

        for (int i = 0; i < productList.size(); i++) {
            OrderProduct orderProduct = productList.get(i);
            if (orderProduct.getProduct() == null) {
                throw new IllegalArgumentException("Product in OrderProduct cannot be null");
            }

            int count = 0;
            double purchasePrice = 0.0;

            for (ArticleWarehouse aw : articleWarehouseRepository.findStateOfWarehousesForProductId(orderProduct.getProduct().getId())) {
                ++count;
                purchasePrice += aw.getPurchasePrice();
            }

            if (count > 0) {
                purchasePrice /= count;
            } else {
                throw new RuntimeException("No warehouses found for product ID: " + orderProduct.getProduct().getId());
            }

            Long productId = orderProduct.getProduct().getId();
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found for ID: " + productId));


            orderProduct.setPricePerUnit(product.getPrice());
            orderProduct.setTotalPrice((orderProduct.getPricePerUnit() + (orderProduct.getPdv() * orderProduct.getPricePerUnit())) * orderProduct.getQuantity());
            orderProduct.setPdvRate(orderProduct.getPdvRate());
            orderProduct.setPdv(orderProduct.calculatePdv());
            orderProduct.setOrder(order);
            this.orderProductRepository.save(orderProduct);
            totalPrice += orderProduct.getTotalPrice();
        }



        // Notification for new order
        notificationService.createAndSendNotification(
                "ORDER_CREATED",
                "New order #" + order.getId() + " created",
                List.of("ADMIN", "ACCOUNTANT", "INVENTORY_MANAGER")
        );

        Accounting tmpAccounting = new Accounting(order, dateOfPayment, totalPrice);
        accountingRepository.save(tmpAccounting);

        //Notification for new accounting
        notificationService.createAndSendNotification(
                "ACCOUNTING_CREATED",
                "New accounting #" + tmpAccounting.getId() + " created",
                List.of("ADMIN", "ACCOUNTANT")
        );


        ReservationMessage reservationMessage = new ReservationMessage(productList, tmpAccounting);
        rabbitTemplate.convertAndSend(ORDERS_TOPIC_EXCHANGE_NAME, "reservation.queue", reservationMessage);


        Map<String, Object> orderDetails = new HashMap<>();
        orderDetails.put("customerId", customer.getId());
        orderDetails.put("customerName", customer.getFirstName() + " " + customer.getLastName());
        orderDetails.put("userId", user.getId());
        orderDetails.put("userEmail", user.getEmail());
        orderDetails.put("totalProducts", order.getProductList().size());
        orderDetails.put("totalAmount", totalPrice);

        List<Map<String, Object>> productsDetails = order.getProductList().stream()
                .map(op -> {
                    Map<String, Object> p = new HashMap<>();
                    p.put("productId", op.getProduct().getId());
                    p.put("productName", op.getProduct().getProductName());
                    p.put("quantity", op.getQuantity());
                    p.put("pricePerUnit", op.getPricePerUnit());
                    return p;
                })
                .collect(Collectors.toList());

        orderDetails.put("products", productsDetails);

        auditService.logEvent("ORDER_CREATE", "ORDER", order.getId(), orderDetails);



        //    System.out.println("Order created successfully with total price: " + totalPrice);
    }

    private boolean isStockAvailable(List<OrderProduct> productList) {
        for (OrderProduct orderProduct : productList) {
            int totalStock = 0;
            List<ArticleWarehouse> articleWarehouses = articleWarehouseRepository.findStateOfWarehousesForProductId(orderProduct.getProduct().getId());
            for (ArticleWarehouse aw : articleWarehouses) {
                totalStock += aw.getQuantity();
            }

            // Get total reserved quantity for this product
            int reservedQty = reservationRepository.findTotalReservedQuantityByProductId(orderProduct.getProduct().getId())
                    .orElse(0);

            int availableStock = totalStock - reservedQty;

            if (orderProduct.getQuantity() > availableStock) {
                return false;
            }
        }
        return true;
    }


    public Invoice addInvoice(long accountingId, double totalPrice) throws Exception {
        Optional<Accounting> accountingOptional = accountingRepository.findById(accountingId);
        try {
            Accounting accounting = accountingOptional.orElseThrow(() ->
                    new Exception("Accounting id does not exist!"));

            if (accounting.getState() == 1) {
                throw new Exception("Order is already paid!");
            }
        /*    if(accounting.getState() == 2){
                accountingRepository.deleteById(accountingId);
                throw new Exception("Order is already cancelled!");
            }*/
            if (accounting.getTotalPrice() > totalPrice) {
                throw new Exception("Not enough money!");
            }

            accounting.setState((short) 1);
            accountingRepository.save(accounting);

            LocalDate payDate = LocalDate.now();
            Invoice invoice = new Invoice(accounting, totalPrice, payDate);
            invoiceRepository.save(invoice);

            Order order = accounting.getOrder();
            Map<String, Object> invoiceDetails = new HashMap<>();
            invoiceDetails.put("orderId", order.getId());
            invoiceDetails.put("customerId", order.getCustomer().getId());
            invoiceDetails.put("customerName", order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName());
            invoiceDetails.put("accountingId", accounting.getId());
            invoiceDetails.put("totalAmount", totalPrice);
            invoiceDetails.put("paymentMethod", "CASH");

            auditService.logEvent("INVOICE_CREATE", "INVOICE", invoice.getId(), invoiceDetails);


            // Notification for new invoice
            notificationService.createAndSendNotification(
                    "INVOICE_CREATED",
                    "Invoice for order #" + invoice.getAccounting().getOrder().getId(),
                    List.of("ADMIN", "ACCOUNTANT", "SALES_MANAGER")
            );

            SoldProductMessage soldProductMessage = new SoldProductMessage(invoice.getAccounting().getOrder().getId());
            rabbitTemplate.convertAndSend(ORDERS_TOPIC_EXCHANGE_NAME,
                    "soldproducts.queue", soldProductMessage);
            return invoice;
        } catch (Exception e) {
            throw e;
        }
    }

    @Scheduled(cron = "0 0 9 * * MON-FRI")
    //@PostConstruct
    public void dailyCheckAccountings() {

        checkAccountings();
        deleteCancelledAccountings();

        // Notification for a daily report
        notificationService.createAndSendNotification(
                "DAILY_REPORT",
                "Daily accounting report",
                List.of("ACCOUNTANT", "ADMIN", "SALES_MANAGER")
        );
    }


    @Transactional
    protected void deleteCancelledAccountings() {
        List<Accounting> accountings = accountingRepository.findByStateTwo();
        if (accountings.isEmpty()) {
            return;
        }

        for (Accounting accounting : accountings) {
            try {
                Map<String, Object> deletionDetails = new HashMap<>();
                deletionDetails.put("orderId", accounting.getOrder().getId());
                deletionDetails.put("accountingId", accounting.getId());
                deletionDetails.put("totalAmount", accounting.getTotalPrice());
                deletionDetails.put("cancellationDate", accounting.getDate().toString());

                auditService.logEvent("ACCOUNTING_DELETED", "ACCOUNTING",
                        accounting.getId(), deletionDetails);

                accountingRepository.deleteByIdAndStateTwo(accounting.getId());
                logger.info("Deleted accounting with ID: {}", accounting.getId());
            } catch (Exception e) {
                logger.error("Error deleting accounting with ID: {}", accounting.getId(), e);
            }
        }
    }


    public void checkAccountings() {
        LocalDate date = LocalDate.now();
        List<Accounting> accountings = accountingRepository.deadlinePassed(date);
        if (!accountings.isEmpty()) {
            for (Accounting accounting : accountings) {
                try {

                    Map<String, Object> cancellationDetails = new HashMap<>();
                    cancellationDetails.put("orderId", accounting.getOrder().getId());
                    cancellationDetails.put("accountingId", accounting.getId());
                    cancellationDetails.put("originalDueDate", accounting.getDate().toString());
                    cancellationDetails.put("cancellationReason", "Automatsko otkazivanje zbog neplaćanja");

                    auditService.logEvent("ORDER_AUTO_CANCELLED", "ORDER",
                            accounting.getOrder().getId(), cancellationDetails);

                    accounting.setState((short) 2);
                    accountingRepository.save(accounting);

                    // Notification for canceled order
                    notificationService.createAndSendNotification(
                            "ORDER_CANCELLED",
                            "Order  #" + accounting.getOrder().getId() + " automatically cancelled",
                            List.of("SALES_MANAGER", "ADMIN", "ACCOUNTANT")
                    );

                    long orderId = accounting.getOrder().getId();
                    ReservationCancellationMessage reservationCancellation = new ReservationCancellationMessage(orderId);
                    rabbitTemplate.convertAndSend(ORDERS_TOPIC_EXCHANGE_NAME, "cancelreservation.queue", reservationCancellation);


                    logger.info("Canceled accounting with Order ID: {}", accounting.getOrder().getId());
                } catch (Exception e) {
                    logger.error("Error canceling accounting with Order ID: {}", accounting.getOrder().getId(), e);
                }
            }
        }
    }


    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Map<String, Object> deletionDetails = new HashMap<>();
        deletionDetails.put("customerId", order.getCustomer().getId());
        deletionDetails.put("customerName", order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName());
        deletionDetails.put("userId", order.getUser().getId());
        deletionDetails.put("userEmail", order.getUser().getEmail());
        deletionDetails.put("totalProducts", order.getProductList().size());

        List<String> productNames = order.getProductList().stream()
                .map(op -> op.getProduct().getProductName())
                .collect(Collectors.toList());

        deletionDetails.put("products", productNames);

        auditService.logEvent("ORDER_DELETED", "ORDER", orderId, deletionDetails);

//        // Provera da li postoji faktura
//        if (order.getAccounting() != null && order.getAccounting().getInvoice() != null) {
//            throw new RuntimeException("Cannot delete order with existing invoice");
//        }

        // Delete related entities
        orderProductRepository.deleteByOrderId(orderId);
        accountingRepository.deleteByOrderId(orderId);
        reservationRepository.deleteByOrderId(orderId);
        orderRepository.delete(order);


    }


}


