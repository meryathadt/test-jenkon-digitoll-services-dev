package com.digitoll.commons.dto;

import com.digitoll.commons.model.Role;
import com.digitoll.commons.model.User;
import com.digitoll.commons.util.BasicUtils;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.digitoll.commons.validation.UserValidation.*;

public class UserDetailsDTO {

	private String id;

	@Size(min = USERNAME_SIZE_MIN, max = USERNAME_SIZE_MAX, message = USERNAME_SIZE_MESSAGE)
	@Pattern(regexp = USERNAME_VALIDATION_REGEXP, message = USERNAME_VALIDATION_MESSAGE)
	private String username;

	private String firstName;
	private String lastName;
	private String partnerId;
	private List<String> posIds;
	private Date createdAt;
	private List<Role> roles;
	private boolean active;

	public UserDetailsDTO() {
	}

	public UserDetailsDTO(User user) {
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

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean hasAuthority(String roleCode) {
		for (Role role : roles) {
			if (role.getCode().equals(roleCode)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserDetailsDTO that = (UserDetailsDTO) o;
		return active == that.active &&
				Objects.equals(id, that.id) &&
				Objects.equals(username, that.username) &&
				Objects.equals(firstName, that.firstName) &&
				Objects.equals(lastName, that.lastName) &&
				Objects.equals(partnerId, that.partnerId) &&
				Objects.equals(posIds, that.posIds) &&
				Objects.equals(createdAt, that.createdAt) &&
				Objects.equals(roles, that.roles);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, username, firstName, lastName, partnerId, posIds, createdAt, roles, active);
	}
}
