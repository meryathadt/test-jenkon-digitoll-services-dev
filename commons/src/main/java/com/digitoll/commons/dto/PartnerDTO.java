package com.digitoll.commons.dto;

import com.digitoll.commons.model.Partner;
import com.digitoll.commons.util.BasicUtils;

public class PartnerDTO  extends Partner {

    public PartnerDTO(){

    }

    public PartnerDTO(Partner partner){
        BasicUtils.copyNonNullProps(partner, this);
    }
}
