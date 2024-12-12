package com.datn.demo.Repositories;

import com.datn.demo.Entities.AccountEntity;
import com.datn.demo.Entities.MovieEntity;
import com.datn.demo.Entities.ShowtimeEntity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Integer> {
    AccountEntity findByUsername(String username);
    AccountEntity findByEmail(String email);
    List<AccountEntity> findByRole_RoleName(String roleName);
    List<AccountEntity> findByRole_RoleId(int roleId);
    boolean existsByUsername(String username);  // Kiểm tra tên đăng nhập đã tồn tại
    boolean existsByEmail(String email);
    List<AccountEntity> findAllByOrderByAccountIdDesc();
	AccountEntity findByAccountId(int accountId);
	List<AccountEntity> findByIsDeletedFalseOrderByAccountIdDesc();

	List<AccountEntity> findByIsDeletedTrueOrderByAccountIdDesc();
    // Phương thức tìm các tài khoản có roleId = 2
    List<AccountEntity> findByRoleRoleId(int roleId);
}

