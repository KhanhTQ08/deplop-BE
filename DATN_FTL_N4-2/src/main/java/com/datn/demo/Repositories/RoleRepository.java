package com.datn.demo.Repositories;

import com.datn.demo.Entities.RoleEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Integer> {
    List<RoleEntity> findByRoleIdIn(List<Integer> roleIds);

    RoleEntity findByRoleName(String roleName);
}
