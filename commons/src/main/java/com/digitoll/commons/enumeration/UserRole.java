package com.digitoll.commons.enumeration;

public enum UserRole {

    USER("ROLE_USERS"),
    PARTNER_EMPLOYEE("ROLE_C2"),
    WEB_USER("ROLE_C9"),
    ADMIN("ROLE_ADMIN"),
    PARTNER_ADMIN("ROLE_PARTNER_ADMIN"),
    NO_POS_USER("ROLE_NO_POS"),
    ACCOUNTANT("ROLE_ACCOUNTANT"),
    SUPPORT_1("ROLE_SUPPORT_1");

    private String roleCode;

    UserRole(String roleCode) {

        this.roleCode = roleCode;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }
}
