package com.app.erp.sales.repository;


import com.app.erp.entity.invoice.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {


   List<Invoice> findByPayDate(LocalDate payDate);

   @Query("SELECT DISTINCT i FROM Invoice i " +
           "LEFT JOIN FETCH i.accounting a " +
           "LEFT JOIN FETCH a.order o " +
           "LEFT JOIN FETCH o.productList " +
           "WHERE i.payDate IS NOT NULL")
   List<Invoice> findAllWithOrderProducts();

   @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.accounting a LEFT JOIN FETCH a.order o LEFT JOIN FETCH o.productList p LEFT JOIN FETCH p.product WHERE i.id = :id")
   Optional<Invoice> findInvoiceWithDetails(@Param("id") Long id);
}
