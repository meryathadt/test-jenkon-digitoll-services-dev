package com.digitoll.erp.controller;

import com.digitoll.erp.service.MigrationService;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MigrationController {

    @Autowired
    private MigrationService migrationService;

    private static final Logger log = LoggerFactory.getLogger(MigrationController.class);

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/migration/migrate")
    public long migrate(
            @RequestParam(name = "class_name")
            @ApiParam(name = "class_name", example = "Name of migration you want to start, e.g. TestMigration", required = true)
                    String className
    ) throws InterruptedException, ClassNotFoundException {
        return migrationService.migrate(className);
    }

}
