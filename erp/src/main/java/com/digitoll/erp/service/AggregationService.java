package com.digitoll.erp.service;

import com.digitoll.commons.aggregation.AggregatedResult;
import com.digitoll.commons.aggregation.AggregatedResults;
import com.digitoll.commons.dto.SaleAggregationDTO;
import com.digitoll.commons.enumeration.DateGroupingBases;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

@Service
public class AggregationService {

    public static final String KAPSCH_AMOUNT = "kapschProperties.price.amount";
    public static final String MONTHLY_BASES = "%m-%Y";
    public static final String DAILY_BASES = "%d-%m-%Y";
    public static final String YEARLY_BASES = "%Y";
    public static final String KAPSCH_PURCHASE_DATE_TIME_UTC = "kapschProperties.purchase.purchaseDateTimeUTC";
    public static final String CREATED_ON = "createdOn";
    public static final String DESCRIPTION = "kapschProperties.product.description";
    public static final String PARTNER_NAME = "partnerName";
    public static final String POS_NAME = "posName";
    public static final String PURCHASE_DATE = "purchaseDate";
    public static final String REGISTRATION_DATE = "registrationDate";
    public static final String KAPSCH_PRODUCT_ID = "kapschProductId";
    public static final String PARTNER_ID = "partnerId";
    public static final String POS_ID = "posId";
    public static final String TOTAL_AMOUNT = "totalAmount";
    public static final String PRODUCT_NAME = "productName";
    public static final String STATUS = "status";
    public static final String KAPSCH_PROPERTIES_STATUS = "kapschProperties.status";
    public static final String TIMEZONE_EUROPE_SOFIA = "Europe/Sofia";
    private MongoTemplate mongoTemplate;
    private List<String> groupingList;

    @Autowired
    public AggregationService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.groupingList = new ArrayList<>();
    }

    public AggregatedResults getAggregation(String[] groupingFields, List<Criteria> criteriaList,
                                            PageRequest page, Sort.Direction direction, String[] sortingFields,
                                            Class<?> typeOfEntity, DateGroupingBases dateGroupingBases) {

        List<AggregationOperation> aggregationOperations = new ArrayList<>();
        this.groupingList = new ArrayList<>();

        if (criteriaList != null && !criteriaList.isEmpty()) {
            MatchOperation matchOperation = match(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
            aggregationOperations.add(matchOperation);
        }

        aggregationOperations.add(mapProjectParameters(groupingFields, getDateGroupingBasesPattern(dateGroupingBases)));
        aggregationOperations.add(Aggregation.group(groupingList.toArray(new String[0]))
                .count()
                .as("count")
                .sum(ConvertOperators.valueOf("totalAmount").convertToDecimal())
                .as("totalAmount"));

        if (sortingFields != null && sortingFields.length > 0 && direction != null) {
            SortOperation sortOperation = sort(direction, sortingFields);
            aggregationOperations.add(sortOperation);
        }

        Aggregation aggregation = Aggregation.newAggregation(aggregationOperations);

        AggregationResults<AggregatedResult> groupResults
                = mongoTemplate.aggregate(aggregation, typeOfEntity, AggregatedResult.class);

        AggregatedResults aggregatedResults = new AggregatedResults();
        if (groupResults != null) {
            List<AggregatedResult> result = new ArrayList<>(groupResults.getMappedResults());
            Page<AggregatedResult> paginatedResult = findPaginated(page, result);
            aggregatedResults.setTotalElements(paginatedResult.getTotalElements());
            aggregatedResults.setTotalPages(paginatedResult.getTotalPages());
            BigDecimal totalSum = new BigDecimal(0);
            int totalCount = 0;
            if (!result.isEmpty()) {
                for (AggregatedResult row : result) {
                    if (groupingList.size() == 1) {
                        setPrivateField(row, groupingList.get(0), row.get_id());
                    }

                    if (row.getTotalAmount() != null) {
                        totalSum = totalSum.add(row.getTotalAmount());
                    }

                    if (row.getCount() != null) {
                        totalCount += row.getCount();
                    }
                }
            }
            List<SaleAggregationDTO> aggregationDTOList = paginatedResult.getContent()
                    .stream().map(SaleAggregationDTO::new).collect(Collectors.toList());
            aggregatedResults.setResults(aggregationDTOList);
            aggregatedResults.setTotalCount(totalCount);
            aggregatedResults.setTotalSum(totalSum);
        }
        return aggregatedResults;
    }

    private void setPrivateField(Object o, String fieldName, Object value) {
        try {
            Field f = o.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(o, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Page<AggregatedResult> findPaginated(Pageable pageable, List<AggregatedResult> results) {
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<AggregatedResult> list;

        if (results.size() < startItem) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, results.size());
            list = results.subList(startItem, toIndex);
        }

        return new PageImpl<>(list, PageRequest.of(currentPage, pageSize), results.size());
    }

    private String getDateGroupingBasesPattern(DateGroupingBases dateGroupingBases) {
        switch (dateGroupingBases) {
            case MONTHLY:
                return MONTHLY_BASES;
            case YEARLY:
                return YEARLY_BASES;
            default:
                return DAILY_BASES;
        }
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
                case STATUS:
                    projectFields.add(KAPSCH_PROPERTIES_STATUS);
                    groupingList.add(STATUS);
                    break;
                case POS_ID:
                    projectFields.add(field);
                    projectFields.add(POS_NAME);
                    groupingList.add(POS_NAME);
                    groupingList.add(field);
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

    public AggregatedResults getPagedAggregationForAmount(String[] groupingFields, List<Criteria> criteriaList,
                                                          PageRequest page, Sort.Direction direction, String[] sortingFields,
                                                          Class<?> typeOfEntity, DateGroupingBases dateGroupingBases) {
        return getAggregation(groupingFields, criteriaList, page, direction, sortingFields, typeOfEntity, dateGroupingBases);
    }

    public AggregatedResults getAggregationForAmount(String[] groupingFields, List<Criteria> criteriaList,
                                                     Class<?> typeOfEntity, DateGroupingBases dateGroupingBases) {
        return getAggregation(groupingFields, criteriaList, PageRequest.of(0, Integer.MAX_VALUE), Sort.Direction.ASC, null, typeOfEntity, dateGroupingBases);
    }

}
