package com.digitoll.commons.model;

import com.digitoll.commons.util.BasicUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "kapsch_price_list")
public class KapschPriceList extends KapschProperties{

    @Id
    private String id;

    public KapschPriceList(KapschProperties kapschProperties){
        BasicUtils.copyNonNullProps(kapschProperties,this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
