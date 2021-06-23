package com.tqb.project.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tqb.project.model.User;
import com.tqb.project.model.dto.ChangePasswordDTO;
import com.tqb.project.model.Role;

import com.tqb.project.service.IUserService;


@CrossOrigin(origins = "http://localhost:4200")
@RestController
@Configuration
@EnableScheduling
@RequestMapping("/api")
public class UserController {
	
	public static final String BUSINESS_MAIL ="redestqb@gmail.com";
	public static final String AFFAIR_CONTACT_MAIL ="Validacióm de cuenta TQB";
	public static final String AFFAIR_BUSINESS_MAIL ="Nuevo usuario a confirmar";


	

	@Autowired
	private IUserService userService;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@PostMapping("/user")
	@ResponseStatus(HttpStatus.CREATED)
	public void create(@RequestBody User user) throws MessagingException, IOException {
//		userService.sendEmail(user.getEmail(), AFFAIR_CONTACT_MAIL );
//		userService.sendEmail(BUSINESS_MAIL, AFFAIR_BUSINESS_MAIL);
		userService.save(user, passwordEncoder);
	}
	
	@PostMapping("user/change-password")
	@ResponseStatus(HttpStatus.CREATED)
	public void changePassword(@RequestBody ChangePasswordDTO changePasswordForm, Authentication authentication) throws MessagingException, IOException {
		userService.changePassword(changePasswordForm, passwordEncoder, authentication);
	}



}
