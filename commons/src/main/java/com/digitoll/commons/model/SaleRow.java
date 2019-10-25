package com.digitoll.commons.model;

import com.digitoll.commons.enumeration.VignetteValidityType;
import com.digitoll.commons.kapsch.response.VignetteRegistrationResponseContent;
import com.digitoll.commons.response.SaleRowDTO;
import com.digitoll.commons.util.BasicUtils;
import com.digitoll.commons.util.DateTimeUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

// This is the eVignette
@Document(collection = "saleRows")
public class SaleRow implements SaleRowProperties {

    @Id
    @ApiModelProperty(notes = "Auto-generated ID of the single vignette sell",
            example = "5d511ef929f61a0001dca2f4")
    private String id;

    @Indexed
    @ApiModelProperty(notes = "Kapsch vignette ID", example = "19081249907121")
    private String vignetteId;

    @Indexed
    @ApiModelProperty(notes = "The ID of the sale", example = "5d511ef829f61a0001dca2f2")
    private String saleId;

    private Sale sale;

    @Indexed
    @ApiModelProperty(notes = "The ID of the vehicle that this vignette sale is for",
            example = "5d511ef929f61a0001dca2f3")
    private String vehicleId;

    @ApiModelProperty(notes = "License plate number", example = "CM1837PP")
    private String lpn;
    //Validity type
    @ApiModelProperty(notes = "The vignette validity duration type")
    private VignetteValidityType validityType;

    //pos user, pos Id, Partner Id and name, Pos name
    @Indexed
    @ApiModelProperty(notes = "The ID of the user that made the sale",
            example = "5d949afe587eb1574a91f754")
    private String userId;

    private UserCache user;

    @Indexed
    @ApiModelProperty(notes = "The partner ID of the user who made the sale",
            example = "5d951144587eb176538525ff")
    private String partnerId;

    private Partner partner;

    @Indexed
    @ApiModelProperty(notes = "The ID of the POS that was selected by the user when creating " +
            "the sale", example = "5d951144587eb17653852600")
    private String posId;

    private Pos pos;

    @ApiModelProperty(notes = "The partner name of the user who made the sale", example = "Telenor")
    private String partnerName;

    @ApiModelProperty(notes = "Name of the POS",
            example = "София, кв. Горублене, ул. Самоковско шосе **, магазин ”****”")
    private String posName;

    private String remoteClientId;

    //
    // Kapsch properties
    private Integer kapschProductId;
    private VignetteRegistrationResponseContent kapschProperties;
    // price
    private VignettePrice price;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    @ApiModelProperty(notes = "The date on which the vignette sale was created",
            example = "2019-08-12 08:10:32.000Z")
    private Date createdOn;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime activationDate;
    // email to send
    @ApiModelProperty(notes = "The client e-mail. Will send e-mail upon sale activation to this e-mail.",
            example = "client@mail.bg")
    private String email;

    // userCreated the sale
    @ApiModelProperty(notes = "The username of the user that made the sale",
            example = "digitoll@hyperaspect.com")
    private String userName;
    @ApiModelProperty(notes = "The active status of the vignette")
    private boolean active = false;
    @ApiModelProperty(notes = "When the activation of the vignette fails, " +
            "this field is set to true")
    private boolean failedKapschTrans = false;
    @ApiModelProperty(notes = "When the activation of the vignette fails, " +
            "this field describes why it failed")
    private String failureMessage;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    @ApiModelProperty(notes = "Start date of vignette", example = "2019-08-12 15:45:07.000Z")
    private Date validityStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    @ApiModelProperty(notes = "End date validity", example = "2019-08-18 23:59:59.000Z")
    private Date validityEndDate;

    @Indexed
    @ApiModelProperty(notes = "Vignette purchase time in UTC in the form year-month-day " +
            "but without the \"-\" between them", example = "20190812")
    private int cacheDateUTC;

    @Indexed
    @ApiModelProperty(notes = "Vignette purchase time in EET in the form year-month-day " +
            "but without the \"-\" between them", example = "20190812")
    private int cacheDateEET;

    @ApiModelProperty(notes = "Vignette purchase time in UTC and in the format year-month-day",
            example = "2019-08-12")
    private String forDateUTC;
    @ApiModelProperty(notes = "Vignette purchase time in EET and in the format year-month-day",
            example = "2019-08-12")
    private String forDateEET;

    @Indexed
    @ApiModelProperty(notes = "The sequential number of the sale.", example = "18607")
    private long saleSequence;

    public SaleRow() {

    }

    // TODO remove once denormalization frontend is complete
    @JsonIgnore
    public void addReportPropertiesFromSale(Sale sale, User user) {
        setUserId(sale.getUserId());
        setUserName(sale.getUserName());
        if (StringUtils.isEmpty(this.getEmail())) {
            setEmail(sale.getEmail());
        }

        setPartnerName(sale.getPartnerName());

        setPartnerId(sale.getPartnerId());
        setPosId(sale.getPosId());
        setPosName(sale.getPosName());
        setCreatedOn(sale.getCreatedOn());
    }

    public SaleRow(SaleRowDTO saleRowDTO) {

        BasicUtils.copyPropsSkip(saleRowDTO, this, Arrays.asList("productsResponse"));
    }

    public LocalDateTime getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(LocalDateTime activationDate) {
        this.activationDate = activationDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVignetteId() {
        return vignetteId;
    }

    public void setVignetteId(String vignetteId) {
        this.vignetteId = vignetteId;
    }

    public String getSaleId() {
        return saleId;
    }

    public void setSaleId(String saleId) {
        this.saleId = saleId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public VignettePrice getPrice() {
        return price;
    }

    public void setPrice(VignettePrice price) {
        this.price = price;
    }

    public VignetteValidityType getValidityType() {
        return validityType;
    }

    public void setValidityType(VignetteValidityType validityType) {
        this.validityType = validityType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getPosId() {
        return posId;
    }

    public void setPosId(String posId) {
        this.posId = posId;
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

    public Date getValidityEndDate() {
        return validityEndDate;
    }

    public void setValidityEndDate(Date validityEndDate) {
        this.validityEndDate = validityEndDate;
    }

    public Date getValidityStartDate() {
        return validityStartDate;
    }

    public void setValidityStartDate(Date validityStartDate) {
        this.validityStartDate = validityStartDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void calculateValidityDates() {
        setValidityStartDate(Date.from(getActivationDate()
                .atZone(ZoneId.systemDefault())
                .toInstant()));
        LocalDateTime validTill = DateTimeUtil.calculateVignetteValidTill(getActivationDate(), getValidityType().toString());
        setValidityEndDate(Date.from(validTill.atZone(ZoneId.systemDefault()).toInstant()));
    }

    public VignetteRegistrationResponseContent getKapschProperties() {
        return kapschProperties;
    }

    public void setKapschProperties(
            VignetteRegistrationResponseContent kapschProperties) {
        this.kapschProperties = kapschProperties;
    }

    public Integer getKapschProductId() {
        return kapschProductId;
    }

    public void setKapschProductId(Integer kapschProductId) {
        this.kapschProductId = kapschProductId;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public boolean isFailedKapschTrans() {
        return failedKapschTrans;
    }

    public void setFailedKapschTrans(boolean failedKapschTrans) {
        this.failedKapschTrans = failedKapschTrans;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }

    public String getLpn() {
        return lpn;
    }

    public void setLpn(String lpn) {
        this.lpn = lpn;
    }

    public void cachePurchaseDates() {
        Date UTC;
        if (active) {
            UTC = this.getKapschProperties().getPurchase().getPurchaseDateTimeUTC();
        } else {
            UTC = createdOn;
        }
        int eetInt = BasicUtils.getIntFromDate(UTC, TimeZone.getTimeZone("Europe/Sofia"));
        int utcInt = BasicUtils.getIntFromDate(UTC, TimeZone.getTimeZone("UTC"));
        setCacheDateEET(eetInt);
        setCacheDateUTC(utcInt);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        setForDateUTC(sdf.format(UTC));
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Sofia"));
        setForDateEET(sdf.format(UTC));

//          cache as dates always transforms to utc
//        LocalDateTime localDateEET = UTC.toInstant().atZone(ZoneId.of("Europe/Sofia")).toLocalDate().atStartOfDay();
//        LocalDateTime localDateUTC = UTC.toInstant().atZone(ZoneId.of("UTC")).toLocalDate().atStartOfDay();
//        Date dateEET = Date.from(localDateEET.toInstant(ZoneId.of("Europe/Sofia").getRules().getOffset(localDateEET)));
//        Date dateUTC = Date.from(localDateUTC.toInstant(ZoneOffset.UTC));
//        "forDateUTC" : ISODate("2019-09-25T00:00:00Z"),
//        "forDateEET" : ISODate("2019-09-24T21:00:00Z"),
//        setForDateEET(dateEET);
//        setForDateUTC(dateUTC);
// cache as strings
    }

    public void updateDatesFromKapsch() {
        if (this.getKapschProperties().getValidity() != null) {
            this.setValidityStartDate(this.getKapschProperties().getValidity().getValidityStartDateTimeUTC());
            this.setValidityEndDate(this.getKapschProperties().getValidity().getValidityEndDateTimeUTC());
        }
        cachePurchaseDates();
    }

    public void setSaleSequence(long saleSequence) {
        this.saleSequence = saleSequence;
    }

    public long getSaleSequence() {
        return saleSequence;
    }

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }

    public Pos getPos() {
        return pos;
    }

    public void setPos(Pos pos) {
        this.pos = pos;
    }

    public UserCache getUser() {
        return user;
    }

    public void setUser(UserCache user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SaleRow saleRow = (SaleRow) o;
        return active == saleRow.active &&
                failedKapschTrans == saleRow.failedKapschTrans &&
                saleSequence == saleRow.saleSequence &&
                Objects.equals(id, saleRow.id) &&
                Objects.equals(vignetteId, saleRow.vignetteId) &&
                Objects.equals(saleId, saleRow.saleId) &&
                Objects.equals(vehicleId, saleRow.vehicleId) &&
                Objects.equals(lpn, saleRow.lpn) &&
                validityType == saleRow.validityType &&
                Objects.equals(userId, saleRow.userId) &&
                Objects.equals(partnerId, saleRow.partnerId) &&
                Objects.equals(posId, saleRow.posId) &&
                Objects.equals(partnerName, saleRow.partnerName) &&
                Objects.equals(posName, saleRow.posName) &&
                Objects.equals(kapschProductId, saleRow.kapschProductId) &&
                Objects.equals(kapschProperties, saleRow.kapschProperties) &&
                Objects.equals(price, saleRow.price) &&
                Objects.equals(createdOn, saleRow.createdOn) &&
                Objects.equals(activationDate, saleRow.activationDate) &&
                Objects.equals(email, saleRow.email) &&
                Objects.equals(userName, saleRow.userName) &&
                Objects.equals(failureMessage, saleRow.failureMessage) &&
                Objects.equals(validityStartDate, saleRow.validityStartDate) &&
                Objects.equals(validityEndDate, saleRow.validityEndDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, vignetteId, saleId, vehicleId, lpn, validityType, userId, partnerId, posId, partnerName, posName, kapschProductId, kapschProperties, price, createdOn, activationDate, email, userName, active, failedKapschTrans, failureMessage, validityStartDate, validityEndDate, saleSequence);
    }

    public int getCacheDateUTC() {
        return cacheDateUTC;
    }

    public void setCacheDateUTC(int cacheDateUTC) {
        this.cacheDateUTC = cacheDateUTC;
    }

    public int getCacheDateEET() {
        return cacheDateEET;
    }

    public void setCacheDateEET(int cacheDateEET) {
        this.cacheDateEET = cacheDateEET;
    }

    @Override
    public String getRemoteClientId() {
        return remoteClientId;
    }

    @Override
    public void setRemoteClientId(String remoteClientId) {
        this.remoteClientId = remoteClientId;
    }

    public Sale getSale() {
        return sale;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
    }

    public String getForDateUTC() {
        return forDateUTC;
    }

    public void setForDateUTC(String forDateUTC) {
        this.forDateUTC = forDateUTC;
    }

    public String getForDateEET() {
        return forDateEET;
    }

    public void setForDateEET(String forDateEET) {
        this.forDateEET = forDateEET;
    }
}
