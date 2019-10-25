package com.digitoll.commons.model;

import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
//we need these interfaces to be sure that the fields are named the same in the request and the database so we can copy
//the properties, instead of mapping them
//erp - {"userId":"5d263939f0f430170b944b41","partnerId":"5d23188b09cadc1c48d07640","posId":"5d24550609cadc1fa8988508","saleRows":[{"activationDate":"2019-07-23T09:00:00.000Z","email":"pepsa@mail.bg","kapschProductId":102,"vehicle":{"countryCode":"BG","lpn":"CB1111KK"}}]}
//site - {"saleRows":[{"activationDate":"2019-07-31T09:00:00.000Z","email":"pepsa@mail.bg","kapschProductId":103,"vehicle":{"countryCode":"BG","lpn":"CB1111KK"}}]}
public interface SaleRowProperties {

    void setActivationDate(LocalDateTime activationDate);

    @ApiModelProperty(notes = "The activation date that was selected by the user when " +
            "creating the vignette sale", example = "2019-08-16 09:00:00.000Z")
    LocalDateTime getActivationDate();

    // email of client
    @ApiModelProperty(notes = "The e-mail that the user entered when " +
            "creating this vignette sale", example = "digitoll@hyperaspect.com")
    String getEmail();

    void setEmail(String email);

    void setKapschProductId(Integer kapschProductId);

    @ApiModelProperty(notes = "The ID of the Kapsch product", example = "101")
    Integer getKapschProductId();

    String getRemoteClientId();

    void setRemoteClientId(String remoteClientId);
}
