package com.app.erp.sales.service;


import com.app.erp.entity.accounting.Accounting;
import com.app.erp.sales.repository.AccountingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class AccountingService {

    private final AccountingRepository accountingRepository;

    @Autowired
    public AccountingService(AccountingRepository accountingRepository) {
        this.accountingRepository = accountingRepository;
    }

    public Page<Accounting> getAllAccountings(int page, int size, Integer status) {
        PageRequest pageable = PageRequest.of(page, size);
        if (status != null) {
            return accountingRepository.findByState(status, pageable);
        }
        return accountingRepository.findAllWithRelations(pageable);
    }


    public void deleteAccounting(Long id) {
        accountingRepository.deleteById(id);
    }

    public List<Accounting> findAccountingsByStateTwo() {
        return accountingRepository.findByStateTwo();
    }

    public void deleteByIdAndStateTwo(Long id) {
        accountingRepository.deleteByIdAndStateTwo(id);
    }

    public Accounting getAccountingById(Long id) {
        return accountingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Accounting entry not found"));
    }

    public Accounting updateAccounting(Long id, Accounting updatedAccounting) {
        Accounting existing = getAccountingById(id);

        existing.setDate(updatedAccounting.getDate());
        existing.setTotalPrice(updatedAccounting.getTotalPrice());
        existing.setState(updatedAccounting.getState());



        return accountingRepository.save(existing);
    }


}
