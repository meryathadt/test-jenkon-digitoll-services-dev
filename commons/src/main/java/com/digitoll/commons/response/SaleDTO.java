package com.digitoll.commons.response;

import com.digitoll.commons.model.Sale;
import com.digitoll.commons.request.SaleRequest;
import com.digitoll.commons.util.BasicUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//was PurchasedVignettesDTO
public class SaleDTO extends Sale {

    public SaleDTO(){

    }

    public SaleDTO(Sale sale){
        BasicUtils.copyProps(sale,this, new ArrayList<>());
    }

    private List<SaleRowDTO> saleRows = new ArrayList<>();

    public SaleDTO(SaleRequest saleRequest) {
        BasicUtils.copyNonNullProps(saleRequest,this);
        List<SaleRowDTO> responseRows = new ArrayList<>();
        saleRequest.getSaleRows().forEach(r->{
            responseRows.add(new SaleRowDTO(r));
        });
        this.setSaleRows(responseRows);
    }

    public List<SaleRowDTO> getSaleRows() {
        return saleRows;
    }

    public void setSaleRows(List<SaleRowDTO> saleRows) {
        this.saleRows = saleRows;
    }

    public void addRows(List<SaleRowDTO> vignettes) {
        arraySanity();
        this.saleRows.addAll(vignettes);
    }

    public void addRow(SaleRowDTO row) {
        arraySanity();
        this.saleRows.add(row);
    }

    private void arraySanity(){
        if(this.saleRows == null){
            this.saleRows = new ArrayList<>();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SaleDTO saleDTO = (SaleDTO) o;
        return Objects.equals(saleRows, saleDTO.saleRows);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), saleRows);
    }

    @Override
    public String toString() {
      return "SaleDTO{" +
          "saleRows=" + saleRows +
          '}';
    }
}
