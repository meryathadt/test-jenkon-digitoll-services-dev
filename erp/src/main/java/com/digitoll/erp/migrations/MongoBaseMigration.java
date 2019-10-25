package com.digitoll.erp.migrations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;


/**
 * Base migration script to update all current entities
 * @param <T> Entity type
 * @param <E> Repository type
 */
public abstract class MongoBaseMigration<T, E extends MongoRepository<T, String>> {

    private static final Logger logger = LoggerFactory.getLogger(MongoBaseMigration.class);

    private long totalUpdatedRecords = 0;
    private int sleepInMillis = 100;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private E repository;

    public long runMigration() throws InterruptedException {
        long startMigration = System.currentTimeMillis();
        Page<T> page = getFirstPage();
        logger.info("All metadata db trans: " + (System.currentTimeMillis() - startMigration) / 1000F / 60F + " minutes");

        while (page.hasNext()) {
            migrateCurrentPage(startMigration, page);
            page = getNextPage();

        }
        //Migrate last page
        migrateCurrentPage(startMigration, page);
        logger.info(totalUpdatedRecords + " records updated for " + (System.currentTimeMillis() - startMigration) / 1000F / 60F + " minutes");
        return totalUpdatedRecords;
    }

    protected void migrateCurrentPage(long start, Page<T> page) throws InterruptedException {
        long cnt = getRepository().count();
        for (T item : page.getContent()) {
            if (migrationCondition(item)) {
                getRepository().save(migrateItem(item));
                if (totalUpdatedRecords % 100 == 0) {
                    logger.info("Processed: " + totalUpdatedRecords + " / " + cnt + " || current time:" + (System.currentTimeMillis() - start) / 1000F / 60F);
                }
                Thread.sleep(sleepInMillis);
                totalUpdatedRecords++;
            }
        }
    }

    public int getSleepInMillis() {
        return sleepInMillis;
    }

    public void setSleepInMillis(int sleepInMillis) {
        this.sleepInMillis = sleepInMillis;
    }

    protected abstract Page<T> getNextPage();

    protected abstract Page<T> getFirstPage();

    protected abstract T migrateItem(T item);

    protected abstract boolean migrationCondition(T item);

    public E getRepository() {
        return repository;
    }

    public void setRepository(E repository) {
        this.repository = repository;
    }
}
