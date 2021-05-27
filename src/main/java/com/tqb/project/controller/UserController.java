package com.tqb.project.controller;

import java.io.IOException;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tqb.project.model.User;
import com.tqb.project.service.IUserService;

@RestController
@Configuration
@EnableScheduling
@RequestMapping("/api")
public class UserController {
	
	@Autowired
	private IUserService userService;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	

	
	@PostMapping("/user")
	@ResponseStatus(HttpStatus.CREATED)
	public void create(@RequestBody User user) throws MessagingException, IOException {

		String passwordBcrypt = passwordEncoder.encode(user.getPassword());
		user.setPassword(passwordBcrypt);
		sendEmail();
		userService.save(user);
	}
	
    void sendEmail() throws MessagingException, IOException {
        userService.sendEmail();
    }

}
