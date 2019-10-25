package com.digitoll.erp.repository;

import com.digitoll.commons.model.SaleRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface SaleRowRepository extends MongoRepository<SaleRow, String> {
    List<SaleRow> findBySaleId(String saleId);

    Page<SaleRow> findByUserName(String username, PageRequest page);

    List<SaleRow> findByUserName(String username);

    SaleRow findOneById(String saleId);

    List<SaleRow> findByEmail(String email);

    SaleRow findOneByVignetteId(String vignetteId);

    Page<SaleRow> findAllByActive(boolean active, PageRequest page);

    Page<SaleRow> findByUserNameAndActive(String username, boolean b, PageRequest page);

    Page<SaleRow> findByEmailAndActive(String email, boolean b, PageRequest page);
}
