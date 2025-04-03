package com.app.erp.sales.repository;


import com.app.erp.entity.Accounting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AccountingRepository extends JpaRepository<Accounting, Long> {



    @Query("SELECT a FROM Accounting a WHERE a.state = 2")
    List<Accounting> findByStateTwo();

    @Query(
            value = "SELECT DISTINCT a FROM Accounting a " +
                    "LEFT JOIN FETCH a.order o " +
                    "LEFT JOIN FETCH o.user",
            countQuery = "SELECT COUNT(DISTINCT a) FROM Accounting a"
    )
    Page<Accounting> findAllWithRelations(Pageable pageable);
//    @Query("SELECT a FROM Accounting a LEFT JOIN FETCH a.order o LEFT JOIN FETCH o.user")
//    List<Accounting> findAllWithRelations();

    @Modifying
    @Transactional
    @Query("DELETE FROM Accounting a WHERE a.id = :accountingId AND a.state = 2")
    void deleteByIdAndStateTwo(@Param("accountingId") Long accountingId);


    @Query("SELECT a FROM Accounting a WHERE a.date<=:currentDate AND a.state=0")
    List<Accounting> deadlinePassed(@Param("currentDate") LocalDate currentDate);
}

