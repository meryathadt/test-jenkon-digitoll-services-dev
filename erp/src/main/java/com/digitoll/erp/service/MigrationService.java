package com.digitoll.erp.service;


import com.digitoll.erp.migrations.MongoBaseMigration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class MigrationService {

    @Autowired
    private ApplicationContext applicationContext;

    private static final String MIGRATIONS_PACKAGE = "com.digitoll.erp.migrations.";

    public long migrate(String className) throws ClassNotFoundException, InterruptedException {
        Class c = Class.forName(MIGRATIONS_PACKAGE + className);
        MongoBaseMigration migration = (MongoBaseMigration) applicationContext.getBean(c);

        return migration.runMigration();
    }
}
