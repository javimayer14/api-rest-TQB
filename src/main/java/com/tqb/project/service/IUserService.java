package com.tqb.project.service;

import java.io.IOException;

import javax.mail.MessagingException;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.tqb.project.model.TestResult;
import com.tqb.project.model.User;
import com.tqb.project.model.dto.ChangePasswordDTO;

public interface IUserService {
	
	public User save (User user, BCryptPasswordEncoder passwordEncoder) throws MessagingException, IOException;
	public void sendEmail(String email, String asunt, String name) throws MessagingException, IOException;
	public void changePassword(ChangePasswordDTO changePasswordForm, BCryptPasswordEncoder passwordEncoder, Authentication authentication);
	public void saveTest(TestResult testResult);
	public void sendEmailResultTest(String email, String name, String bussinesMail) throws MessagingException, IOException;
	public void sendEmailTeem(String businessMail, String affairBusinessMail, String userEmail) throws MessagingException ;

}
