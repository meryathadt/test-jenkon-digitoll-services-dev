package com.digitoll.commons.model;

import com.digitoll.commons.dto.PartnerDTO;
import com.digitoll.commons.util.BasicUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Document(collection = "partners")
public class Partner {

    @Id
    private String id;
    private String name;

    private String kapschPartnerId;

    public Partner(){

    }

    public Partner(PartnerDTO partnerDTO){
        BasicUtils.copyNonNullProps(partnerDTO,this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKapschPartnerId() {
        return kapschPartnerId;
    }

    public void setKapschPartnerId(String kapschPartnerId) {
        this.kapschPartnerId = kapschPartnerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Partner partner = (Partner) o;
        return Objects.equals(id, partner.id) &&
                Objects.equals(name, partner.name) &&
                Objects.equals(kapschPartnerId, partner.kapschPartnerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, kapschPartnerId);
    }
}
