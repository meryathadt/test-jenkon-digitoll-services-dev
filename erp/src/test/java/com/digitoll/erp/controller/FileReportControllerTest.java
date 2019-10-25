package com.digitoll.erp.controller;

import com.digitoll.erp.service.FilesReportService;
import org.apache.commons.io.FileUtils;
import org.apache.http.auth.BasicUserPrincipal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.UUID;

import static com.digitoll.erp.utils.ErpTestHelper.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(PowerMockRunner.class)
@WebMvcTest(secure = false)
@ContextConfiguration(classes = {
        FileReportController.class
})
@PrepareForTest({FileReportController.class, UUID.class, FileUtils.class})
@PowerMockRunnerDelegate(SpringRunner.class)
public class FileReportControllerTest {
    @MockBean
    private FilesReportService filesReportService;

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private SimpleDateFormat modelFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    @Autowired
    private MockMvc mvc;

    private static final String DATE_STR = "2015-09-26T09:30:00.000+0000";
    private static final String USERNAME = "userName";
    private static final String PARTNER_ID = "partnerID";
    private static final String RANDOM_UUID = "b34475a6-bc51-40cb-bd8c-455610db8cd0";
    private static final String DIRECTORY_NAME = "autob34475a6-bc51-40cb-bd8c-455610db8cd0";

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetCsvReportForDate() throws Exception {
        mvc.perform(get("/file/report").param("from_date", DATE_STR)
                .param("partner_id", PARTNER_ID)).andExpect(status().isOk());
        verify(filesReportService).getCsvAggregatedForPartners(eq(formatter.parse(DATE_STR)), eq(PARTNER_ID), any(HttpServletResponse.class), eq(null));
    }

    @Test
    public void testSendReports() throws Exception {
        PowerMockito.mockStatic(FileUtils.class);

        UUID uuid = UUID.fromString(RANDOM_UUID);
        PowerMockito.mockStatic(UUID.class);
        PowerMockito.when(UUID.randomUUID()).thenReturn(uuid);

        File directory = PowerMockito.mock(File.class);
        whenNew(File.class).withArguments(DIRECTORY_NAME).thenReturn(directory);
        mvc.perform(get("/send/reports").principal(() -> USERNAME).param("from_date", DATE_STR))
                .andExpect(status().isOk());

        // Verify that service is called with the correct directory
        verify(filesReportService).sendReportToPartners(formatter.parse(DATE_STR), directory, USERNAME);
        PowerMockito.verifyStatic(FileUtils.class, times(1));

        // Verify that the same directory is deleted
        FileUtils.deleteDirectory(directory);
    }

    @Test
    public void testGetCsvForPartners()
            throws Exception {
        mvc.perform(get("/file/report/partners").principal(() -> USERNAME).param("from_date", DATE_STR)
                .param("partner_id", PARTNER_ID)).andExpect(status().isOk());
        verify(filesReportService).getCsvForPartnersDaily(eq(formatter.parse(DATE_STR)), eq(PARTNER_ID), any(HttpServletResponse.class), eq(""), eq(USERNAME));
    }

    @Test
    public void testAggregateTwiceMonthly() throws Exception {
        mvc.perform(get("/file/report/aggregated").param("from_date", DATE_STR)
                .param("partner_id", PARTNER_ID)
                .param("type", "monthly")).andExpect(status().isOk());
        verify(filesReportService).getAggregatedReportForProductsTwiceMonthly(eq(formatter.parse(DATE_STR)), eq(PARTNER_ID), any(HttpServletResponse.class), eq(null));
    }

    @Test
    public void testAggregateDaily() throws Exception {
        mvc.perform(get("/file/report/aggregated").param("from_date", DATE_STR)
                .param("partner_id", PARTNER_ID)
                .param("type", "daily")).andExpect(status().isOk());
        verify(filesReportService).getAggregatedReportForProductsDaily(eq(formatter.parse(DATE_STR)), eq(PARTNER_ID), any(HttpServletResponse.class), eq(null));
    }

    @Test
    public void testGetCsvExport() throws Exception {
        Principal principal = new BasicUserPrincipal(PRINCIPAL_NAME);

        mvc.perform(get("/file/report/export")
                .param("vignette_id", VIGNETTE_ID)
                .param("vehicle_id", VEHICLE_ID)
                .param("partner_id", PARTNER_ID)
                .param("pos_id", POS_ID)
                .param("sale_id", SALE_ID)
                .param("user_id", USER_ID)
                .param("partner_name", PARTNER_NAME)
                .param("pos_name", POS_NAME)
                .param("user_name", USERNAME)
                .param("lpn", LPN)
                .param("email", EMAIL)
                .param("validity_type", VIGNETTE_VALIDITY_TYPE.name())
                .param("is_active", String.valueOf(ACTIVE))
                .param("validity_start_date", VALIDITY_START_DATE_PARAM)
                .param("validity_end_date", VALIDITY_END_DATE_PARAM)
                .param("created_on", CREATED_ON_PARAM)
                .param("from_date", FROM_DATE)
                .param("to_date", TO_DATE)
                .param("from_activation_date", FROM_DATE)
                .param("to_activation_date", TO_DATE)
                .param("remote_client_id", REMOTE_CLIENT_ID)
                .param("category", String.valueOf(NO_CATEGORY))
                .principal(principal))
                .andExpect(status().isOk());

        verify(filesReportService).getCsvExport(
                eq(modelFormatter.parse(VALIDITY_START_DATE)),
                eq(modelFormatter.parse(VALIDITY_END_DATE)),
                eq(LPN),
                eq(PARTNER_ID),
                eq(POS_ID),
                eq(VIGNETTE_ID),
                eq(SALE_ID),
                eq(VEHICLE_ID),
                eq(USER_ID),
                eq(PARTNER_NAME),
                eq(POS_NAME),
                eq(USERNAME),
                eq(VIGNETTE_VALIDITY_TYPE),
                eq(EMAIL),
                eq(ACTIVE),
                eq(modelFormatter.parse(CREATED_ON)),
                eq(formatter.parse(FROM_DATE)),
                eq(formatter.parse(TO_DATE)),
                eq(formatter.parse(FROM_DATE)),
                eq(formatter.parse(TO_DATE)),
                eq(REMOTE_CLIENT_ID),
                eq(NO_CATEGORY),
                eq(PRINCIPAL_NAME),
                any(HttpServletResponse.class)
        );
    }
}
