package com.digitoll.commons.dto;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Objects;

import static com.digitoll.commons.validation.UserValidation.*;

public class PasswordUpdateUserDTO {

    private String oldPassword;

    @Size(min = PASSWORD_SIZE_MIN, max = PASSWORD_SIZE_MAX, message = PASSWORD_SIZE_MESSAGE)
    @Pattern(regexp = PASSWORD_VALIDATION_REGEXP, message = PASSWORD_VALIDATION_MESSAGE)
    private String newPassword;

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public PasswordUpdateUserDTO() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordUpdateUserDTO that = (PasswordUpdateUserDTO) o;
        return Objects.equals(newPassword, that.newPassword) &&
                Objects.equals(oldPassword, that.oldPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(newPassword, oldPassword);
    }
}
