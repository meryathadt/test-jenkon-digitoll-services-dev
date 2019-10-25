package com.digitoll.commons.kapsch.response;

public class VignetteRegistrationResponse {
    private VignetteRegistrationResponseContent eVignette;

    public VignetteRegistrationResponseContent geteVignette() {
        return eVignette;
    }

    public void seteVignette(VignetteRegistrationResponseContent eVignette) {
        this.eVignette = eVignette;
    }

    public VignetteRegistrationResponse() {
    }
}
