    package com.app.erp.goods.service;


    import com.app.erp.entity.ArticleWarehouse;
    import com.app.erp.entity.Category;
    import com.app.erp.entity.Product;
    import com.app.erp.entity.Warehouse;
    import com.app.erp.goods.repository.ArticleWarehouseRepository;
    import com.app.erp.goods.repository.ProductRepository;
    import com.app.erp.goods.repository.ReservationRepository;
    import com.app.erp.goods.repository.WarehouseRepository;
    import com.app.erp.messaging.ProductMessage;
    import org.springframework.amqp.rabbit.core.RabbitTemplate;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.stereotype.Service;

    import java.time.LocalDate;
    import java.util.*;

    import static com.app.erp.config.RabbitMQConfig.PRODUCTS_TOPIC_EXCHANGE_NAME;

    @Service
    public class ProductService {

        @Autowired
        private ProductRepository productRepository;

        @Autowired
        private RabbitTemplate rabbitTemplate;

        @Autowired
        private WarehouseRepository warehouseRepository;

        @Autowired
        private ReservationRepository reservationRepository;

        @Autowired
        private ArticleWarehouseRepository articleWarehouseRepository;

        public Page<Product> getAllProducts(Pageable pageable) {
            return productRepository.findAll(pageable);
        }
        public List<Product> getAllProductsWithoutPagination() {
             return productRepository.findAll();
        }

        public long getProductCount()  {
            return productRepository.count();
        }

        public Product addProduct(Product product) {
            product.setSku(generateSku());
            productRepository.save(product);
            ProductMessage productEventMessage = ProductMessage.createNewProduct(product);
            rabbitTemplate.convertAndSend(PRODUCTS_TOPIC_EXCHANGE_NAME,
                    "product.create",productEventMessage);
            return product;
        }

        public String generateSku() {
            Long count = productRepository.count();
            return "PROD-" + String.format("%03d", count + 1);
        }


        public void receptionOfProducts(Warehouse warehouse, List<ArticleWarehouse> articles) {
            List<Product> products = new ArrayList<>();

            for (ArticleWarehouse article : articles) {
                article.setWarehouse(warehouse);
                articleWarehouseRepository.save(article); // Ovaj poziv je dovoljan jer već čuva promene
            }

            ProductMessage productEventMessage = ProductMessage.updateStateOfProduct(products);
            rabbitTemplate.convertAndSend(PRODUCTS_TOPIC_EXCHANGE_NAME,
                    "product.updateState",productEventMessage);
        }




          public String getProductData(long productId) {

              StringBuilder sb = new StringBuilder();
              Product product = productRepository.findById(productId).orElseThrow();
              sb.append("Product: \n").append(product.getProductName()).append("\n");

              Optional<Integer> quantity = articleWarehouseRepository.findTotalQuantityByProductId(productId);
              Optional<Integer> reservedQuantity = reservationRepository.findTotalReservedQuantityByProductId(productId);
              int totalQauntity = reservedQuantity.map(integer -> quantity.get() - integer).orElseGet(quantity::get);
              sb.append("Total quantity: ").append(totalQauntity).append("\n");

              List<ArticleWarehouse> articleWarehouses = articleWarehouseRepository.findByProductId(productId);
              for(ArticleWarehouse article : articleWarehouses) {
                  Warehouse warehouse = article.getWarehouse();
                  sb.append("Price: ").append(article.getPurchasePrice())
                          .append(", name: ").append(warehouse.getWarehouseName())
                          .append(", location: ").append(warehouse.getLocation()).append("\n");
              }
              return sb.toString();


          }

        public String getProductState(long productId) {
            StringBuilder sb = new StringBuilder();
            sb.append("WarehouseID | quantity ");
            List<Object[]> result = articleWarehouseRepository.findQuantityForProductIdGroupByWarehouse(productId);

            for(Object[] o : result) {
                sb.append(o[0]).append(" | ").append(o[1]).append("\n");
            }
            return sb.toString();
        }

        public Map<String, Integer> getProductCountByCategory() {
            List<Object[]> results = productRepository.countProductsByCategory();
            Map<String, Integer> categoryCounts = new LinkedHashMap<>();

            results.forEach(result -> {
                String categoryName = (String) result[0];
                Integer count = ((Number) result[1]).intValue();
                categoryCounts.put(categoryName, count);
            });

            return categoryCounts;
        }

        public List<Category> getAllCategories() {
            return Arrays.asList(Category.values());
        }


    }



