package com.digitoll.commons.aggregation;

import com.digitoll.commons.dto.SaleAggregationDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class AggregatedResults {
    private List<SaleAggregationDTO> results;
    private Long totalElements;
    private Integer totalPages;
    private BigDecimal totalSum;
    private Integer totalCount;

    public BigDecimal getTotalSum() {
        return totalSum;
    }

    public void setTotalSum(BigDecimal totalSum) {
        this.totalSum = totalSum;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public List<SaleAggregationDTO> getResults() {
        return results;
    }

    public void setResults(List<SaleAggregationDTO> results) {
        this.results = results;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AggregatedResults that = (AggregatedResults) o;
        return Objects.equals(results, that.results) &&
                Objects.equals(totalElements, that.totalElements) &&
                Objects.equals(totalPages, that.totalPages) &&
                Objects.equals(totalSum, that.totalSum) &&
                Objects.equals(totalCount, that.totalCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(results, totalElements, totalPages, totalSum, totalCount);
    }
}
