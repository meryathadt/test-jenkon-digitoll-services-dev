package com.digitoll.erp.service;

import com.digitoll.commons.aggregation.AggregatedResult;
import com.digitoll.commons.exception.NoPosIdAssignedToUserException;
import com.digitoll.commons.model.Partner;
import com.digitoll.commons.model.SaleRow;
import com.digitoll.commons.response.PaginatedRowsResponse;
import com.digitoll.commons.response.ProductsResponse;
import com.digitoll.commons.response.SaleRowDTO;
import com.digitoll.erp.component.EmailComponent;
import com.digitoll.erp.repository.PartnerRepository;
import com.digitoll.erp.utils.ErpTestHelper;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import javax.mail.MessagingException;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FilesReportService.class, Files.class, CsvMapWriter.class, FileWriterWithEncoding.class})
@PowerMockRunnerDelegate(SpringRunner.class)
@ContextConfiguration(classes = {
        FilesReportService.class
})
public class FilesReportServiceTest {

    @MockBean
    private SaleReportService saleReportService;

    @MockBean
    private ProductService productService;

    @MockBean
    private MongoTemplate mongoTemplate;

    @MockBean
    private EmailComponent emailComponent;

    @MockBean
    private PartnerRepository partnerRepository;

    @Autowired
    private FilesReportService filesReportService;

    private ErpTestHelper erpTestHelper = new ErpTestHelper();

    private static final String ACTIVE_ENVIRONMENT = "activeEnvironment";
    private static final String CASH_TERMINAL_ID = "cashTerminalId";
    private static final String PARTNER_EMAIL = "partnerEmail";
    private static final String OFFICE_EMAIL = "officeEmail";
    private static final String ABSOLUTE_PATH = "absolutePath";

    private static final String TEST_DATE = "2018-09-16T09:30:00.000+0000";
    private static final String DAY_START = "2018-09-15T00:00:00.000+0300";
    private static final String DAY_END = "2018-09-16T00:00:00.000+0300";
    private static final String TWICE_MONTHLY_START = "2018-09-01T00:00:00.000+0300";
    private static final String TWICE_MONTHLY_END = "2018-09-16T00:00:00.000+0300";

    private static final String CASH_TERMINAL_DAILY_PARTNER_FILE_NAME = "aggregated_report_partners2018-09-161.csv";
    private static final String CASH_TERMINAL_DAILY_PARTNER_FULL_FILE_NAME = "absolutePath/aggregated_report_partners2018-09-161.csv";

    private static final String CASH_TERMINAL_DAILY_FILE_NAME_AGGREGATED = "name_2018-09-161.csv";
    private static final String CASH_TERMINAL_DAILY_FULL_FILE_NAME_AGGREGATED = "absolutePath/name_2018-09-161.csv";

    private static final String CASH_TERMINAL_FILE_NAME_AGGREGATED = "aggregated_report_name_2018-09-15_2018-09-16_1.csv";
    private static final String CASH_TERMINAL_FULL_FILE_NAME_AGGREGATED = "absolutePath/aggregated_report_name_2018-09-15_2018-09-16_1.csv";

    private static final String DAILY_FILE_NAME_AGGREGATED = "aggregated_report_2018-09-15_2018-09-16_1.csv";
    private static final String DAILY_FULL_FILE_NAME_AGGREGATED = "absolutePath/aggregated_report_2018-09-15_2018-09-16_1.csv";

    private static final String FILE_NAME_AGGREGATED = "aggregated_report_partners2018-09-161.csv";
    private static final String FULL_FILE_NAME_AGGREGATED = "absolutePath/aggregated_report_partners2018-09-161.csv";

    private static final String TWICE_MONTHLY_AGGREGATED = "aggregated_report_2018-09-01_2018-09-16_1.csv";
    private static final String TWICE_MONTHLY_AGGREGATED_FULL_NAME = "absolutePath/aggregated_report_2018-09-01_2018-09-16_1.csv";

    private static final String CASH_TERMINAL_DAILY_PARTNER_CSV = "";
    private static final String CASH_TERMINAL_DAILY_AGGREGATED_CSV = "";
    private static final String CASH_TERMINAL_NAME_AGGREGATED_CSV = "";
    private static final String DAILY_NAME_AGGREGATED_CSV = "";
    private static final String NAME_AGGREGATED_CSV = "";
    private static final String TWICE_MONTHLY_AGGREGATED_CSV = "";

    private static final int COUNT = 1;
    private static final BigDecimal AMOUNT = new BigDecimal(1);

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @Before
    public void mockPartners() {
        List<Partner> partners = new ArrayList<>();

        Partner partner = new Partner();
        partner.setId(CASH_TERMINAL_ID);
        partner.setKapschPartnerId(ErpTestHelper.POS_ID);
        partner.setName(ErpTestHelper.NAME);

        partners.add(partner);

        when(partnerRepository.findAll()).thenReturn(partners);
    }

    @Before
    public void mockProducts() {
        List<ProductsResponse> productsResponses = new ArrayList<>(1);
        ProductsResponse productsResponse = erpTestHelper.createProductResponse();
        productsResponse.setId(ErpTestHelper.KAPSCH_PRODUCT_ID);

        productsResponses.add(productsResponse);

        when(productService.getProducts()).thenReturn(productsResponses);
    }

    @Before
    public void mockConfig() {
        List<String> partnerEmails = new ArrayList<>(1);
        List<String> officeEmails = new ArrayList<>(1);

        partnerEmails.add(PARTNER_EMAIL);
        officeEmails.add(OFFICE_EMAIL);

        ReflectionTestUtils.setField(filesReportService, "activeEnvironment", ACTIVE_ENVIRONMENT);
        ReflectionTestUtils.setField(filesReportService, "cashTerminalId", CASH_TERMINAL_ID);
        ReflectionTestUtils.setField(filesReportService, "partnerEmails", partnerEmails);
        ReflectionTestUtils.setField(filesReportService, "officeEmails", officeEmails);
    }

    @Test
    public void testSendReportToPartners() throws Exception {
        Map<String, WritingInfo> writingInfoMap = new HashMap<>();

        writingInfoMap.put(CASH_TERMINAL_DAILY_FULL_FILE_NAME_AGGREGATED,
                new WritingInfo(""));
        writingInfoMap.put(CASH_TERMINAL_DAILY_PARTNER_FULL_FILE_NAME,
                new WritingInfo(""));
        writingInfoMap.put(CASH_TERMINAL_FULL_FILE_NAME_AGGREGATED,
                new WritingInfo(""));
        writingInfoMap.put(TWICE_MONTHLY_AGGREGATED_FULL_NAME,
                new WritingInfo(""));
        writingInfoMap.put(DAILY_FULL_FILE_NAME_AGGREGATED,
                new WritingInfo(""));

        File directory = mock(File.class);

        when(directory.getAbsolutePath()).thenReturn(ABSOLUTE_PATH);

        PageRequest allResults = new PageRequest(0, Integer.MAX_VALUE);

        List<SaleRowDTO> saleRowDTOList = new ArrayList<>(1);

        SaleRowDTO saleRowDTO = erpTestHelper.createSaleRowDTO();
        saleRowDTO.getKapschProperties().setValidity(erpTestHelper.createVignetteValidity());
        saleRowDTO.getKapschProperties().setPurchase(erpTestHelper.createVignettePurchase());
        saleRowDTO.getKapschProperties().setVehicle(saleRowDTO.getVehicle());
        saleRowDTOList.add(saleRowDTO);

        mockCashTerminalGroupByPartner();
        mockCashTerminalGroupByProduct();
        mockGroupByPartner();
        mockGroupByProduct();
        mockTwiceMonthly();

        PaginatedRowsResponse paginatedRowsResponse = getPaginatedRowsResponse(saleRowDTOList);

        when(saleReportService.getSalesByCriteria(null,
                null,
                null,
                CASH_TERMINAL_ID,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                null,
                null,
                null,
                formatter.parse(DAY_START),
                formatter.parse(DAY_END),
                null,
                allResults,
                ErpTestHelper.SHOW_TOTAL_SUM,
                0,
                ErpTestHelper.USERNAME))
                .thenReturn(paginatedRowsResponse);

        List<String> officeEmails = new ArrayList<>(1);
        officeEmails.add(OFFICE_EMAIL);

        List<String> partnerFullFileNames = new ArrayList<>(3);
        List<String> partnerFiles = new ArrayList<>(3);

        List<String> partnerEmails = new ArrayList<>(1);
        partnerEmails.add(PARTNER_EMAIL);

        partnerFullFileNames.add(CASH_TERMINAL_DAILY_FULL_FILE_NAME_AGGREGATED);
        partnerFullFileNames.add(CASH_TERMINAL_FULL_FILE_NAME_AGGREGATED);
        partnerFullFileNames.add(CASH_TERMINAL_DAILY_PARTNER_FULL_FILE_NAME);
        partnerFiles.add(CASH_TERMINAL_DAILY_FILE_NAME_AGGREGATED);
        partnerFiles.add(CASH_TERMINAL_FILE_NAME_AGGREGATED);
        partnerFiles.add(CASH_TERMINAL_DAILY_PARTNER_FILE_NAME);

        List<String> officeFullFileNames = new ArrayList<>(3);
        List<String> officeFiles = new ArrayList<>(3);

        officeFullFileNames.add(DAILY_FULL_FILE_NAME_AGGREGATED);
        officeFullFileNames.add(FULL_FILE_NAME_AGGREGATED);
        officeFullFileNames.add(TWICE_MONTHLY_AGGREGATED_FULL_NAME);
        officeFiles.add(DAILY_FILE_NAME_AGGREGATED);
        officeFiles.add(FILE_NAME_AGGREGATED);
        officeFiles.add(TWICE_MONTHLY_AGGREGATED);

        verifyCorrectFilesCreated(partnerFullFileNames, partnerFiles, partnerEmails, writingInfoMap);
        mockWritings(writingInfoMap);
        filesReportService.sendReportToPartners(formatter.parse(TEST_DATE), directory, ErpTestHelper.USERNAME);

        verify(emailComponent).sendEmailToPartners(partnerFullFileNames, partnerFiles, partnerEmails, formatter.parse(TEST_DATE));
        verify(emailComponent).sendEmailToPartners(officeFullFileNames, officeFiles, officeEmails, formatter.parse(TEST_DATE));

        verifyCorrectCSV(writingInfoMap.get(DAILY_FULL_FILE_NAME_AGGREGATED).stringWriter.toString(),
                "aggregated_report_2018-09-15_2018-09-16_1.csv");
        verifyCorrectCSV(writingInfoMap.get(FULL_FILE_NAME_AGGREGATED).stringWriter.toString(),
                "aggregated_report_partners2018-09-161.csv");
        verifyCorrectCSV(writingInfoMap.get(TWICE_MONTHLY_AGGREGATED_FULL_NAME).stringWriter.toString(),
                "aggregated_report_2018-09-01_2018-09-16_1.csv");
    }

    @Test
    public void testGetCsvExport() throws Exception {
        PageRequest allResults = new PageRequest(0, Integer.MAX_VALUE);

        List<SaleRowDTO> saleRowDTOList = new ArrayList<>(1);

        SaleRowDTO saleRowDTO = erpTestHelper.createSaleRowDTO();
        saleRowDTO.getKapschProperties().setValidity(erpTestHelper.createVignetteValidity());
        saleRowDTO.getKapschProperties().setPurchase(erpTestHelper.createVignettePurchase());
        saleRowDTO.getKapschProperties().setVehicle(saleRowDTO.getVehicle());
        saleRowDTOList.add(saleRowDTO);

        PaginatedRowsResponse paginatedRowsResponse = getPaginatedRowsResponse(saleRowDTOList);

        mockGetSalesByCriteria(allResults, paginatedRowsResponse);

        MockHttpServletResponse response = new MockHttpServletResponse();

        filesReportService.getCsvExport(erpTestHelper.modelFormatter.parse(ErpTestHelper.VALIDITY_START_DATE),
                erpTestHelper.modelFormatter.parse(ErpTestHelper.VALIDITY_END_DATE),
                ErpTestHelper.LPN,
                ErpTestHelper.PARTNER_ID,
                ErpTestHelper.POS_ID,
                ErpTestHelper.VIGNETTE_ID,
                ErpTestHelper.SALE_ID,
                ErpTestHelper.VEHICLE_ID,
                ErpTestHelper.USER_ID,
                ErpTestHelper.PARTNER_NAME,
                ErpTestHelper.POS_NAME,
                ErpTestHelper.USERNAME,
                ErpTestHelper.VIGNETTE_VALIDITY_TYPE,
                ErpTestHelper.EMAIL,
                ErpTestHelper.ACTIVE,
                erpTestHelper.modelFormatter.parse(ErpTestHelper.CREATED_ON),
                erpTestHelper.formatter.parse(ErpTestHelper.FROM_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.TO_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.FROM_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.TO_DATE),
                null,
                ErpTestHelper.NO_CATEGORY,
                ErpTestHelper.USERNAME,
                response);

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        String expectedContent = String.format("ACTIVE STATUS,LPN,EVIGNETTE ID,PURCHASE DATE,REGISTRATION DATE,"
                        + "EMAIL,VALIDITY STARTING DT UTC,VALIDITY END DT UTC,CATEGORY,TYPE,POS NAME,PARTNER NAME,PRICE,CURRENCY\r\n"
                        + "%b,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%.1f,%s",
                saleRowDTO.isActive(), saleRowDTO.getLpn(), saleRowDTO.getVignetteId(),
                sdf.format(saleRowDTO.getCreatedOn()),
                sdf.format(saleRowDTO.getKapschProperties().getPurchase().getPurchaseDateTimeUTC()),
                saleRowDTO.getEmail(), sdf.format(saleRowDTO.getKapschProperties().getValidity().getValidityStartDateTimeUTC()),
                sdf.format(saleRowDTO.getKapschProperties().getValidity().getValidityEndDateTimeUTC()),
                saleRowDTO.getProductsResponse().getCategoryDescriptionText(),
                saleRowDTO.getProductsResponse().getValidityTypeText(),
                saleRowDTO.getPosName(), saleRowDTO.getPartnerName(),
                saleRowDTO.getKapschProperties().getPrice().getAmount(),
                saleRowDTO.getKapschProperties().getPrice().getCurrency());

        verify(saleReportService).getSalesByCriteria(erpTestHelper.modelFormatter.parse(ErpTestHelper.VALIDITY_START_DATE),
                erpTestHelper.modelFormatter.parse(ErpTestHelper.VALIDITY_END_DATE),
                ErpTestHelper.LPN,
                ErpTestHelper.PARTNER_ID,
                ErpTestHelper.POS_ID,
                ErpTestHelper.VIGNETTE_ID,
                ErpTestHelper.SALE_ID,
                ErpTestHelper.VEHICLE_ID,
                ErpTestHelper.USER_ID,
                ErpTestHelper.PARTNER_NAME,
                ErpTestHelper.POS_NAME,
                ErpTestHelper.USERNAME,
                ErpTestHelper.VIGNETTE_VALIDITY_TYPE,
                ErpTestHelper.EMAIL,
                ErpTestHelper.ACTIVE,
                erpTestHelper.modelFormatter.parse(ErpTestHelper.CREATED_ON),
                erpTestHelper.formatter.parse(ErpTestHelper.FROM_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.TO_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.FROM_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.TO_DATE),
                null,
                allResults,
                ErpTestHelper.SHOW_TOTAL_SUM,
                ErpTestHelper.NO_CATEGORY,
                ErpTestHelper.USERNAME);

        assertEquals(expectedContent, response.getContentAsString().trim());
    }

    @Test
    public void testGetCsvExportNoResults() throws Exception {
        PageRequest allResults = new PageRequest(0, Integer.MAX_VALUE);

        List<SaleRowDTO> saleRowDTOList = new ArrayList<>();

        PaginatedRowsResponse paginatedRowsResponse = getPaginatedRowsResponse(saleRowDTOList);

        mockGetSalesByCriteria(allResults, paginatedRowsResponse);

        MockHttpServletResponse response = new MockHttpServletResponse();

        filesReportService.getCsvExport(erpTestHelper.modelFormatter.parse(ErpTestHelper.VALIDITY_START_DATE),
                erpTestHelper.modelFormatter.parse(ErpTestHelper.VALIDITY_END_DATE),
                ErpTestHelper.LPN,
                ErpTestHelper.PARTNER_ID,
                ErpTestHelper.POS_ID,
                ErpTestHelper.VIGNETTE_ID,
                ErpTestHelper.SALE_ID,
                ErpTestHelper.VEHICLE_ID,
                ErpTestHelper.USER_ID,
                ErpTestHelper.PARTNER_NAME,
                ErpTestHelper.POS_NAME,
                ErpTestHelper.USERNAME,
                ErpTestHelper.VIGNETTE_VALIDITY_TYPE,
                ErpTestHelper.EMAIL,
                ErpTestHelper.ACTIVE,
                erpTestHelper.modelFormatter.parse(ErpTestHelper.CREATED_ON),
                erpTestHelper.formatter.parse(ErpTestHelper.FROM_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.TO_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.FROM_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.TO_DATE),
                null,
                ErpTestHelper.NO_CATEGORY,
                ErpTestHelper.USERNAME,
                response);

        String expectedContent = "ACTIVE STATUS,LPN,EVIGNETTE ID,PURCHASE DATE,REGISTRATION DATE,"
                + "EMAIL,VALIDITY STARTING DT UTC,VALIDITY END DT UTC,CATEGORY,TYPE,POS NAME,PARTNER NAME,PRICE,CURRENCY";

        verify(saleReportService).getSalesByCriteria(erpTestHelper.modelFormatter.parse(ErpTestHelper.VALIDITY_START_DATE),
                erpTestHelper.modelFormatter.parse(ErpTestHelper.VALIDITY_END_DATE),
                ErpTestHelper.LPN,
                ErpTestHelper.PARTNER_ID,
                ErpTestHelper.POS_ID,
                ErpTestHelper.VIGNETTE_ID,
                ErpTestHelper.SALE_ID,
                ErpTestHelper.VEHICLE_ID,
                ErpTestHelper.USER_ID,
                ErpTestHelper.PARTNER_NAME,
                ErpTestHelper.POS_NAME,
                ErpTestHelper.USERNAME,
                ErpTestHelper.VIGNETTE_VALIDITY_TYPE,
                ErpTestHelper.EMAIL,
                ErpTestHelper.ACTIVE,
                erpTestHelper.modelFormatter.parse(ErpTestHelper.CREATED_ON),
                erpTestHelper.formatter.parse(ErpTestHelper.FROM_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.TO_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.FROM_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.TO_DATE),
                null,
                allResults,
                ErpTestHelper.SHOW_TOTAL_SUM,
                ErpTestHelper.NO_CATEGORY,
                ErpTestHelper.USERNAME);

        assertEquals(expectedContent, response.getContentAsString().trim());
    }

    private void mockGetSalesByCriteria(PageRequest allResults, PaginatedRowsResponse paginatedRowsResponse) throws NoPosIdAssignedToUserException, ParseException {
        when(saleReportService.getSalesByCriteria(erpTestHelper.modelFormatter.parse(ErpTestHelper.VALIDITY_START_DATE),
                erpTestHelper.modelFormatter.parse(ErpTestHelper.VALIDITY_END_DATE),
                ErpTestHelper.LPN,
                ErpTestHelper.PARTNER_ID,
                ErpTestHelper.POS_ID,
                ErpTestHelper.VIGNETTE_ID,
                ErpTestHelper.SALE_ID,
                ErpTestHelper.VEHICLE_ID,
                ErpTestHelper.USER_ID,
                ErpTestHelper.PARTNER_NAME,
                ErpTestHelper.POS_NAME,
                ErpTestHelper.USERNAME,
                ErpTestHelper.VIGNETTE_VALIDITY_TYPE,
                ErpTestHelper.EMAIL,
                ErpTestHelper.ACTIVE,
                erpTestHelper.modelFormatter.parse(ErpTestHelper.CREATED_ON),
                erpTestHelper.formatter.parse(ErpTestHelper.FROM_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.TO_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.FROM_DATE),
                erpTestHelper.formatter.parse(ErpTestHelper.TO_DATE),
                null,
                allResults,
                ErpTestHelper.SHOW_TOTAL_SUM,
                ErpTestHelper.NO_CATEGORY,
                ErpTestHelper.USERNAME))
                .thenReturn(paginatedRowsResponse);
    }

    private PaginatedRowsResponse getPaginatedRowsResponse(List<SaleRowDTO> saleRowDTOList) {
        PaginatedRowsResponse paginatedRowsResponse = new PaginatedRowsResponse();
        paginatedRowsResponse.setTotalPages(ErpTestHelper.TOTAL_PAGES);
        paginatedRowsResponse.setTotalElements(ErpTestHelper.TOTAL_ELEMENTS);
        paginatedRowsResponse.setSaleRows(saleRowDTOList);
        return paginatedRowsResponse;
    }

    private void verifyCorrectFilesCreated(List<String> partnerFullFileNames, List<String> partnerFiles, List<String> partnerEmails, Map<String, WritingInfo> writingInfoMap) throws ParseException, MessagingException {
        CsvMapWriter csvMapWriter = new CsvMapWriter(new StringWriter(), CsvPreference.TAB_PREFERENCE);
        FileWriterWithEncoding fileWriter = mock(FileWriterWithEncoding.class);

        doAnswer((Answer<Void>) invocation -> {
            verifyCorrectCSV(writingInfoMap.get(CASH_TERMINAL_DAILY_FULL_FILE_NAME_AGGREGATED).stringWriter.toString(),
                    "cash_terminal/name_2018-09-161.csv");
            verifyCorrectCSV(writingInfoMap.get(CASH_TERMINAL_DAILY_PARTNER_FULL_FILE_NAME).stringWriter.toString(),
                    "cash_terminal/aggregated_report_partners2018-09-161.csv");
            verifyCorrectCSV(writingInfoMap.get(CASH_TERMINAL_FULL_FILE_NAME_AGGREGATED).stringWriter.toString(),
                    "cash_terminal/aggregated_report_name_2018-09-15_2018-09-16_1.csv");
            whenNew(FileWriterWithEncoding.class)
                    .withArguments(FULL_FILE_NAME_AGGREGATED, "UTF-8", false).thenReturn(fileWriter);

            whenNew(CsvMapWriter.class).withParameterTypes(Writer.class, CsvPreference.class)
                    .withArguments(fileWriter, CsvPreference.TAB_PREFERENCE)
                    .thenReturn(csvMapWriter);

            return null;
        }).when(emailComponent).sendEmailToPartners(partnerFullFileNames, partnerFiles, partnerEmails, formatter.parse(TEST_DATE));
    }

    private void verifyCorrectCSV(String csv, String fileName) throws IOException {
        CsvMapReader producedCSVReader = new CsvMapReader(new StringReader(csv), CsvPreference.TAB_PREFERENCE);
        CsvMapReader expectedReader = new CsvMapReader(new BufferedReader(new InputStreamReader(
                this.getClass().getResourceAsStream("/reports/" + fileName))), CsvPreference.TAB_PREFERENCE);

        equals(producedCSVReader, expectedReader);
    }

    private void equals(CsvMapReader reader1, CsvMapReader reader2) throws IOException {
        String[] headers1 = reader1.getHeader(true);
        String[] headers2 = reader2.getHeader(true);

        assertArrayEquals(headers1, headers2);
        assertEquals(reader1.length(), reader2.length());

        for (int i = 1; i <= reader1.length(); ++i) {
            String line1 = reader1.get(i);
            String line2 = reader2.get(i);

            assertEquals(line1, line2);
        }
    }

    private void mockWritings(Map<String, WritingInfo> writingInfoMap) throws Exception {
        PowerMockito.mockStatic(Files.class);
        PowerMockito.mockStatic(CsvMapWriter.class);

        writingInfoMap.keySet().forEach(key -> {
            WritingInfo writingInfo = writingInfoMap.get(key);
            writingInfo.stringWriter = new StringWriter();
            writingInfo.fileWriter = mock(FileWriterWithEncoding.class);
            writingInfo.csvWriter = new CsvMapWriter(writingInfo.stringWriter, CsvPreference.TAB_PREFERENCE);
        });

        // Just make sure no files are created
        File mockFile = PowerMockito.mock(File.class);
        whenNew(File.class).withAnyArguments().thenReturn(mockFile);

        writingInfoMap.forEach((key, value) -> {
            try {
                whenNew(FileWriterWithEncoding.class)
                        .withArguments(key, "UTF-8", false).thenReturn(value.fileWriter);

                whenNew(CsvMapWriter.class).withParameterTypes(Writer.class, CsvPreference.class)
                        .withArguments(value.fileWriter, CsvPreference.TAB_PREFERENCE)
                        .thenReturn(value.csvWriter);
            } catch (Exception e) {
                e.printStackTrace();
                fail(e.getMessage());
            }
        });
    }

    public static class WritingInfo {
        StringWriter stringWriter;
        FileWriterWithEncoding fileWriter;
        String expectedContent;
        CsvMapWriter csvWriter;

        public WritingInfo(String expectedContent) {
            this.expectedContent = expectedContent;
        }
    }

    private void mockTwiceMonthly() throws ParseException {
        Aggregation agg = newAggregation(
                match(Criteria.where("kapschProperties.purchase.purchaseDateTimeUTC")
                        .gte(formatter.parse(TWICE_MONTHLY_START))
                        .lt(formatter.parse(TWICE_MONTHLY_END)))
                ,
                group("kapschProductId")
                        .count()
                        .as("count")
                        .sum(ConvertOperators.valueOf("kapschProperties.price.amount").convertToDouble())
                        .as("amount")
                        .addToSet("kapschProductId")
                        .as("kapschProductId")
                        .addToSet("partnerId")
                        .as("partnerId")
        );

        List<AggregatedResult> aggregatedResultList = new ArrayList<>(1);

        AggregatedResult aggregatedResult = new AggregatedResult();
        aggregatedResult.setTotalAmount(new Decimal128(AMOUNT));
        aggregatedResult.setCount(COUNT);
        aggregatedResult.setKapschProductId(ErpTestHelper.KAPSCH_PRODUCT_ID);

        aggregatedResultList.add(aggregatedResult);

        AggregationResults<AggregatedResult> groupResults =
                new AggregationResults(aggregatedResultList,
                        new Document());

        when(mongoTemplate.aggregate(argThat(new ToStringMatcher<>(agg)),
                eq(SaleRow.class),
                eq(AggregatedResult.class)))
                .thenReturn(groupResults);
    }

    private void mockGroupByProduct() throws ParseException {
        Aggregation agg = newAggregation(
                match(Criteria.where("kapschProperties.purchase.purchaseDateTimeUTC")
                        .gte(formatter.parse(DAY_START)).lt(formatter.parse(DAY_END)))
                ,
                group("kapschProductId")
                        .count()
                        .as("count")
                        .sum(ConvertOperators.valueOf("kapschProperties.price.amount").convertToDouble())
                        .as("amount")
                        .addToSet("kapschProductId")
                        .as("kapschProductId")
        );

        List<AggregatedResult> aggregatedResultList = new ArrayList<>(1);

        AggregatedResult aggregatedResult = new AggregatedResult();
        aggregatedResult.setTotalAmount(new Decimal128(AMOUNT));
        aggregatedResult.setCount(COUNT);
        aggregatedResult.setKapschProductId(ErpTestHelper.KAPSCH_PRODUCT_ID);

        aggregatedResultList.add(aggregatedResult);

        AggregationResults<AggregatedResult> groupResults =
                new AggregationResults(aggregatedResultList,
                        new Document());

        when(mongoTemplate.aggregate(argThat(new ToStringMatcher<>(agg)),
                eq(SaleRow.class),
                eq(AggregatedResult.class)))
                .thenReturn(groupResults);
    }

    private void mockGroupByPartner() throws ParseException {
        Aggregation agg = newAggregation(
                match(Criteria.where("kapschProperties.purchase.purchaseDateTimeUTC")
                        .gte(formatter.parse(DAY_START)).lt(formatter.parse(DAY_END)))
                ,
                group("partnerId")
                        .count()
                        .as("count")
                        .sum(ConvertOperators.valueOf("kapschProperties.price.amount").convertToDouble())
                        .as("amount")
                        .addToSet("kapschProductId")
                        .as("kapschProductId")
        );

        List<AggregatedResult> aggregatedResultList = new ArrayList<>(1);

        AggregatedResult aggregatedResult = new AggregatedResult();
        aggregatedResult.setTotalAmount(new Decimal128(AMOUNT));
        aggregatedResult.setCount(COUNT);
        aggregatedResult.setPartnerId(CASH_TERMINAL_ID);

        aggregatedResultList.add(aggregatedResult);

        AggregationResults<AggregatedResult> groupResults =
                new AggregationResults(aggregatedResultList,
                        new Document());

        when(mongoTemplate.aggregate(argThat(new ToStringMatcher<>(agg)),
                eq(SaleRow.class),
                eq(AggregatedResult.class)))
                .thenReturn(groupResults);
    }

    private void mockCashTerminalGroupByProduct() throws ParseException {
        Aggregation agg = newAggregation(
                match(Criteria.where("kapschProperties.purchase.purchaseDateTimeUTC")
                        .gte(formatter.parse(DAY_START)).lt(formatter.parse(DAY_END))
                        .and("partnerId").is(CASH_TERMINAL_ID))
                ,
                group("kapschProductId")
                        .count()
                        .as("count")
                        .sum(ConvertOperators.valueOf("kapschProperties.price.amount").convertToDouble())
                        .as("amount")
                        .addToSet("kapschProductId")
                        .as("kapschProductId")
        );

        List<AggregatedResult> aggregatedResultList = new ArrayList<>(1);

        AggregatedResult aggregatedResult = new AggregatedResult();
        aggregatedResult.setTotalAmount(new Decimal128(AMOUNT));
        aggregatedResult.setCount(COUNT);
        aggregatedResult.setKapschProductId(ErpTestHelper.KAPSCH_PRODUCT_ID);

        aggregatedResultList.add(aggregatedResult);

        AggregationResults<AggregatedResult> groupResults =
                new AggregationResults(aggregatedResultList,
                        new Document());

        when(mongoTemplate.aggregate(argThat(new ToStringMatcher<>(agg)),
                eq(SaleRow.class),
                eq(AggregatedResult.class)))
                .thenReturn(groupResults);
    }

    private void mockCashTerminalGroupByPartner() throws ParseException {
        Aggregation agg = newAggregation(
                match(Criteria.where("kapschProperties.purchase.purchaseDateTimeUTC")
                        .gte(formatter.parse(DAY_START)).lt(formatter.parse(DAY_END))
                        .and("partnerId").is(CASH_TERMINAL_ID))
                ,
                group("partnerId")
                        .count()
                        .as("count")
                        .sum(ConvertOperators.valueOf("kapschProperties.price.amount").convertToDouble())
                        .as("amount")
                        .addToSet("kapschProductId")
                        .as("kapschProductId")
        );

        List<AggregatedResult> aggregatedResultList = new ArrayList<>(1);

        AggregatedResult aggregatedResult = new AggregatedResult();
        aggregatedResult.setTotalAmount(new Decimal128(AMOUNT));
        aggregatedResult.setCount(COUNT);
        aggregatedResult.setPartnerId(CASH_TERMINAL_ID);

        aggregatedResultList.add(aggregatedResult);

        AggregationResults<AggregatedResult> groupResults =
                new AggregationResults(aggregatedResultList,
                        new Document());

        when(mongoTemplate.aggregate(argThat(new ToStringMatcher<>(agg)),
                eq(SaleRow.class),
                eq(AggregatedResult.class)))
                .thenReturn(groupResults);
    }

    public static final class ToStringMatcher<T> implements ArgumentMatcher<T> {
        private T base;

        public ToStringMatcher(T base) {
            this.base = base;
        }

        @Override
        public boolean matches(T t) {
            if (t == null) {
                return false;
            }
            return base.toString().equals(t.toString());
        }
    }
}