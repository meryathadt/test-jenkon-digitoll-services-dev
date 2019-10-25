package com.digitoll.commons.kapsch.response.c9;

public class SearchVignetteValidity {

    private String requestedValidityStartDate;
    private String validityEndDateTimeUTC;
    private String validityStartDateTimeUTC;

    public String getRequestedValidityStartDate() {
        return requestedValidityStartDate;
    }

    public void setRequestedValidityStartDate(String requestedValidityStartDate) {
        this.requestedValidityStartDate = requestedValidityStartDate;
    }

    public String getValidityEndDateTimeUTC() {
        return validityEndDateTimeUTC;
    }

    public void setValidityEndDateTimeUTC(String validityEndDateTimeUTC) {
        this.validityEndDateTimeUTC = validityEndDateTimeUTC;
    }

    public String getValidityStartDateTimeUTC() {
        return validityStartDateTimeUTC;
    }

    public void setValidityStartDateTimeUTC(String validityStartDateTimeUTC) {
        this.validityStartDateTimeUTC = validityStartDateTimeUTC;
    }
}
