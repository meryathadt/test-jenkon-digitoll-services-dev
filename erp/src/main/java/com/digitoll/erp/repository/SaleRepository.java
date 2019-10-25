package com.digitoll.erp.repository;

import com.digitoll.commons.model.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SaleRepository extends MongoRepository<Sale, String> {
    List<Sale> findByUserName(String username);

    Page<Sale> findByUserName(String username, Pageable paging);

    Sale findOneById(String id);

    Sale findOneByBankTransactionId(String transactionId);
}
