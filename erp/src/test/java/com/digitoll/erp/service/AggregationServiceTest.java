package com.digitoll.erp.service;

import com.digitoll.commons.aggregation.AggregatedResult;
import com.digitoll.commons.aggregation.AggregatedResults;
import com.digitoll.commons.enumeration.DateGroupingBases;
import com.digitoll.commons.model.SaleRow;
import com.digitoll.erp.utils.ErpTestHelper;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.digitoll.erp.service.AggregationService.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;

@ContextConfiguration(classes = AggregationService.class)
@RunWith(SpringRunner.class)
public class AggregationServiceTest {

    private static final String KAPSCH_PRODUCT_ID = "kapschProductId";
    private static final String PURCHASE_DATE = "purchaseDate";
    private List<String> groupingList = new ArrayList<>();

    @MockBean
    private MongoTemplate mongoTemplate;

    @Autowired
    private AggregationService aggregationService;
    private ErpTestHelper erpTestHelper;

    @Before
    public void init() {
        erpTestHelper = new ErpTestHelper();
    }

    @Test
    public void getAggregationTest() {

        AggregatedResults expectedResult = erpTestHelper.createAggregatedResults();
        mockAggregation(Collections.emptyList(),
                new String[]{KAPSCH_PRODUCT_ID, PURCHASE_DATE});

        assertEquals(aggregationService.getAggregation(
                new String[]{KAPSCH_PRODUCT_ID, PURCHASE_DATE}, Collections.emptyList(),
                PageRequest.of(0, 10), null, null, SaleRow.class, DateGroupingBases.DAILY), expectedResult);
    }

    @Test
    public void getAggregationNoResultsTest() {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("active").is(false));
        String[] groupingFields = {KAPSCH_PRODUCT_ID, PURCHASE_DATE};

        when(mongoTemplate.aggregate(Mockito.any(Aggregation.class),
                eq(SaleRow.class),
                eq(AggregatedResult.class)))
                .thenReturn(new AggregationResults(new ArrayList(),
                        new Document()));

        assertTrue(aggregationService.getAggregation(
                groupingFields, criteriaList,
                PageRequest.of(0, 10), null, null, SaleRow.class, DateGroupingBases.DAILY).getResults().isEmpty());
    }


    private void mockAggregation(List<Criteria> criteriaList, String[] groupFields) {
        List<AggregationOperation> aggregationOperations = new ArrayList<>();

        if (criteriaList != null && !criteriaList.isEmpty()) {
            MatchOperation matchOperation = match(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
            aggregationOperations.add(matchOperation);
        }

        aggregationOperations.add(mapProjectParameters(groupFields, DAILY_BASES));
        aggregationOperations.add(Aggregation.group(groupingList.toArray(new String[0]))
                .count()
                .as("count")
                .sum(ConvertOperators.valueOf("totalAmount").convertToDecimal())
                .as("totalAmount"));

        Aggregation aggregation = Aggregation.newAggregation(aggregationOperations);

        List<AggregatedResult> aggregatedResultList = new ArrayList<>(1);


        aggregatedResultList.add(erpTestHelper.createAggregatedResult());

        AggregationResults<AggregatedResult> groupResults =
                new AggregationResults(aggregatedResultList,
                        new Document());

        when(mongoTemplate.aggregate(argThat(new FilesReportServiceTest.ToStringMatcher<>(aggregation)),
                eq(SaleRow.class),
                eq(AggregatedResult.class)))
                .thenReturn(groupResults);
    }

    private ProjectionOperation mapProjectParameters(String[] groupingFields, String dateGroupingBases) {

        ArrayList<String> projectFields = new ArrayList<>();

        for (String field : groupingFields) {
            switch (field) {
                case PURCHASE_DATE:
                    projectFields.add(KAPSCH_PURCHASE_DATE_TIME_UTC);
                    groupingList.add(field);
                    break;
                case REGISTRATION_DATE:
                    projectFields.add(CREATED_ON);
                    groupingList.add(field);
                    break;
                case KAPSCH_PRODUCT_ID:
                    projectFields.add(field);
                    groupingList.add(field);
                    projectFields.add(DESCRIPTION);
                    groupingList.add(PRODUCT_NAME);
                    break;
                case PARTNER_ID:
                    projectFields.add(field);
                    projectFields.add(PARTNER_NAME);
                    groupingList.add(PARTNER_NAME);
                    groupingList.add(field);
                    break;
                case POS_ID:
                    projectFields.add(field);
                    projectFields.add(POS_NAME);
                    groupingList.add(POS_NAME);
                    groupingList.add(field);
                    break;
                case STATUS:
                    projectFields.add(KAPSCH_PROPERTIES_STATUS);
                    groupingList.add(STATUS);
                    break;
                default:
                    projectFields.add(field);
                    groupingList.add(field);
                    break;
            }
        }

        projectFields.add(KAPSCH_AMOUNT);

        return Aggregation.project(projectFields.toArray(new String[0]))
                .and(DateOperators.dateOf(KAPSCH_PURCHASE_DATE_TIME_UTC).withTimezone(DateOperators.Timezone.valueOf(TIMEZONE_EUROPE_SOFIA)).toString(dateGroupingBases)).as(PURCHASE_DATE)
                .and(DateOperators.dateOf(CREATED_ON).withTimezone(DateOperators.Timezone.valueOf(TIMEZONE_EUROPE_SOFIA)).toString(dateGroupingBases)).as(REGISTRATION_DATE)
                .andExpression(KAPSCH_AMOUNT).as(TOTAL_AMOUNT)
                .andExpression(KAPSCH_PROPERTIES_STATUS).as(STATUS)
                .andExpression(DESCRIPTION).as(PRODUCT_NAME);
    }
}
