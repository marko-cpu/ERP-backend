//package com.app.erp.customer;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import com.app.erp.entity.Customer;
//import com.app.erp.user.repository.CustomerRepository;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
//import org.springframework.test.annotation.Rollback;
//import java.util.Optional;
//
//@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@Rollback(false)
//public class CustomerRepositoryTest {
//
//    @Autowired private CustomerRepository customerRepository;
//    @Autowired private TestEntityManager entityManager;
//
//    @Test
//    public void testCreateCustomer1() {
//
//        Customer customer = new Customer();
//        customer.setFirstName("John");
//        customer.setLastName("Doe");
//        customer.setEmail("Xt0kz@example.com");
//        customer.setPassword("password");
//        customer.setAddress("123 Main St");
//        customer.setCity("Kragujevac");
//        customer.setCreatedTime(new java.util.Date());
//        customer.setPostalCode("12345");
//        customer.setPhoneNumber("555-1234");
//
//        Customer savedCustomer = customerRepository.save(customer);
//
//        assertThat(savedCustomer).isNotNull();
//        assertThat(savedCustomer.getId()).isGreaterThan(0);
//    }
//
//    @Test
//    public void testCreateCustomer2() {
//
//        Customer customer = new Customer();
//        customer.setFirstName("Marko");
//        customer.setLastName("Petrovic");
//        customer.setEmail("marko@gmail.com");
//        customer.setPassword("password123");
//        customer.setAddress("123 saint am");
//        customer.setCity("Kragujevac");
//        customer.setCreatedTime(new java.util.Date());
//        customer.setPostalCode("34000");
//        customer.setPhoneNumber("555-1234");
//
//        Customer savedCustomer = customerRepository.save(customer);
//
//        assertThat(savedCustomer).isNotNull();
//        assertThat(savedCustomer.getId()).isGreaterThan(0);
//    }
//
//
//    @Test
//    public void testListCustomers() {
//        Iterable<Customer> customers = customerRepository.findAll();
//        customers.forEach(System.out::println);
//
//        assertThat(customers).hasSizeGreaterThan(1);
//    }
//
//    @Test
//    public void testGetCustomer() {
//        Integer coustomerId = 3;
//        Optional<Customer> findById = customerRepository.findById(coustomerId);
//
//        assertThat(findById).isPresent();
//        Customer customer = findById.get();
//        System.out.println(customer);
//    }
//
//    @Test
//    public void testUpdateCustomer() {
//        Integer coustomerId = 3;
//        String firstName = "Ognjen";
//        String lastName = "Ognjenovic";
//        Customer customer = customerRepository.findById(coustomerId).get();
//        customer.setFirstName(firstName);
//        customer.setLastName(lastName);
//
//        Customer updatedCustomer = customerRepository.save(customer);
//        assertThat(updatedCustomer.getFirstName()).isEqualTo(firstName);
//        assertThat(updatedCustomer.getLastName()).isEqualTo(lastName);
//    }
//
//    @Test
//    public void testDeleteCustomer() {
//        Integer coustomerId = 4;
//        customerRepository.deleteById(coustomerId);
//
//        Optional<Customer> findById = customerRepository.findById(coustomerId);
//        assertThat(findById).isNotPresent();
//    }
//
////    @Test
////    public void testFindByEmail() {
////        String email = "marko@gmail.com";
////        Customer customer = customerRepository.findByEmail(email);
////        assertThat(customer).isNotNull();
////        System.out.println(customer);
////    }
//
//}
