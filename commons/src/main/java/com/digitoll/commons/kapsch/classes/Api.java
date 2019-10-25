package com.digitoll.commons.kapsch.classes;

import java.util.Objects;

public class Api {
	private String userName;
	private String password;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Api api = (Api) o;
        return Objects.equals(userName, api.userName) &&
                Objects.equals(password, api.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, password);
    }
}
