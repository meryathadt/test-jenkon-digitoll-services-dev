package com.digitoll.commons.model;

import com.digitoll.commons.response.SaleDTO;
import com.digitoll.commons.util.BasicUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

@Document(collection = "sales")
public class Sale {
    @Transient
    public static final String SEQUENCE_NAME = "sales_sequence";

    @Id
    private String id;

    @ApiModelProperty(notes = "The sequential number of the sale.", example = "18607")
    private long saleSeq;

    //Pos properties for reports
    @ApiModelProperty(notes = "The ID of the user that made the sale", example = "5d949afe587eb1574a91f754")
    private String userId;
    @ApiModelProperty(notes = "ID of the partner which made the sale",
            example = "5d951144587eb176538525ff")
    private String partnerId;
    @ApiModelProperty(example = "5d949afe587eb1574a91f753")
    private String posId;
    @ApiModelProperty(notes = "Name of the partner which made the sale", example = "Telenor")
    private String partnerName;
    @ApiModelProperty(notes = "Name of the POS",
            example="София, кв. Горублене, ул. Самоковско шосе **, магазин ”****”")
    private String posName;
    //
    @ApiModelProperty(notes = "The total amount of the registered vignettes for sale", example = "15.00")
    private BigDecimal total;
    // Bank
    private String bankTransactionId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date createdOn;

    //client
    @ApiModelProperty(notes = "The username of the user that made the sale",
            example = "hyperaspect@mail.bg")
    private String userName;
    //client
    @ApiModelProperty(notes = "The client's email (the one who buys the vignette)")
    private String email;
    private String names;
    private String companyCountry;
    private String companyCity;
    private String companyStreet;
    private String companyIdNumber;
    private String companyName;
    private String vatId;

    private boolean failed = false;

    private String failureMessage;

    private boolean active = false;

    private String language;

    public Sale(SaleDTO saleDTO) {
        setCreatedOn(new Date());
        BasicUtils.copyPropsSkip(saleDTO, this, Arrays.asList("saleRows"));
    }

    public Sale() {
        setCreatedOn(new Date(System.currentTimeMillis()));
    }

    public String getNames() {
        return names;
    }

    public void setNames(String names) {
        this.names = names;
    }

    public String getCompanyCountry() {
        return companyCountry;
    }

    public void setCompanyCountry(String companyCountry) {
        this.companyCountry = companyCountry;
    }

    public String getCompanyCity() {
        return companyCity;
    }

    public void setCompanyCity(String companyCity) {
        this.companyCity = companyCity;
    }

    public String getCompanyStreet() {
        return companyStreet;
    }

    public void setCompanyStreet(String companyStreet) {
        this.companyStreet = companyStreet;
    }

    public String getCompanyIdNumber() {
        return companyIdNumber;
    }

    public void setCompanyIdNumber(String companyIdNumber) {
        this.companyIdNumber = companyIdNumber;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getVatId() {
        return vatId;
    }

    public void setVatId(String vatId) {
        this.vatId = vatId;
    }

    public String getPosId() {
        return posId;
    }

    public void setPosId(String posId) {
        this.posId = posId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getPosName() {
        return posName;
    }

    public void setPosName(String posName) {
        this.posName = posName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getBankTransactionId() {
        return bankTransactionId;
    }

    public void setBankTransactionId(String bankTransactionId) {
        this.bankTransactionId = bankTransactionId;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public String getLanguage() {
        return language;
    }

    public long getSaleSeq() {
        return saleSeq;
    }

    public void setSaleSeq(long saleSeq) {
        this.saleSeq = saleSeq;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sale sale = (Sale) o;
        return saleSeq == sale.saleSeq &&
                failed == sale.failed &&
                active == sale.active &&
                Objects.equals(id, sale.id) &&
                Objects.equals(userId, sale.userId) &&
                Objects.equals(partnerId, sale.partnerId) &&
                Objects.equals(posId, sale.posId) &&
                Objects.equals(partnerName, sale.partnerName) &&
                Objects.equals(posName, sale.posName) &&
                Objects.equals(total, sale.total) &&
                Objects.equals(bankTransactionId, sale.bankTransactionId) &&
                Objects.equals(createdOn, sale.createdOn) &&
                Objects.equals(userName, sale.userName) &&
                Objects.equals(email, sale.email) &&
                Objects.equals(names, sale.names) &&
                Objects.equals(companyCountry, sale.companyCountry) &&
                Objects.equals(companyCity, sale.companyCity) &&
                Objects.equals(companyStreet, sale.companyStreet) &&
                Objects.equals(companyIdNumber, sale.companyIdNumber) &&
                Objects.equals(companyName, sale.companyName) &&
                Objects.equals(vatId, sale.vatId) &&
                Objects.equals(failureMessage, sale.failureMessage) &&
                Objects.equals(language, sale.language);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, saleSeq, userId, partnerId, posId, partnerName, posName, total, bankTransactionId, createdOn, userName, email, names, companyCountry, companyCity, companyStreet, companyIdNumber, companyName, vatId, failed, failureMessage, active, language);
    }
}
