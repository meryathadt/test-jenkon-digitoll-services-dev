package com.digitoll.erp.repository;

import com.digitoll.commons.model.Role;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends MongoRepository<Role, String> {
    public List<Role> findAll();
}