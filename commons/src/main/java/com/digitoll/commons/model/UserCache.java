package com.digitoll.commons.model;

import com.digitoll.commons.util.BasicUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

// saleRow model cant work with the roles unique code constrain, so this class exists.
public class UserCache implements UserProperties{

    private String id;

    private String username;

    private String firstName;

    private String lastName;
    // Ref partners - id
    private String partnerId;
    //Ref pos - id
    private List<String> posIds;

    private List<RoleCache> roles;

    private Date createdAt = new Date();

    private Date updatedAt;

    private boolean active = true;

    public UserCache() {}

    public UserCache(User user) {
        BasicUtils.copyPropsSkip(user, this, Arrays.asList("password"));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<RoleCache> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleCache> roles) {
        this.roles = roles;
    }


    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public List<String> getPosIds() {
        return posIds;
    }

    public void setPosIds(List<String> posIds) {
        this.posIds = posIds;
    }

    public boolean isActive() {
        return active;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
