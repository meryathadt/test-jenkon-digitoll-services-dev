package com.digitoll.erp.repository;

import com.digitoll.commons.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    User findByUsername(String username);

    User findOneById(String userId);

    List<User> findAllByPartnerId(String partnerId);

    Optional<User> findByIdAndPartnerId(String userId, String partnerId);
}
