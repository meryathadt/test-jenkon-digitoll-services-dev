package com.digitoll.erp.repository;

import com.digitoll.commons.model.Pos;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PosRepository extends MongoRepository<Pos, String> {

    List<Pos> findByPartnerId(String partnerId);

    Pos findOneById(String id);

    List<Pos> findByIdIn(List<String> posIds);

    Pos findOneByPosIdInPartnersDb(String posIdInPartnersDb);
    
    Optional <Pos> findOneByCode(String code);

    Optional<Pos> findPosByPartnerIdAndId(String partnerId, String id);
}
