package com.tqb.project.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
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
import com.tqb.project.model.dto.ContactProyectDTO;

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

		} catch (DataIntegrityViolationException ex) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, DATA_INTEGRITY);
		}

	}

	public void sendEmail(String email, String asunt, String name) throws MessagingException, IOException {

		MimeMessage msg = javaMailSender.createMimeMessage();

		// true = multipart message
		MimeMessageHelper helper = new MimeMessageHelper(msg, true);
		helper.setTo(email);
		helper.setFrom(new InternetAddress("equipo@thequalitybridge.com"));
		helper.setSubject(asunt);
		helper.setText("<html>" + "<body>"
				+ "<img src='cid:rightSideImage' style='align-content: center;width:1000px;height:200px;'/>"
				+ "<div><h2>Hola " + name + "</h2>" + "<div>"
				+ "<h4> Te contamos que tu solicitud para ser miembro de The Quality Bridge está siendo evaluada por nuestro equipo.</h4>"
				+ "<h4> En caso de aceptarse, te llegará un correo en las próximas horas con la bienvenida y más información. </h4>"
				+ "<h4> Para nosotros es muy importante saber que quienes forman parte de TQB sean inversores reales interesados en conectarse para invertir mejor.</h4>"
				+ "<h4> Desde ya agradecemos tu interés en participar de la comunidad de inversores que te facilita los puentes para expandir tus oportunidades.</h4>"
				+ "</div>" + "<div>Síguenos en nuestras redes para mantenerte conectado y en movimiento</div>"
				+ "<a href=\"https://www.facebook.com/thequalitybridge\">Facebook</a>\n"
				+ "<a href=\"https://www.instagram.com/thequalitybridge\">Instagram</a>\n"
				+ "<a href=\"https://www.linkedin.com/company/thequalitybridge\">Linkedin</a>\n" + "<br>"
				+ "<div>Saludos,</div>" + "The Quality Bridge \n" + "</div>" + "</body>" + "</html>", true);
		helper.addInline("rightSideImage", new File("src/main/resources/img/ENCABEZADO_TQB_GENERAL.jpg"));

		// helper.addAttachment("ENCABEZADO_TQB_GENERAL.jpg", new
		// ClassPathResource("android.png"));
		javaMailSender.send(msg);
	}

	public void sendEmailResultTest(String bussinesEmail, String email, String name)
			throws MessagingException, IOException {

		MimeMessage msg = javaMailSender.createMimeMessage();

		// true = multipart message
		MimeMessageHelper helper = new MimeMessageHelper(msg, true);
		helper.setTo(bussinesEmail);
		helper.setFrom(new InternetAddress("equipo@thequalitybridge.com"));
		helper.setSubject("Nuevo test finalizado");
		helper.setText("<html>" + "<body>"
				+ "<img src='cid:rightSideImage' style='align-content: center;width:1000px;height:200px;'/>" + "<div>"
				+ "<h4> Un nuevo usuario con email " + email + " realizó el test </h4>" + "</div>" + "</body>"
				+ "</html>", true);
		helper.addInline("rightSideImage", new File("src/main/resources/img/ENCABEZADO_TQB_GENERAL.jpg"));

		// helper.addAttachment("ENCABEZADO_TQB_GENERAL.jpg", new
		// ClassPathResource("android.png"));
		javaMailSender.send(msg);
	}

	@Override
	public void changePassword(ChangePasswordDTO user, BCryptPasswordEncoder passwordEncoder,
			Authentication authentication) {
		try {
			String username = authentication.getName();
			User currentUser = usuarioDao.findByUsername(username);
			String passwordBcrypt = passwordEncoder.encode(user.getNewPassword());
			currentUser.setPassword(passwordBcrypt);
			usuarioDao.save(currentUser);
		} catch (NullPointerException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND);
		}

	}

	@Override
	public void saveTest(TestResult testResult) {
		testDao.save(testResult);

	}

	@Scheduled(fixedDelay = 5400000)
	public void validateUser() throws MessagingException {
		System.out.println("prueba cron");
		List<User> usersNotValidated = usuarioDao.findNotValidated();
		BCryptPasswordEncoder passwordEncode = new BCryptPasswordEncoder();
		if (usersNotValidated != null) {
			for (User u : usersNotValidated) {
				Integer randomInt = getRandomNumber(10000, 99999);
				String randomStringNumber = randomInt.toString();
				u.setPassword(passwordEncode.encode(randomStringNumber));
				u.setValidate(true);
				sendEmailvalidation(u.getEmail(), u.getName(), randomStringNumber);
				System.out.println("La contra generada es: " + randomStringNumber);
			}
			usuarioDao.saveAll(usersNotValidated);
		}
	}

	private void sendEmailvalidation(String mail, String name, String randomStringNumber) throws MessagingException {
		if (mail == null)
			return;

		MimeMessage msg = javaMailSender.createMimeMessage();

		// true = multipart message
		MimeMessageHelper helper = new MimeMessageHelper(msg, true);
		helper.setTo(mail);
		helper.setFrom(new InternetAddress("equipo@thequalitybridge.com"));
		helper.setSubject("Bienvenido a la comunidad de inversores. ¡Ya eres miembro!");
		helper.setText("<html>" + "<body>"
				+ "<img src='cid:rightSideImage' style='align-content: center;width:1000px;height:200px;'/>" + "<div>"
				+ "<h2> Hola " + name + "</h2><br>" + "<br>"
				+ "Es un gusto darte la bienvenida a la comunidad de inversores que te facilita los puentes para expandir tus oportunidades. <br>"
				+ "<br>"
				+ "Antes de contarte más, por estar dentro de los 1000 primeros miembros de la comunidad, te invitamos a descargarte el material exclusivo que te prometimos: <br>"
				+ "Key Steps: Cómo dejar de ser ahorrista y transformarse en inversor. <br>"
				+ "Haz click<a href=\"http://www.thequalitybridgeblog.com/KeySteps.pdf\" target=\"_blank\"> aquí</a> para descargarlo.<br>"
				+ "<br>"
				+ "The Quality Bridge está conformada por inversores de habla hispana de todo el mundo que tienen el mismo objetivo: Realizar inversiones exitosas. <br>"
				+ "<br>" + "¿Cómo lo logramos? Conectados y en movimiento. <br>" + "<br>"
				+ "Ser miembro te va a permitir: <br>" + "<br>"
				+ "Acceder a la información completa para invertir en los proyectos.<br>"
				+ "Recibir contenido exclusivo para mejorar tus habilidades como inversor <br>"
				+ "Asegurar tu lugar en seminarios y workshops con referentes del mercado. <br>"
				+ "Prioridad para obtener informes y análisis de mercado para tomar mejores decisiones<br>"
				+ "Tener la posibilidad de compartir proyectos a todos los miembros <br>" + "<br>" + "<br>"
				+ "Como miembro registrado ahora podrás consultar y solicitar la información completa de los proyectos de inversión. Ya los viste? ¡Haz click<a href=\"https://thequalitybridge.com/#proyectos\" target=\"_blank\"> aquí</a>\n"
				+ " para conocerlos!   <br>" + "<br>"
				+ "The Quality Blog: Con frecuencia semanal podrás ver notas, recomendaciones, consejos y análisis, escritos por nuestro equipo de profesionales y reconocidos periodistas de los medios más importantes. Sobre distintos temas: Economía, Mercado, Real Estate, Finanzas y mucho más. Haz click<a href=\"https://www.thequalitybridgeblog.com/\" target=\"_blank\"> aquí</a>\n"
				+ " para ingresar. <br>" + "<br>" + "PRÓXIMAMENTE <br>" + "<br>"
				+ "Ciclo de Webinars: En muy poco tiempo presentaremos ciclos de webinars para ampliar tu visión como inversor. ¡Estate atento! <br>"
				+ "<br>"
				+ "TQB Flash Report: Estamos preparando algo que te va acompañar todas las semanas y que tendrá impacto en tus evaluaciones de mercado y al momento de decidir en qué invertir. <br>"
				+ "<br>"
				+ "Si deseas saber más o tienes alguna duda sobre la comunidad de inversores, puedes contactarte con nosotros respondiendo este correo. <br>"
				+ "<br>" + "Es un honor contar contigo en TQB. <br>" + "<br>" + "Saludos! <br>" + "<br>"
				+ "El equipo de inversores de The Quality Bridge <br>" + "" + "</h4>" + "</div>"
				+ "<div>Síguenos en nuestras redes para mantenerte conectado y en movimiento</div>"
				+ "<a href=\"https://www.facebook.com/thequalitybridge\">Facebook</a>\n"
				+ "<a href=\"https://www.instagram.com/thequalitybridge\">Instagram</a>\n"
				+ "<a href=\"https://www.linkedin.com/company/thequalitybridge\">Linkedin</a>\n" + "<div>Saludos,</div>"
				+ "The Quality Bridge \n" + "</body>" + "</html>", true);
		helper.addInline("rightSideImage", new File("src/main/resources/img/ENCABEZADO_TQB_WELCOME.jpg"));

		javaMailSender.send(msg);
	}

	@Override
	public void sendEmailTeem(String businessMail, String affairBusinessMail, String userEmail)
			throws MessagingException {
		MimeMessage msg = javaMailSender.createMimeMessage();

		// true = multipart message
		MimeMessageHelper helper = new MimeMessageHelper(msg, true);
		helper.setTo(businessMail);
		helper.setFrom(new InternetAddress(businessMail));
		helper.setSubject(affairBusinessMail);
		helper.setText("<html>" + "<body>"
				+ "<img src='cid:rightSideImage' style='align-content: center;width:1000px;height:200px;'/>" + "<div>"
				+ "<h4> Un nuevo usuario con email " + userEmail + " se acaba de sumar al equipo de tqb </h4>"
				+ "</div>" + "</body>" + "</html>", true);
		helper.addInline("rightSideImage", new File("src/main/resources/img/ENCABEZADO_TQB_GENERAL.jpg"));

		// helper.addAttachment("ENCABEZADO_TQB_GENERAL.jpg", new
		// ClassPathResource("android.png"));
		javaMailSender.send(msg);

	}

	public int getRandomNumber(int min, int max) {
		return (int) ((Math.random() * (max - min)) + min);
	}

	@Override
	public void sendEmailContactProyect(String businessMail, ContactProyectDTO contactProyectDTO)
			throws MessagingException {
		MimeMessage msg = javaMailSender.createMimeMessage();

		// true = multipart message
		MimeMessageHelper helper = new MimeMessageHelper(msg, true);
		helper.setTo(businessMail);
		helper.setFrom(new InternetAddress(businessMail));
		helper.setSubject("Un usuario está interesado en el proyecto");
		helper.setText("<html>" + "<body>"
				+ "<img src='cid:rightSideImage' style='align-content: center;width:1000px;height:200px;'/>" + "<div>"
				+ "<h4> Un nuevo usuario está interesado en el proyecto, a continuación se detallan sus datos de contacto: <br>"
				+ "Nombre:  " + contactProyectDTO.getName() + "<br>" + "Apellido: " + contactProyectDTO.getLastName()
				+ "<br>" + "Mail: " + contactProyectDTO.getMail() + "<br>" + "Telefono: "
				+ contactProyectDTO.getTelefono() + "<br></h4>" + "</div>" + "</body>" + "</html>", true);

		javaMailSender.send(msg);
	}

	@Override
	public void saveUserProyectContact(ContactProyectDTO contactProyectDTO) {
		User user = new User();
		user.setEmail(contactProyectDTO.getMail());
		user.setName(contactProyectDTO.getName());
		user.setLastname(contactProyectDTO.getLastName());
		user.setPhone(contactProyectDTO.getTelefono());
		usuarioDao.save(user);
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
