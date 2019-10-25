package com.digitoll.commons.kapsch.response;

import java.util.List;

public class BatchActivationResponse {

    private List<VignetteRegistrationResponseContent> eVignettes;

    public List<VignetteRegistrationResponseContent> geteVignettes() {
        return eVignettes;
    }

    public void seteVignettes(List<VignetteRegistrationResponseContent> eVignettes) {
        this.eVignettes = eVignettes;
    }
}
