package com.digitoll.commons.dto;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Objects;

import static com.digitoll.commons.validation.UserValidation.*;

public class PasswordUpdateAdminDTO {
    @ApiModelProperty(notes = "The user's ID for which the password change is",
            example="5d263939f0f430170b944b41")
    private String userId;

    @ApiModelProperty(notes = "The new password", dataType = "java.lang.String",
            example="\"1234\"")
    @Size(min = PASSWORD_SIZE_MIN, max = PASSWORD_SIZE_MAX, message = PASSWORD_SIZE_MESSAGE)
    @Pattern(regexp = PASSWORD_VALIDATION_REGEXP, message = PASSWORD_VALIDATION_MESSAGE)
    private String password;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public PasswordUpdateAdminDTO() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordUpdateAdminDTO that = (PasswordUpdateAdminDTO) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, password);
    }
}
