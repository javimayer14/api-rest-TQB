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
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
		User lastInsert = usuarioDao.save(user);
		usuarioDao.saveUserRole(lastInsert.getId(), 1);
		return lastInsert;

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
	                + "<h3> Te contamos que tu solicitud para ser miembro de The Quality Bridge está siendo evaluada por nuestro equipo.</h3>"
	                + "<h3> En caso de aceptarse la solicitud te llegará un correo en las próximas horas con la bienvenida y más información. </h3>"
	                + "<h3> Para nosotros es muy importante saber que quienes forman parte de TQB sean inversores reales interesados en conectarse para invertir mejor.</h3>"
	                + "<h3> Desde ya agradecemos tu interés en participar de la comunidad de inversores que te facilita los puentes para expandir tus oportunidades.</h3>"
	                + "</div>"
	                + "<div>Saludos,</div>"
	                + "El equipo de inversores de The Quality Bridge \n"
	              + "</div></body>"
	            + "</html>", true);
	        helper.addInline("rightSideImage",
	                new File("src/main/resources/img/ENCABEZADO_TQB_GENERAL.jpg"));

	 
		// helper.addAttachment("ENCABEZADO_TQB_GENERAL.jpg", new ClassPathResource("android.png"));
		javaMailSender.send(msg);
	}
	
	public void sendEmailResultTest(String email, String name) throws MessagingException, IOException {
		
		MimeMessage msg = javaMailSender.createMimeMessage();

		// true = multipart message
		MimeMessageHelper helper = new MimeMessageHelper(msg, true);
		helper.setTo(email);
		helper.setSubject("Nuevo test finalizado");
		helper.setText( "<html>"
	            + "<body>"
                + "<img src='cid:rightSideImage' style='align-content: center;width:1000px;height:200px;'/>"
	                + "<div>"
	                + "<h3> Un nuevo usuario con email "+ email +" realizó el test </h3>"
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
