package com.tqb.project.dao;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.tqb.project.model.User;

public interface IUserDao extends CrudRepository<User, Long> {

	@Query(	"FROM User u "
	+ 		"WHERE u.username = :username")
	public User findByUsername(String username);

	@Modifying
	@Query(value = "INSERT INTO user_role (user_id, role_id) VALUES (:idUsuario , :role)", nativeQuery = true)
	@Transactional
	public void saveUserRole(@Param("idUsuario") Long idUsuario, @Param("role") int role);

	@Query(	"FROM User u "
	+ 		"WHERE u.validate = false"
	+ "			OR u.validate = null")
	public List<User> findNotValidated();


}
