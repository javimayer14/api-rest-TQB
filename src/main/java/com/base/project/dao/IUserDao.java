package com.base.project.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.base.project.model.User;

public interface IUserDao extends CrudRepository<User, Long> {

	@Query(	"FROM User u "
	+ 		"WHERE u.username = :username")
	public User findByUsername(String username);


}
