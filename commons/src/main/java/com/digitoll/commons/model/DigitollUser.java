package com.digitoll.commons.model;

import com.digitoll.commons.model.DigitollUserDetails;
import com.digitoll.commons.model.Role;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class DigitollUser extends DigitollUserDetails {
    
    private String password;
    
    @DBRef
    private Set<Role> roles = new HashSet<>();
    
    public DigitollUser() {
    }
    
    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
