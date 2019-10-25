package com.digitoll.erp.service;

import com.digitoll.commons.model.SaleRow;
import com.digitoll.erp.migrations.MongoBaseMigration;
import com.digitoll.erp.repository.SaleRowRepository;
import com.digitoll.erp.utils.ErpTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = MigrationService.class)
@RunWith(SpringRunner.class)
public class MigrationServiceTest {

    private static final String NEW_DESCRIPTION = "New Description";

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private SaleRowRepository repository;

    @InjectMocks
    private MigrationService migrationService;

    @Test
    public void testMigrate() throws InterruptedException, ClassNotFoundException, ParseException {
        ErpTestHelper erpTestHelper = new ErpTestHelper();
        Page<SaleRow> saleRows = erpTestHelper.createSaleRowPage();

        String className = "MongoBaseMigration";
        MongoBaseMigration mockMigration = getMockMongoBaseMigration(saleRows);
        doReturn(mockMigration).when(applicationContext)
                .getBean(any(Class.class));
        migrationService.migrate(className);
        verify(mockMigration).runMigration();
        assertEquals(saleRows.getContent().get(0).getKapschProperties().getProduct().getDescription(), NEW_DESCRIPTION);
    }

    @Test(expected = ClassNotFoundException.class)
    public void testMigrateClassNotFound() throws InterruptedException, ClassNotFoundException {
        String className = "NoSuchClass";
        migrationService.migrate(className);
    }

    private MongoBaseMigration getMockMongoBaseMigration(Page<SaleRow> saleRows) {
        MongoBaseMigration<SaleRow, SaleRowRepository> migration = new MongoBaseMigration<SaleRow, SaleRowRepository>() {

            @Override
            protected Page<SaleRow> getNextPage() {
                return saleRows;
            }

            @Override
            protected Page<SaleRow> getFirstPage() {
                return saleRows;
            }

            @Override
            protected SaleRow migrateItem(SaleRow item) {
                item.getKapschProperties().getProduct().setDescription(NEW_DESCRIPTION);
                return item;
            }

            @Override
            protected boolean migrationCondition(SaleRow item) {
                return true;
            }
        };

        migration.setRepository(repository);
        return spy(migration);
    }
}
