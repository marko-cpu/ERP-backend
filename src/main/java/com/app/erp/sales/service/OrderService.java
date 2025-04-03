package com.app.erp.sales.service;


import com.app.erp.entity.*;
import com.app.erp.goods.repository.ArticleWarehouseRepository;
import com.app.erp.goods.repository.ProductRepository;
import com.app.erp.goods.repository.ReservationRepository;
import com.app.erp.goods.repository.WarehouseRepository;
import com.app.erp.messaging.ReservationCancellationMessage;
import com.app.erp.messaging.ReservationMessage;
import com.app.erp.messaging.SoldProductMessage;
import com.app.erp.sales.repository.*;
import com.app.erp.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.app.erp.config.RabbitMQConfig.ORDERS_TOPIC_EXCHANGE_NAME;

@Service
public class OrderService {


    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    OrderProductRepository orderProductRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    WarehouseRepository warehouseRepository;

    @Autowired
    ArticleWarehouseRepository articleWarehouseRepository;

    @Autowired
    private AccountingRepository accountingRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;
    @PersistenceContext
    private EntityManager entityManager;

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

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

    public long getOrderCount()  {
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
            // Постојећи купац
            customer = customerRepository.findById(orderRequest.getCustomer().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        } else {
            // Нови купац - провери обавезна поља
            if (orderRequest.getCustomer().getEmail() == null) {
                throw new IllegalArgumentException("Email is required for new customers");
            }

            // Провери да ли већ постоји купац са истим email-ом
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
            orderProduct.setPdv(orderProduct.calculatePdv());
            orderProduct.setOrder(order);
            this.orderProductRepository.save(orderProduct);
            totalPrice += orderProduct.getTotalPrice();
        }

        Accounting tmpAccounting = new Accounting(order, dateOfPayment, totalPrice);
        ReservationMessage reservationMessage = new ReservationMessage(productList, tmpAccounting);
        rabbitTemplate.convertAndSend(ORDERS_TOPIC_EXCHANGE_NAME, "reservation.queue", reservationMessage);


    //    System.out.println("Order created successfully with total price: " + totalPrice);
    }


    public Invoice addInvoice(long accountingId, double totalPrice) throws Exception {
        Optional<Accounting> accountingOptional = accountingRepository.findById(accountingId);
        try {
            Accounting accounting = accountingOptional.orElseThrow(() -> new Exception("Accounting id does not exist!"));

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

            SoldProductMessage soldProductMessage = new SoldProductMessage(invoice.getAccounting().getOrder().getId());
            rabbitTemplate.convertAndSend(ORDERS_TOPIC_EXCHANGE_NAME,
                    "soldproducts.queue", soldProductMessage);
            return invoice;
        } catch (Exception e) {
            throw e;
        }
    }

   // @Scheduled(cron = "0 0 9 * * MON-FRI")
    @PostConstruct
    public void dailyCheckAccountings() {

        checkAccountings();
        deleteCancelledAccountings();


    }



    @Transactional
    private void deleteCancelledAccountings() {
        List<Accounting> accountings = accountingRepository.findByStateTwo();
        if (accountings.isEmpty()) {
            return;
        }

        for (Accounting accounting : accountings) {
            try {
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
                    accounting.setState((short) 2);
                    accountingRepository.save(accounting);
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






}


