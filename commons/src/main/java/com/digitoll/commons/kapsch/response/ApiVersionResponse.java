package com.digitoll.commons.kapsch.response;

import java.util.Objects;

public class ApiVersionResponse {
	private Boolean obsolete;
	private Boolean latest;

	public Boolean getObsolete() {
		return obsolete;
	}

	public void setObsolete(Boolean obsolete) {
		this.obsolete = obsolete;
	}

	public Boolean getLatest() {
		return latest;
	}

	public void setLatest(Boolean latest) {
		this.latest = latest;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ApiVersionResponse response = (ApiVersionResponse) o;
		return Objects.equals(obsolete, response.obsolete) &&
				Objects.equals(latest, response.latest);
	}

	@Override
	public int hashCode() {
		return Objects.hash(obsolete, latest);
	}
}
