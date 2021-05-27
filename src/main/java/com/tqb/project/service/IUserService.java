package com.tqb.project.service;

import java.io.IOException;

import javax.mail.MessagingException;

import com.tqb.project.model.User;

public interface IUserService {
	
	public User save (User user);
	public void sendEmail() throws MessagingException, IOException;

}
