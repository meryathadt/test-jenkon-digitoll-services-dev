package com.digitoll.erp.service;

import com.digitoll.commons.exception.ExpiredAuthTokenException;
import com.digitoll.commons.kapsch.classes.APIEndpoints;
import com.digitoll.commons.kapsch.classes.Api;
import com.digitoll.commons.kapsch.request.AuthenticationRequest;
import com.digitoll.commons.kapsch.request.BatchActivationRequest;
import com.digitoll.commons.kapsch.request.VignetteActivationRequest;
import com.digitoll.commons.kapsch.request.VignetteRegistrationRequest;
import com.digitoll.commons.kapsch.response.*;
import com.digitoll.commons.kapsch.response.c9.*;
import com.digitoll.commons.util.ObjectsSortingComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class KapschService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${cbo.api.endpoint.c2}")
    private String endpointC2;

    @Value("${cbo.api.version.c2}")
    private String apiVersionC2;

    @Value("${cbo.api.endpoint.c9}")
    private String endpointC9;

    @Value("${cbo.api.version.c9}")
    private String apiVersionC9;

    @Value("${cbo.api.username}")
    private String apiUsername;

    @Value("${cbo.api.password}")
    private String apiPassword;

    private volatile String authToken;

    private static final Logger log = LoggerFactory.getLogger(KapschService.class);

    /* ################################## */
    /* ############### C2 ############### */
    /* ################################## */

    public ApiVersionResponse getVersion() {
        return restTemplate.getForObject(endpointC2 + "/" + apiVersionC2, ApiVersionResponse.class);
    }

    @Retryable(
            value = {ExpiredAuthTokenException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200)
    )
    public VignetteInventoryResponse getInventory(HttpSession session)
            throws ExpiredAuthTokenException, HttpStatusCodeException {

        String url = endpointC2 + "/" + apiVersionC2 + "/" + APIEndpoints.VIGNETTE_INVENTORY.getURL();

        return (VignetteInventoryResponse) this.execute(
                session,
                endpointC2,
                apiVersionC2,
                url,
                VignetteInventoryResponse.class,
                HttpMethod.GET
        );
    }

    @Retryable(
            value = {ExpiredAuthTokenException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200)
    )
    public VignetteRegistrationResponse registerVignette(VignetteRegistrationRequest request, HttpSession session)
            throws ExpiredAuthTokenException, HttpStatusCodeException {

        String url = endpointC2 + "/" + apiVersionC2 + "/" + APIEndpoints.REGISTER_VIGNETTE.getURL();

        log.debug("sessionId in register: " + session.getId());

        return (VignetteRegistrationResponse) this.execute(
                session,
                endpointC2,
                apiVersionC2,
                url,
                request,
                VignetteRegistrationResponse.class,
                HttpMethod.POST
        );
    }

    @Retryable(
            value = {ExpiredAuthTokenException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200)
    )
    public VignetteRegistrationResponse activateVignette(VignetteActivationRequest request, String posId,
                                                         HttpSession session) throws ExpiredAuthTokenException, HttpStatusCodeException {

        String url = endpointC2 + "/" + apiVersionC2 + "/" + APIEndpoints.ACTIVATE_VIGNETTE_START.getURL() +
                "/" + request.getId() + "/" + APIEndpoints.ACTIVATE_VIGNETTE_END.getURL();

        return (VignetteRegistrationResponse) this.execute(
                session,
                endpointC2,
                apiVersionC2,
                url,
                request,
                VignetteRegistrationResponse.class,
                HttpMethod.POST,
                posId
        );
    }

    @Retryable(
            value = {ExpiredAuthTokenException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200)
    )
    public BatchActivationResponse activateBatch(BatchActivationRequest batchActivationRequest, HttpSession session)
            throws ExpiredAuthTokenException, HttpStatusCodeException {

        String url = endpointC2 + "/" + apiVersionC2 + "/" + APIEndpoints.ACTIVATE_VIGNETTE_BATCH.getURL();

        return (BatchActivationResponse) this.execute(
                session,
                endpointC2,
                apiVersionC2,
                url,
                batchActivationRequest,
                BatchActivationResponse.class,
                HttpMethod.POST
        );
    }


    /* ################################## */
    /* ############### C9 ############### */
    /* ################################## */

    @Retryable(
            value = {ExpiredAuthTokenException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200)
    )

    public AggregatedSalesResponse getDailySales(Date date, HttpSession session)
            throws ExpiredAuthTokenException, HttpStatusCodeException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        String url = endpointC9 + "/" +
                apiVersionC9 + "/" +
                APIEndpoints.DAILY_SALES_C9.getURL() +
                "?date=" + sdf.format(date);

        return (AggregatedSalesResponse) this.execute(
                session,
                endpointC9,
                apiVersionC9,
                url,
                AggregatedSalesResponse.class,
                HttpMethod.GET
        );
    }

    @Retryable(
            value = {ExpiredAuthTokenException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200)
    )
    public VignetteStatesResponse getVignetteStates(HttpSession session)
            throws ExpiredAuthTokenException, HttpStatusCodeException {

        String url = endpointC9 + "/" +
                apiVersionC9 + "/" +
                APIEndpoints.VIGNETTE_STATES_C9.getURL();

        return (VignetteStatesResponse) this.execute(
                session,
                endpointC9,
                apiVersionC9,
                url,
                VignetteStatesResponse.class,
                HttpMethod.GET
        );
    }

    public ApiVersionResponse getVersionC9() {
        return restTemplate.getForObject(endpointC9 + "/" + apiVersionC9, ApiVersionResponse.class);
    }

    @Retryable(
            value = {ExpiredAuthTokenException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200)
    )
    public VignetteInventoryResponse getInventoryC9(HttpSession session)
            throws ExpiredAuthTokenException, HttpStatusCodeException {

        String url = endpointC9 + "/" + apiVersionC9 + "/" + APIEndpoints.VIGNETTE_INVENTORY_C9.getURL();

        return (VignetteInventoryResponse) this.execute(
                session,
                endpointC9,
                apiVersionC9,
                url,
                VignetteInventoryResponse.class,
                HttpMethod.GET
        );
    }

    @Retryable(
            value = {ExpiredAuthTokenException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200)
    )
    public SearchResponse vignetteSearch(String vignetteId, HttpSession session) throws ExpiredAuthTokenException {
        String queryString =
                "recordsFrom=" +
                        "&lastUpdateFrom=" +
                        "&lastUpdateTo=" +
                        "&eVignetteID=" + (StringUtils.isEmpty(vignetteId) ? "" : vignetteId) +
                        "&salesPartnerID=" +
                        "&productID=" +
                        "&lpn=" +
                        "&countryCode=" +
                        "&status=";

        String url = String.format("%s/%s/%s?%s", endpointC9, apiVersionC9,
                APIEndpoints.VIGNETTE_SEARCH_C9.getURL(),
                queryString
        );

        return (SearchResponse) this.execute(
                session,
                endpointC9,
                apiVersionC9,
                url,
                SearchResponse.class,
                HttpMethod.GET
        );
    }

    @Retryable(
            value = {ExpiredAuthTokenException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200)
    )
    public VignetteRegistrationResponse vignetteInfo(String vignetteId, HttpSession session) throws ExpiredAuthTokenException {

        String url = endpointC2 + "/" + apiVersionC2 + "/" + APIEndpoints.VIGNETTE_INFO_START.getURL() +
                "/" + vignetteId + "/" + APIEndpoints.VIGNETTE_INFO_END.getURL();

        return (VignetteRegistrationResponse) this.execute(
                session,
                endpointC2,
                apiVersionC2,
                url,
                VignetteRegistrationResponse.class,
                HttpMethod.GET
        );
    }

    @Retryable(
            value = {ExpiredAuthTokenException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200)
    )
    public PaginatedKapshSearchResponse vignetteSearch(String sortingParameter, Sort.Direction sortingDirection, int lastRecord, String eVignetteID, Integer salesPartnerID, Integer productID, String lpn,
                                                       String countryCode, String recordsFrom, Integer status, Date lastUpdateFrom,
                                                       Date lastUpdateTo, HttpSession session,
                                                       PageRequest page) throws ExpiredAuthTokenException, HttpStatusCodeException {


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        String queryString =
                "recordsFrom=" + (StringUtils.isEmpty(recordsFrom) ? lastRecord - 1 : recordsFrom) +
                        "&lastUpdateFrom=" + (lastUpdateFrom == null ? "" : sdf.format(lastUpdateFrom)) +
                        "&lastUpdateTo=" + (lastUpdateTo == null ? "" : sdf.format(lastUpdateTo)) +
                        "&eVignetteID=" + (StringUtils.isEmpty(eVignetteID) ? "" : eVignetteID) +
                        "&salesPartnerID=" + (salesPartnerID == null ? "" : salesPartnerID) +
                        "&productID=" + (productID == null ? "" : productID) +
                        "&lpn=" + (StringUtils.isEmpty(lpn) ? "" : lpn) +
                        "&countryCode=" + (StringUtils.isEmpty(countryCode) ? "" : countryCode) +
                        "&status=" + (status == null ? "" : status);

        String url = String.format("%s/%s/%s?%s", endpointC9, apiVersionC9,
                APIEndpoints.VIGNETTE_SEARCH_C9.getURL(),
                queryString
        );
        SearchResponse searchResponse = (SearchResponse) this.execute(
                session,
                endpointC9,
                apiVersionC9,
                url,
                SearchResponse.class,
                HttpMethod.GET
        );

        PaginatedKapshSearchResponse response = new PaginatedKapshSearchResponse();
        List<SearchVignette> vignettesList = searchResponse.geteVignetteList();
        if (sortingParameter != null) {
            vignettesList.sort(new ObjectsSortingComparator(sortingParameter, sortingDirection));
        }
        int maxListSizeFromKapsch = 500;
        int start = (int) page.getOffset() - ((maxListSizeFromKapsch * lastRecord) - maxListSizeFromKapsch);
        int end = Math.min((start + page.getPageSize()), vignettesList.size());

        if (searchResponse.isHasMoreRecords() && start >= maxListSizeFromKapsch) {
            return vignetteSearch(sortingParameter, sortingDirection, ++lastRecord, eVignetteID, salesPartnerID, productID, lpn, countryCode, recordsFrom,
                    status, lastUpdateFrom, lastUpdateTo, session, page);

        }

        if (start < 0) {
            return vignetteSearch(sortingParameter, sortingDirection, --lastRecord, eVignetteID, salesPartnerID, productID, lpn, countryCode, recordsFrom,
                    status, lastUpdateFrom, lastUpdateTo, session, page);
        }

        Page<SearchVignette> vignettes = new PageImpl<>(vignettesList.subList(start, end), page, vignettesList.size());
        if (searchResponse.isHasMoreRecords()) {
            response.setHasMoreRecords(true);
        } else {
            if (start + page.getPageSize() >= vignettesList.size()) {
                response.setHasMoreRecords(false);
            } else {
                response.setHasMoreRecords(true);
            }
        }

        response.setLastRecord(searchResponse.getLastRecord() != null ? searchResponse.getLastRecord() : lastRecord - 1);
        response.seteVignetteList(getVignettesResponse(vignettes));
        return response;
    }

    private ArrayList<SearchVignette> getVignettesResponse(Page<SearchVignette> vignettes) {
        ArrayList<SearchVignette> result = new ArrayList<>();
        for (SearchVignette vignette : vignettes) {
            result.add(vignette);
        }
        return result;
    }

    @Retryable(
            value = {ExpiredAuthTokenException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200)
    )
    public PeriodSalesResponse getPeriodSales(Integer salesPartnerID, Integer productID,
                                              String recordsFrom, Date purchaseFromDate,
                                              Date purchaseToDate, HttpSession session
    ) throws ExpiredAuthTokenException, HttpStatusCodeException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        String queryString =
                "recordsFrom=" + recordsFrom +
                        "&purchaseFromDate=" + sdf.format(purchaseFromDate) +
                        "&purchaseToDate=" + sdf.format(purchaseToDate) +
                        "&salesPartnerID=" + (salesPartnerID == null ? "" : salesPartnerID) +
                        "&productID=" + (productID == null ? "" : productID);

        String url = String.format("%s/%s/%s?%s", endpointC9, apiVersionC9,
                APIEndpoints.PERIOD_SALES_C9.getURL(),
                queryString
        );

        return (PeriodSalesResponse) this.execute(
                session,
                endpointC9,
                apiVersionC9,
                url,
                PeriodSalesResponse.class,
                HttpMethod.GET
        );
    }

    private HttpHeaders createHeadersWithToken(String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }

    private AuthenticationResponse authenticate(String endpoint, String apiVersion, String userName, String password, String posId) {

        AuthenticationRequest payload = new AuthenticationRequest();

        Api api = new Api();
        api.setUserName(userName);
        api.setPassword(password);

        payload.setApi(api);
        payload.setPosId(posId);

        return restTemplate.postForObject(endpoint + "/" + apiVersion + "/" + APIEndpoints.AUTHENITCATE.getURL(),
                payload, AuthenticationResponse.class);
    }

    private Object execute(HttpSession session, String endpoint, String apiVersion, String url, Class responseType,
                           HttpMethod httpMethod) throws ExpiredAuthTokenException {

        return execute(session, endpoint, apiVersion, url, null, responseType, httpMethod);
    }

    private Object execute(HttpSession session, String endpoint, String apiVersion, String url, Object request, Class responseType,
                           HttpMethod httpMethod) throws ExpiredAuthTokenException {

        return execute(session, endpoint, apiVersion, url, request, responseType, httpMethod, null);
    }

    private Object execute(HttpSession session, String endpoint, String apiVersion, String url, Object request, Class responseType,
                           HttpMethod httpMethod, String posId) throws ExpiredAuthTokenException, HttpStatusCodeException {

        try {

            if ( authToken == null ) {
              throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
            }

            log.info("url: " + url);

            return restTemplate.exchange(
                    url,
                    httpMethod,
                    new HttpEntity(request, this.createHeadersWithToken(authToken)),
                    responseType
            ).getBody();
        } catch (HttpStatusCodeException ex) {

            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED || ex.getStatusCode() == HttpStatus.FORBIDDEN) {
                log.info("in retry");

                AuthenticationResponse response =
                        this.authenticate(endpoint, apiVersion, apiUsername, apiPassword, null);

                authToken = response.getToken();

                throw new ExpiredAuthTokenException("token expired");
            }
            log.error(ex.getResponseBodyAsString());
            throw ex;
        }
    }


}
