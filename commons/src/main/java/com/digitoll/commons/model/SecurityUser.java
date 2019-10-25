package com.digitoll.commons.model;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class SecurityUser extends org.springframework.security.core.userdetails.User {

    private String userId;
    private String firstName;
    private String lastName;

    public SecurityUser(String userId, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username,password,authorities);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
