package com.digitoll.erp.service;

import com.digitoll.commons.aggregation.AggregatedResult;
import com.digitoll.commons.enumeration.VignetteValidityType;
import com.digitoll.commons.exception.NoPosIdAssignedToUserException;
import com.digitoll.commons.model.Partner;
import com.digitoll.commons.model.SaleRow;
import com.digitoll.commons.response.PaginatedRowsResponse;
import com.digitoll.commons.response.ProductsResponse;
import com.digitoll.commons.response.SaleRowDTO;
import com.digitoll.commons.util.DateTimeUtil;
import com.digitoll.erp.component.EmailComponent;
import com.digitoll.erp.repository.PartnerRepository;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class FilesReportService {


    @Value("${active.environment}")
    private String activeEnvironment;

    @Value("${digitoll.partners.cash.terminal.id}")
    private String cashTerminalId;

    @Autowired
    private SaleReportService saleReportService;

    @Autowired
    private ProductService productService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private EmailComponent emailComponent;

    @Autowired
    private PartnerRepository partnerRepository;

    @Value("#{'${digitoll.partner.emails}'.split(',')}")
    private List<String> partnerEmails;

    @Value("#{'${digitoll.office.emails}'.split(',')}")
    private List<String> officeEmails;

    private static Criteria getDateCriteria(Date fromDate, Date toDate) {
        return Criteria.where("kapschProperties.purchase.purchaseDateTimeUTC").gte(fromDate).lt(toDate);
    }

    private static Criteria getCriteria(Date fromDate, Date toDate, String partnerId) {
        if (partnerId != null) {
            return getDateCriteria(fromDate, toDate).and("partnerId").is(partnerId);
        } else {
            return getDateCriteria(fromDate, toDate);
        }
    }

    public void sendReportToPartners(Date forDate, File directory, String username) throws IOException, MessagingException, NoPosIdAssignedToUserException {
        if (forDate == null) {
            forDate = new Date();
        }
        LocalDate localDate = forDate.toInstant().atZone(ZoneId.of("Europe/Sofia")).toLocalDate();
        //TODO
        List<String> fullFileNames = new ArrayList<>();
        List<String> fileNames = new ArrayList<>();
        List<String> emails = new ArrayList<>();

        // Partners --
        String fullCsvName = getCsvForPartnersDaily(forDate, cashTerminalId, null, directory.getAbsolutePath(), username);
        fullFileNames.add(fullCsvName);
        fileNames.add(fullCsvName.replace(directory.getAbsolutePath() + "/", ""));
        fullCsvName = getAggregatedReportForProductsDaily(forDate, cashTerminalId, null, directory.getAbsolutePath());
        fullFileNames.add(fullCsvName);
        fileNames.add(fullCsvName.replace(directory.getAbsolutePath() + "/", ""));
        fullCsvName = getCsvAggregatedForPartners(forDate, cashTerminalId, null, directory.getAbsolutePath());
        fullFileNames.add(fullCsvName);
        fileNames.add(fullCsvName.replace(directory.getAbsolutePath() + "/", ""));

        for (String email : partnerEmails) {
            emails.add(email);
        }
        emailComponent.sendEmailToPartners(fullFileNames, fileNames, emails, forDate);
        fullFileNames = new ArrayList<>();
        fileNames = new ArrayList<>();
        emails = new ArrayList<>();
        // In-house --
        fullCsvName = getAggregatedReportForProductsDaily(forDate, null, null, directory.getAbsolutePath());
        fullFileNames.add(fullCsvName);
        fileNames.add(fullCsvName.replace(directory.getAbsolutePath() + "/", ""));
        fullCsvName = getCsvAggregatedForPartners(forDate, null, null, directory.getAbsolutePath());
        fullFileNames.add(fullCsvName);
        fileNames.add(fullCsvName.replace(directory.getAbsolutePath() + "/", ""));

        if (localDate.getDayOfMonth() == 1 || localDate.getDayOfMonth() == 16) {
            fullCsvName = getAggregatedReportForProductsTwiceMonthly(forDate, null, null, directory.getAbsolutePath());
            fullFileNames.add(fullCsvName);
            fileNames.add(fullCsvName.replace(directory.getAbsolutePath() + "/", ""));
        }

        for (String email : officeEmails) {
            emails.add(email);
        }

        emailComponent.sendEmailToPartners(fullFileNames, fileNames, emails, forDate);
    }

    public String getCsvForPartnersDaily(Date date, String partnerId, HttpServletResponse response, String reportFolder, String username) throws IOException, NoPosIdAssignedToUserException {
        ArrayList<Date> fromDateToDate = DateTimeUtil.getOneDayDateRangeEET(date);
        return getCsvForPartners(fromDateToDate.get(0), fromDateToDate.get(1), partnerId, null, response, reportFolder, username);
    }

    public String getCsvAggregatedForPartners(Date date, String partnerId, HttpServletResponse response, String reportFolder)
            throws IOException {
        ArrayList<Date> fromDateToDate = DateTimeUtil.getOneDayDateRangeEET(date);
        return getCsvAggregatedForPartners(fromDateToDate.get(0), fromDateToDate.get(1), partnerId, response, reportFolder);
    }

    public String getAggregatedReportForProductsDaily(Date date, String partnerId, HttpServletResponse response, String reportFolder)
            throws IOException {
        ArrayList<Date> fromDateToDate = DateTimeUtil.getOneDayDateRangeEET(date);
        return aggregatedReportForProducts(fromDateToDate.get(0), fromDateToDate.get(1), partnerId, response, reportFolder);
    }

    public String getAggregatedReportForProductsTwiceMonthly(Date date, String partnerId, HttpServletResponse response, String reportFolder)
            throws IOException {
        ArrayList<Date> fromDateToDate = DateTimeUtil.getHalfMonthDateRangeEET(date);
        return aggregatedReportForProducts(fromDateToDate.get(0), fromDateToDate.get(1), partnerId, response, reportFolder);
    }

    private List<AggregatedResult> getAggregatedResults(Date fromDate, Date toDate, String groupBy, String partnerId) {

        Aggregation agg = newAggregation(
                match(getCriteria(fromDate, toDate, partnerId))
                ,
                group(groupBy)
                        .count()
                        .as("count")
                        .sum(ConvertOperators.valueOf("kapschProperties.price.amount").convertToDecimal())
                        .as("totalAmount")
                        .addToSet("kapschProductId")
                        .as("kapschProductId")
                        .addToSet("partnerId")
                        .as("partnerId")
        );
        AggregationResults<AggregatedResult> groupResults
                = mongoTemplate.aggregate(agg, SaleRow.class, AggregatedResult.class);
        List<AggregatedResult> result = new ArrayList<>();
        if (groupResults != null) {
            result.addAll(groupResults.getMappedResults());
        }
        return result;

    }

    private HashMap<String, Partner> getPartnersMap() {
        List<Partner> partners = partnerRepository.findAll();
        HashMap<String, Partner> mPartners = new HashMap<>();
        for (Partner p : partners) {
            mPartners.put(p.getId(), p);
        }
        return mPartners;
    }

    // If there is no response object to write csv to - write to file, to send to email
    private CsvMapWriter getCsvWritter(HttpServletResponse response, String fullFilePath) throws IOException {
        CsvMapWriter csvWriter;
        if (response != null) {
            csvWriter = new CsvMapWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
            response.setContentType("text/csv;charset=utf-8");
            String headerKey = "Content-Disposition";
            String headerValue = String.format("attachment; filename=\"%s\"", fullFilePath);
            response.setHeader(headerKey, headerValue);
        } else {
            File csv = new File(fullFilePath);
            File parent = new File(csv.getParent());
            if (!parent.exists()) {
                Files.createDirectories(parent.toPath());
            }
            csv.createNewFile();
            csvWriter = new CsvMapWriter(new FileWriterWithEncoding(fullFilePath, "UTF-8", false),
                    CsvPreference.TAB_PREFERENCE);
        }
        return csvWriter;
    }

    private String getCsvAggregatedForPartners(Date fromDate, Date toDate, String partnerId, HttpServletResponse response, String reportFolder)
            throws IOException {
        List<String> header = new ArrayList<>();
        String csvFileName = "aggregated_report_partners";

        Integer fileCounterForDate = 1;
        SimpleDateFormat filenameDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        csvFileName += filenameDateFormat.format(toDate) + fileCounterForDate + ".csv";
        String fullFilePath = reportFolder + "/" + csvFileName;
        CsvMapWriter csvWriter = getCsvWritter(response, fullFilePath);

        header.add("Partner");
        header.add("Kapsch_Partner_Id");
        header.add("Count");
        header.add("Amount");

        List<AggregatedResult> aggregatedResults = getAggregatedResults(fromDate, toDate, "partnerId", partnerId);
        HashMap<String, Partner> mPartners = getPartnersMap();

        String[] staticHeader = new String[header.size()];
        staticHeader = header.toArray(staticHeader);
        csvWriter.writeHeader(staticHeader);

        if (aggregatedResults == null ||
                aggregatedResults.size() == 0) {
            csvWriter.close();
            return fullFilePath;
        }

        for (AggregatedResult row : aggregatedResults) {
            if (row.getPartnerId() == null || row.getTotalAmount() == null) {
                continue;
            }
            Map<String, String> csvObj = new HashMap<>();
            BigDecimal sumForPartner = new BigDecimal("0");
            csvObj.put("Partner", mPartners.get(row.getPartnerId()).getName());
            csvObj.put("Kapsch_Partner_Id", mPartners.get(row.getPartnerId()).getKapschPartnerId());
            csvObj.put("Count", row.getCount().toString());
            csvObj.put("Amount", row.getTotalAmount().toString());
            csvWriter.write(csvObj, staticHeader);
        }
        csvWriter.close();
        return fullFilePath;
    }

    private String getCsvForPartners(Date fromActivationDate, Date toActivationDate, String partnerId, String userId, HttpServletResponse response, String reportFolder, String username) throws IOException, NoPosIdAssignedToUserException {
        List<String> header = new ArrayList<>();
        String csvFileName = "";
        if (partnerId != null) {
            HashMap<String, Partner> mPartners = getPartnersMap();
            csvFileName += mPartners.get(partnerId).getName().replace(" ", "_") + "_";
        }
        Integer fileCounterForDate = 1;
        SimpleDateFormat filenameDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        csvFileName += filenameDateFormat.format(toActivationDate) + fileCounterForDate + ".csv";

        String fullFilePath = reportFolder + "/" + csvFileName;
        CsvMapWriter csvWriter = getCsvWritter(response, fullFilePath);

        header.add("PRODUCT_CODE");
        header.add("COUNTRY_CODE");
        header.add("LPN");
        header.add("EVIGNETTE_ID");
        header.add("PURCHASE_DT_UTC");
        header.add("PURCHASE_DT_LT");
        header.add("REQUIRED_START_DATE");
        header.add("VALIDITY_STARTING_DT_UTC");
        header.add("VALIDITY_STARTING_DT_LT");
        header.add("VALIDITY_END_DT_UTC");
        header.add("VALIDITY_END_DT_LT");
        header.add("PRICE");
        header.add("CURRENCY");

        PageRequest allResults = new PageRequest(0, Integer.MAX_VALUE);

        List<SaleRowDTO> saleRows = saleReportService.getSalesByCriteria(null,
                null,
                null,
                partnerId,
                null,
                null,
                null,
                null,
                userId,
                null,
                null,
                null,
                null,
                null,
                true,
                null,
                null,
                null,
                fromActivationDate,
                toActivationDate,
                null,
                allResults,
                false,
                0,
                username).getSaleRows();

        String[] staticHeader = new String[header.size()];
        staticHeader = header.toArray(staticHeader);

        csvWriter.writeHeader(staticHeader);

        if (saleRows == null || saleRows.size() == 0) {
            csvWriter.close();
            return fullFilePath;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        SimpleDateFormat onlyDate = new SimpleDateFormat("dd.MM.yyyy");

        for (SaleRow saleRow : saleRows) {
            Map<String, String> csvObj = new HashMap<>();


            csvObj.put("PRODUCT_CODE",
                    saleRow.getKapschProductId().toString());
            csvObj.put("COUNTRY_CODE",
                    saleRow.getKapschProperties().getVehicle().getCountryCode());
            csvObj.put("LPN",
                    saleRow.getKapschProperties().getVehicle().getLpn());
            csvObj.put("EVIGNETTE_ID",
                    saleRow.getVignetteId());
            csvObj.put("PURCHASE_DT_UTC",
                    sdf.format(saleRow.getKapschProperties().getPurchase().getPurchaseDateTimeUTC()));
            csvObj.put("PURCHASE_DT_LT",
                    sdf.format(saleRow.getCreatedOn()));
            csvObj.put("REQUIRED_START_DATE",
                    onlyDate.format(saleRow.getKapschProperties().getValidity().getRequestedValidityStartDate()));
            csvObj.put("VALIDITY_STARTING_DT_UTC",
                    sdf.format(saleRow.getKapschProperties().getValidity().getValidityStartDateTimeUTC()));
            csvObj.put("VALIDITY_STARTING_DT_LT",
                    sdf.format(saleRow.getKapschProperties().getValidity().getValidityStartDateTimeEET()));
            csvObj.put("VALIDITY_END_DT_UTC",
                    sdf.format(saleRow.getKapschProperties().getValidity().getValidityEndDateTimeUTC()));
            csvObj.put("VALIDITY_END_DT_LT",
                    sdf.format(saleRow.getKapschProperties().getValidity().getValidityEndDateTimeEET()));
            csvObj.put("PRICE",
                    saleRow.getKapschProperties().getPrice().getAmount().toString());
            csvObj.put("CURRENCY",
                    saleRow.getKapschProperties().getPrice().getCurrency().toString());

            csvWriter.write(csvObj, staticHeader);
        }

        csvWriter.close();
        return fullFilePath;
    }

    private String aggregatedReportForProducts(Date fromDate, Date toDate, String partnerId, HttpServletResponse response, String reportFolder) throws IOException {
        List<String> header = new ArrayList<>();

        String csvFileName = "aggregated_report_";
        if (partnerId != null) {
            HashMap<String, Partner> mPartners = getPartnersMap();
            csvFileName += mPartners.get(partnerId).getName().replace(" ", "_") + "_";
        }
        Integer fileCounterForDate = 1;
        SimpleDateFormat filenameDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        csvFileName += filenameDateFormat.format(fromDate) + "_" + filenameDateFormat.format(toDate) + "_" + fileCounterForDate + ".csv";
        String fullFilePath = reportFolder + "/" + csvFileName;
        CsvMapWriter csvWriter = getCsvWritter(response, fullFilePath);

        header.add("PRODUCT_NAME");
        header.add("COUNT");
        header.add("SINGLE_PRICE");
        header.add("TOTAL_PRICE");

        LocaleContextHolder.setLocale(new Locale("bg"));
        List<ProductsResponse> products = productService.getProducts();
        Map<Integer, ProductsResponse> mProducts =
                products.stream().collect(Collectors.toMap(ProductsResponse::getId, item -> item));
        List<AggregatedResult> aggregatedResults = getAggregatedResults(fromDate, toDate, "kapschProductId", partnerId);

        String[] staticHeader = new String[header.size()];
        staticHeader = header.toArray(staticHeader);
        csvWriter.writeHeader(staticHeader);

        Collections.sort(aggregatedResults, new Comparator<AggregatedResult>() {
            @Override
            public int compare(AggregatedResult t1, AggregatedResult t2) {
                return t1.getKapschProductId() < t2.getKapschProductId() ? -1 :
                        t1.getKapschProductId().equals(t2.getKapschProductId()) ? 0 : 1;
            }
        });
        for (AggregatedResult row : aggregatedResults) {
            Map<String, String> csvObj = new HashMap<>();

            mProducts.get(row.getKapschProductId());

            header.add("PRODUCT_NAME");
            header.add("COUNT");
            header.add("SINGLE_PRICE");
            header.add("TOTAL_PRICE");

            String categoryDescriptionText = mProducts.get(row.getKapschProductId()).getCategoryDescriptionText();
            String vehicleType = mProducts.get(row.getKapschProductId()).getVehicleTypeText();
            String emissionClass = mProducts.get(row.getKapschProductId()).getEmissionClassText();
            String validityTypeText = mProducts.get(row.getKapschProductId()).getValidityTypeText();
            String name = row.getKapschProductId() + " : еВинетка " +
                    (categoryDescriptionText != null ? categoryDescriptionText + " " : "") +
                    (vehicleType != null ? vehicleType + " " : "") +
                    (emissionClass != null ? emissionClass + " " : "") +
                    (validityTypeText != null ? validityTypeText : "");

            csvObj.put("PRODUCT_NAME", name);

            csvObj.put("COUNT", row.getCount().toString());

            csvObj.put("SINGLE_PRICE",
                    mProducts.get(row.getKapschProductId()).getPrice().getAmount().toString());

            csvObj.put("TOTAL_PRICE",
                    row.getTotalAmount().toString());

            csvWriter.write(csvObj, staticHeader);
        }
        csvWriter.close();
        return fullFilePath;
    }


    public void getCsvExport(Date validityStartDate,
                             Date validityEndDate,
                             String lpn,
                             String partnerId,
                             String posId,
                             String vignetteId,
                             String saleId,
                             String vehicleId,
                             String userId,
                             String partnerName,
                             String posName,
                             String userName,
                             VignetteValidityType validityType,
                             String email,
                             Boolean active,
                             Date createdOn,
                             Date fromRegistrationDate,
                             Date toRegistrationDate,
                             Date fromActivationDate,
                             Date toActivationDate,
                             String remoteClientId,
                             Integer category,
                             String name,
                             HttpServletResponse response
    ) throws IOException, NoPosIdAssignedToUserException {


        aggregateCsvByFilter(validityStartDate, validityEndDate, lpn, partnerId, posId, vignetteId, saleId, vehicleId,
                userId, partnerName, posName, userName, validityType, email, active, createdOn, fromRegistrationDate,
                toRegistrationDate, fromActivationDate, toActivationDate, remoteClientId, category, name, response);
    }

    private void aggregateCsvByFilter(Date validityStartDate, Date validityEndDate, String lpn, String partnerId,
                                      String posId, String vignetteId, String saleId, String vehicleId,
                                      String userId, String partnerName, String posName, String userName,
                                      VignetteValidityType validityType, String email, Boolean active,
                                      Date createdOn, Date fromDate, Date toDate, Date fromRegistrationDate, Date toRegistrationDate, String remoteClientId,
                                      Integer category, String name,
                                      HttpServletResponse response) throws IOException, NoPosIdAssignedToUserException {
        List<String> header = new ArrayList<>();
        String csvFileName = "Report-";
        SimpleDateFormat filenameDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        csvFileName += filenameDateFormat.format(new Date()) + DateTime.now().getMillis() + ".csv";

        CsvMapWriter csvWriter = getCsvWritter(response, csvFileName);

        header.add("ACTIVE STATUS");
        header.add("LPN");
        header.add("EVIGNETTE ID");
        header.add("PURCHASE DATE");
        header.add("REGISTRATION DATE");
        header.add("EMAIL");
        header.add("VALIDITY STARTING DT UTC");
        header.add("VALIDITY END DT UTC");
        header.add("CATEGORY");
        header.add("TYPE");
        header.add("POS NAME");
        header.add("PARTNER NAME");
        header.add("PRICE");
        header.add("CURRENCY");

        PageRequest allResults = new PageRequest(0, Integer.MAX_VALUE);
        boolean showTotalSum = false;
        PaginatedRowsResponse filteredData = saleReportService.getSalesByCriteria(validityStartDate,
                validityEndDate, lpn, partnerId, posId, vignetteId,
                saleId, vehicleId, userId, partnerName, posName, userName, validityType, email, active, createdOn,
                fromDate, toDate, fromRegistrationDate, toRegistrationDate, remoteClientId, allResults, showTotalSum, category, name);
        List<SaleRowDTO> saleRows = filteredData.getSaleRows();

        String[] staticHeader = new String[header.size()];
        staticHeader = header.toArray(staticHeader);

        csvWriter.writeHeader(staticHeader);

        if (saleRows == null || saleRows.size() == 0) {
            csvWriter.close();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        for (SaleRowDTO saleRow : saleRows) {
            Map<String, String> csvObj = new HashMap<>();

            csvObj.put("ACTIVE STATUS",
                    String.valueOf(saleRow.isActive()));
            csvObj.put("LPN",
                    saleRow.getKapschProperties().getVehicle().getLpn());
            csvObj.put("EVIGNETTE ID",
                    saleRow.getVignetteId());
            if (saleRow.getCreatedOn() != null) {
                csvObj.put("PURCHASE DATE",
                        sdf.format(saleRow.getCreatedOn()));
            }

            if (saleRow.getKapschProperties().getPurchase().getPurchaseDateTimeUTC() != null) {
                csvObj.put("REGISTRATION DATE",
                        sdf.format(saleRow.getKapschProperties().getPurchase().getPurchaseDateTimeUTC()));
            }
            csvObj.put("EMAIL",
                    saleRow.getEmail());
            if (saleRow.getKapschProperties().getValidity().getValidityStartDateTimeUTC() != null) {
                csvObj.put("VALIDITY STARTING DT UTC",
                        sdf.format(saleRow.getKapschProperties().getValidity().getValidityStartDateTimeUTC()));
            }
            if (saleRow.getKapschProperties().getValidity().getValidityEndDateTimeUTC() != null) {
                csvObj.put("VALIDITY END DT UTC",
                        sdf.format(saleRow.getKapschProperties().getValidity().getValidityEndDateTimeUTC()));
            }
            csvObj.put("CATEGORY",
                    saleRow.getProductsResponse().getCategoryDescriptionText());
            csvObj.put("TYPE",
                    saleRow.getProductsResponse().getValidityTypeText());
            csvObj.put("POS NAME",
                    saleRow.getPosName());
            csvObj.put("PARTNER NAME",
                    saleRow.getPartnerName());
            csvObj.put("PRICE",
                    saleRow.getKapschProperties().getPrice().getAmount().toString());
            csvObj.put("CURRENCY",
                    saleRow.getKapschProperties().getPrice().getCurrency().toString());

            csvWriter.write(csvObj, staticHeader);
        }
        csvWriter.close();
    }
}
