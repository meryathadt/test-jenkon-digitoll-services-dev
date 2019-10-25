package com.digitoll.erp.controller;

import com.digitoll.erp.service.MigrationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(secure = false)
@ContextConfiguration(classes = {
        MigrationController.class
})
public class MigrationControllerTest {

    private static final long MIGRATED_ITEMS = 15;

    @Autowired
    protected MockMvc mvc;

    @MockBean
    private MigrationService migrationService;

    @Test
    public void testMigrate() throws Exception {
        String migrationClassName = "TestMigration";
        when(migrationService.migrate(eq(migrationClassName))).thenReturn(MIGRATED_ITEMS);

        mvc.perform(get("/migration/migrate")
                .param("class_name", migrationClassName)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(mvcResult -> assertEquals(mvcResult.getResponse().getContentAsString(), String.valueOf(MIGRATED_ITEMS)));
    }
}