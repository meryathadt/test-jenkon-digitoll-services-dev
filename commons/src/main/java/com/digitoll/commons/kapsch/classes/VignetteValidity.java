package com.digitoll.commons.kapsch.classes;

import java.util.Date;
import java.util.Objects;

public class VignetteValidity {
	private Date requestedValidityStartDate;
	private Date validityStartDateTimeUTC;
	private Date validityEndDateTimeUTC;
	private Date validityEndDateTimeEET;
	private Date validityStartDateTimeEET;

	public Date getRequestedValidityStartDate() {
		return requestedValidityStartDate;
	}

	public void setRequestedValidityStartDate(Date requestedValidityStartDate) {
		this.requestedValidityStartDate = requestedValidityStartDate;
	}

	public Date getValidityStartDateTimeUTC() {
		return validityStartDateTimeUTC;
	}

	public void setValidityStartDateTimeUTC(Date validityStartDateTimeUTC) {
		this.validityStartDateTimeUTC = validityStartDateTimeUTC;
	}

	public Date getValidityEndDateTimeUTC() {
		return validityEndDateTimeUTC;
	}

	public void setValidityEndDateTimeUTC(Date validityEndDateTimeUTC) {
		this.validityEndDateTimeUTC = validityEndDateTimeUTC;
	}

	public Date getValidityStartDateTimeEET() {
		return validityStartDateTimeEET;
	}

	public void setValidityStartDateTimeEET(Date validityStartDateTimeEET) {
		this.validityStartDateTimeEET = validityStartDateTimeEET;
	}

	public Date getValidityEndDateTimeEET() {
		return validityEndDateTimeEET;
	}

	public void setValidityEndDateTimeEET(Date validityEndDateTimeEET) {
		this.validityEndDateTimeEET = validityEndDateTimeEET;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VignetteValidity that = (VignetteValidity) o;
		return Objects.equals(requestedValidityStartDate, that.requestedValidityStartDate) &&
				Objects.equals(validityStartDateTimeUTC, that.validityStartDateTimeUTC) &&
				Objects.equals(validityEndDateTimeUTC, that.validityEndDateTimeUTC) &&
				Objects.equals(validityEndDateTimeEET, that.validityEndDateTimeEET) &&
				Objects.equals(validityStartDateTimeEET, that.validityStartDateTimeEET);
	}

	@Override
	public int hashCode() {
		return Objects.hash(requestedValidityStartDate, validityStartDateTimeUTC, validityEndDateTimeUTC, validityEndDateTimeEET, validityStartDateTimeEET);
	}
}
