package com.digitoll.erp.migrations;

import com.digitoll.commons.model.SaleRow;
import com.digitoll.erp.repository.SaleRowRepository;
import com.digitoll.erp.service.SaleReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ProductDescriptionMigration extends MongoBaseMigration<SaleRow, SaleRowRepository> {

    private int pageNumber = 0;
    private static final int PAGE_LIMIT = 100;

    @Autowired
    SaleReportService saleReportService;

    @Override
    protected Page<SaleRow> getNextPage() {
        return getRepository().findAll(PageRequest.of(++pageNumber, PAGE_LIMIT));
    }

    @Override
    protected Page<SaleRow> getFirstPage() {
        return getRepository().findAll(PageRequest.of(pageNumber, PAGE_LIMIT));
    }

    @Override
    protected SaleRow migrateItem(SaleRow item) {
        return saleReportService.getSaleRowWithDescription(item);
    }

    @Override
    protected boolean migrationCondition(SaleRow item) {
        return StringUtils.isEmpty(item.getKapschProperties().getProduct().getDescription());
    }
}
