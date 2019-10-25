package com.digitoll.commons.kapsch.request;

import com.digitoll.commons.kapsch.classes.Api;
import java.util.Objects;

public class AuthenticationRequest {
	private Api api;
	private String posId;

	public Api getApi() {
		return api;
	}

	public void setApi(Api api) {
		this.api = api;
	}

	public String getPosId() {
		return posId;
	}

	public void setPosId(String posId) {
		this.posId = posId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AuthenticationRequest that = (AuthenticationRequest) o;
		return Objects.equals(api, that.api) &&
				Objects.equals(posId, that.posId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(api, posId);
	}
}
