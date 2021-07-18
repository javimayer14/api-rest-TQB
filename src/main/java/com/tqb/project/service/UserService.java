package com.tqb.project.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tqb.project.dao.ITestDao;
import com.tqb.project.dao.IUserDao;
import com.tqb.project.model.TestResult;
import com.tqb.project.model.User;
import com.tqb.project.model.dto.ChangePasswordDTO;

@Service
public class UserService implements UserDetailsService, IUserService {
	
	private static final String USER_NOT_FOUND = "No se encuentra el usuario";
	private static final String DATA_INTEGRITY = "usuario ya creado";


	private Logger logger = LoggerFactory.getLogger(IUserService.class);
	@Autowired
	private IUserDao usuarioDao;
	@Autowired
	private JavaMailSender javaMailSender;
	@Autowired
	private ITestDao testDao;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User usuario = usuarioDao.findByUsername(username);

		if (usuario == null) {
			logger.error("eerror no existe el usuario '" + username + " en el sistema !'");
			throw new UsernameNotFoundException("Error en el login: no existe el usuario:'" + username + "'");
		}

		List<GrantedAuthority> authorities = usuario.getRole().stream()
				.map(role -> new SimpleGrantedAuthority(role.getName()))
				.peek(authority -> logger.info("Role" + authority.getAuthority())).collect(Collectors.toList());

		return new org.springframework.security.core.userdetails.User(usuario.getUsername(), usuario.getPassword(),
				usuario.getEnabled(), true, true, true, authorities);
	}

	@Override
	public User save(User user, BCryptPasswordEncoder passwordEncoder) {
		String passwordBcrypt = passwordEncoder.encode("12345");
		user.setPassword(passwordBcrypt);
		user.setUsername(user.getEmail());
		user.setEnabled(true);
		user.setValidate(false);
		try {
			User lastInsert = usuarioDao.save(user);
			usuarioDao.saveUserRole(lastInsert.getId(), 1);
			return lastInsert;
			
		}catch(DataIntegrityViolationException ex) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, DATA_INTEGRITY);
		}

	}

	public void sendEmail(String email, String asunt, String name) throws MessagingException, IOException {
		
		MimeMessage msg = javaMailSender.createMimeMessage();

		// true = multipart message
		MimeMessageHelper helper = new MimeMessageHelper(msg, true);
		helper.setTo(email);
		helper.setSubject(asunt);
		helper.setText( "<html>"
	            + "<body>"
                + "<img src='cid:rightSideImage' style='align-content: center;width:1000px;height:200px;'/>"
	             + "<div><h2>Hola "+name+"</h2>"
	                + "<div>"
	                + "<h4> Te contamos que tu solicitud para ser miembro de The Quality Bridge está siendo evaluada por nuestro equipo.</h4>"
	                + "<h4> En caso de aceptarse la solicitud te llegará un correo en las próximas horas con la bienvenida y más información. </h4>"
	                + "<h4> Para nosotros es muy importante saber que quienes forman parte de TQB sean inversores reales interesados en conectarse para invertir mejor.</h4>"
	                + "<h4> Desde ya agradecemos tu interés en participar de la comunidad de inversores que te facilita los puentes para expandir tus oportunidades.</h4>"
	                + "</div>"
	                + "<div>Síguenos en nuestras redes para mantenerte conectado y en movimiento</div>"
	                + "<a href=\"https://www.facebook.com/thequalitybridge\">Facebook</a>\n"
	                + "<a href=\"https://www.instagram.com/thequalitybridge\">Instagram</a>\n"
	                + "<a href=\"https://www.linkedin.com/company/thequalitybridge\">Linkedin</a>\n"
	                + "<br>"
	                + "<div>Saludos,</div>"
	                + "The Quality Bridge \n"
	              + "</div>"
	              + "</body>"
	            + "</html>", true);
	        helper.addInline("rightSideImage",
	                new File("src/main/resources/img/ENCABEZADO_TQB_GENERAL.jpg"));

	 
		// helper.addAttachment("ENCABEZADO_TQB_GENERAL.jpg", new ClassPathResource("android.png"));
		javaMailSender.send(msg);
	}
	
	public void sendEmailResultTest(String bussinesEmail, String email, String name) throws MessagingException, IOException {
		
		MimeMessage msg = javaMailSender.createMimeMessage();

		// true = multipart message
		MimeMessageHelper helper = new MimeMessageHelper(msg, true);
		helper.setTo(bussinesEmail);
		helper.setSubject("Nuevo test finalizado");
		helper.setText( "<html>"
	            + "<body>"
                + "<img src='cid:rightSideImage' style='align-content: center;width:1000px;height:200px;'/>"
	                + "<div>"
	                + "<h4> Un nuevo usuario con email "+ email +" realizó el test </h4>"
	                + "</div>"
	                + "</body>"
	            + "</html>", true);
	        helper.addInline("rightSideImage",
	                new File("src/main/resources/img/ENCABEZADO_TQB_GENERAL.jpg"));

	 
		// helper.addAttachment("ENCABEZADO_TQB_GENERAL.jpg", new ClassPathResource("android.png"));
		javaMailSender.send(msg);
	}

	@Override
	public void changePassword(ChangePasswordDTO user, BCryptPasswordEncoder passwordEncoder, Authentication authentication) {
		try {
			String username  = authentication.getName();
			User currentUser = usuarioDao.findByUsername(username);
			String passwordBcrypt = passwordEncoder.encode(user.getNewPassword());
			currentUser.setPassword(passwordBcrypt);
			usuarioDao.save(currentUser);
		}catch(NullPointerException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND);
		}

	}

	@Override
	public void saveTest(TestResult testResult) {
		testDao.save(testResult);
		
	}
	
//	@Scheduled(fixedDelay = 5400000)
	@Scheduled(fixedDelay = 200000)
	public void validateUser() throws MessagingException {
		System.out.println("prueba cron");
		List<User> usersNotValidated = usuarioDao.findNotValidated();
		for(User u : usersNotValidated) {
			u.setValidate(true);
			sendEmailvalidation(u.getEmail());
		}
		usuarioDao.saveAll(usersNotValidated);
	}
	
	private void sendEmailvalidation(String mail) throws MessagingException {
		if(mail == null)
			return;
		
		MimeMessage msg = javaMailSender.createMimeMessage();

		// true = multipart message
		MimeMessageHelper helper = new MimeMessageHelper(msg, true);
		helper.setTo(mail);
		helper.setSubject("Bienvenido a la comunidad de inversores. ¡Ya eres miembro!");
		helper.setText( "<html>"
	            + "<body>"
                + "<img src='cid:rightSideImage' style='align-content: center;width:1000px;height:200px;'/>"
	                + "<div>"
	                + "<h4> Felicitaciones, su cuenta fué validada con éxito</h4>"
	                + "</div>"
	                + "<div>Síguenos en nuestras redes para mantenerte conectado y en movimiento</div>"
	                + "<a href=\"https://www.facebook.com/thequalitybridge\">Facebook</a>\n"
	                + "<a href=\"https://www.instagram.com/thequalitybridge\">Instagram</a>\n"
	                + "<a href=\"https://www.linkedin.com/company/thequalitybridge\">Linkedin</a>\n"
	                + "<div>Saludos,</div>"
	                + "The Quality Bridge \n"
	                + "</body>"
	            + "</html>", true);
	        helper.addInline("rightSideImage",
	                new File("src/main/resources/img/ENCABEZADO_TQB_GENERAL.jpg"));

	        javaMailSender.send(msg);
	}

	@Override
	public void sendEmailTeem(String businessMail, String affairBusinessMail, String userEmail) throws MessagingException {
		MimeMessage msg = javaMailSender.createMimeMessage();

		// true = multipart message
		MimeMessageHelper helper = new MimeMessageHelper(msg, true);
		helper.setTo(businessMail);
		helper.setSubject(affairBusinessMail);
		helper.setText( "<html>"
	            + "<body>"
                + "<img src='cid:rightSideImage' style='align-content: center;width:1000px;height:200px;'/>"
	                + "<div>"
	                + "<h4> Un nuevo usuario con email "+ userEmail +" se acaba de sumar al equipo de tqb </h4>"
	                + "</div>"
	                + "</body>"
	            + "</html>", true);
	        helper.addInline("rightSideImage",
	                new File("src/main/resources/img/ENCABEZADO_TQB_GENERAL.jpg"));

	 
		// helper.addAttachment("ENCABEZADO_TQB_GENERAL.jpg", new ClassPathResource("android.png"));
		javaMailSender.send(msg);
		
	}

//    public void sendEmail() {
//
//        SimpleMailMessage msg = new SimpleMailMessage();
//        msg.setTo("javimayer14@gmail.com");
//
//        msg.setSubject("Validacióm de cuenta TQB");
//        msg.setText("Buenas su cuenta esta siendo evaluada \n "
//        		+ "En breve nos comunicaremos");
//
//        javaMailSender.send(msg);
//
//    }

}
