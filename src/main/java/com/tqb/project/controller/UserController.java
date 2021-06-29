package com.tqb.project.controller;

import java.io.IOException;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tqb.project.model.Pdf;
import com.tqb.project.model.TestResult;
import com.tqb.project.model.User;
import com.tqb.project.model.dto.ChangePasswordDTO;
import com.tqb.project.service.DocStorageService;
import com.tqb.project.service.IUserService;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api")
public class UserController {

	public static final String BUSINESS_MAIL = "redestqb@gmail.com";
	public static final String AFFAIR_CONTACT_MAIL = "Validacióm de cuenta TQB";
	public static final String AFFAIR_BUSINESS_MAIL = "Nuevo usuario a confirmar";

	@Autowired
	private DocStorageService docStorageService;

	@Autowired
	private IUserService userService;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@PostMapping("/user")
	@ResponseStatus(HttpStatus.CREATED)
	public void create(@RequestBody User user) throws MessagingException, IOException {
		userService.sendEmail(user.getEmail(), AFFAIR_CONTACT_MAIL, user.getName() );
		userService.sendEmail(BUSINESS_MAIL, AFFAIR_BUSINESS_MAIL, null);
		userService.save(user, passwordEncoder);
	}

	@PostMapping("user/change-password")
	@ResponseStatus(HttpStatus.CREATED)
	public void changePassword(@RequestBody ChangePasswordDTO changePasswordForm, Authentication authentication)
			throws MessagingException, IOException {
		userService.changePassword(changePasswordForm, passwordEncoder, authentication);
	}

	@PostMapping("user/present-proyect")
	@ResponseStatus(HttpStatus.CREATED)
	public void presentProject(@RequestParam(value = "file") MultipartFile file)
			throws MessagingException, IOException {
		docStorageService.saveFile(file);
//	
//	@PostMapping("user/present-proyect")
//	@ResponseStatus(HttpStatus.CREATED)
//	public void presentProject(@RequestParam(value = "file") MultipartFile file) throws MessagingException, IOException {
//		docStorageService.saveFile(file);
//	}

	}

	@GetMapping("user/download-PDF/{fileId}")
	public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable Long fileId) {
		Pdf pdf = docStorageService.getFile(fileId);
		return ResponseEntity.ok().contentType(MediaType.parseMediaType(pdf.getDocType()))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + pdf.getDocName() + "\"")
				.body(new ByteArrayResource(pdf.getData()));
	}
	
	@PostMapping("user/result-test")
	@ResponseStatus(HttpStatus.CREATED)
	public void resulTest(@RequestBody TestResult testResult)
			throws MessagingException, IOException {
		userService.saveTest(testResult);
		userService.sendEmailResultTest(BUSINESS_MAIL,testResult.getEmail(), testResult.getName());
	}

}
