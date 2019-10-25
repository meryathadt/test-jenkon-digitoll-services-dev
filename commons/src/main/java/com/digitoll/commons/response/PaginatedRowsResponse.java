package com.digitoll.commons.response;

import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class PaginatedRowsResponse {

    @ApiModelProperty(notes = "The amount of elements in this result set", example = "25")
    private Long totalElements;
    @ApiModelProperty(notes = "The amount of pages on this result set", example = "5")
    private Integer totalPages;
    @ApiModelProperty(notes = "The total price of the vignettes in the search results",
            example = "10000.2")
    private BigDecimal totalSum;
    private List<SaleRowDTO> saleRows;

    public PaginatedRowsResponse() {

    }

    public List<SaleRowDTO> getSaleRows() {
        return saleRows;
    }

    public void setSaleRows(List<SaleRowDTO> saleRows) {
        this.saleRows = saleRows;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public BigDecimal getTotalSum() {
        return totalSum;
    }

    public void setTotalSum(BigDecimal totalSum) {
        this.totalSum = totalSum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaginatedRowsResponse response = (PaginatedRowsResponse) o;
        return Objects.equals(totalElements, response.totalElements) &&
                Objects.equals(totalPages, response.totalPages) &&
                Objects.equals(totalSum, response.totalSum) &&
                Objects.equals(saleRows, response.saleRows);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalElements, totalPages, saleRows);
    }

    @Override
    public String toString() {
        return "PaginatedRowsResponse{" +
                "totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                ", saleRows=" + saleRows +
                ", totalSum=" + totalSum +
                '}';
    }
}
