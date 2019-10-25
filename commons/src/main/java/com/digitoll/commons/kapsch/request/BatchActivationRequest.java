package com.digitoll.commons.kapsch.request;

import java.util.ArrayList;
import java.util.List;

public class BatchActivationRequest {

    private List<String> eVignettes = new ArrayList<>();

    public List<String> geteVignettes() {
        return eVignettes;
    }

    public void seteVignettes(List<String> eVignettes) {
        this.eVignettes = eVignettes;
    }

    public void addId(String id){
        this.eVignettes.add(id);
    }
}
