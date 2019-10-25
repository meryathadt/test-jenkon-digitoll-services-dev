package com.digitoll.commons.kapsch.classes;

import java.util.Date;
import java.util.Objects;

public class VignettePurchase {
	private Date purchaseDateTimeUTC;

	public Date getPurchaseDateTimeUTC() {
		return purchaseDateTimeUTC;
	}

	public void setPurchaseDateTimeUTC(Date purchaseDateTimeUTC) {
		this.purchaseDateTimeUTC = purchaseDateTimeUTC;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VignettePurchase that = (VignettePurchase) o;
		return Objects.equals(purchaseDateTimeUTC, that.purchaseDateTimeUTC);
	}

	@Override
	public int hashCode() {
		return Objects.hash(purchaseDateTimeUTC);
	}
}
