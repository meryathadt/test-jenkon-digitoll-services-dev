package com.digitoll.commons.dto;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;

//This class exists because we had issues with url encoding / decoding and spring
public class VignetteIdDTO {
    @NotBlank
    @ApiModelProperty(notes = "The Kapsch vignette ID", example="19081296329260")
    private String vignetteId;
    @ApiModelProperty(notes = "The ID of the POS (point of sale)", example="5d49a3f8ac2d426718b1c45c")
    private String posId;

    public String getVignetteId() {
        return vignetteId;
    }

    public void setVignetteId(String vignetteId) {
        this.vignetteId = vignetteId;
    }

    public String getPosId() {
        return posId;
    }

    public void setPosId(String posId) {
        this.posId = posId;
    }

  @Override
  public String toString() {
    return "VignetteIdDTO{" +
        "vignetteId='" + vignetteId + '\'' +
        ", posId='" + posId + '\'' +
        '}';
  }
}
