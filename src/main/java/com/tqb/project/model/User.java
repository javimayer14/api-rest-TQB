package com.tqb.project.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.Data;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user")
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
 
	@Column(unique = true, length = 64)
	private String username;
	
	@Column(length = 20)
	private String name;
	
	@Column( length = 30)
	private String lastname;
	
	private Boolean validate;
	
	private String email;

	@Column(length = 60)
	@JsonProperty(access = Access.WRITE_ONLY)
	private String password;

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<Role> role;

	private Boolean enabled;

	private String phone;
	
	@Column(name = "created_date", updatable = false)
	@CreatedDate
	private Date createdDate;

	@Column(name = "modified_date")
	@LastModifiedDate
	private Date modifiedDate;



}
