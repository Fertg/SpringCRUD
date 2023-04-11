package com.example.demo.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@org.springframework.web.bind.annotation.RestController
public class RestController {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@PostMapping("/insertarUsuario")
	public String insertarUsuario(@RequestBody Map<String, Object> data) {

		if (data != null) {
			String email = (String) data.get("email");
			if (validarEmail(email)) {
				String password = (String) data.get("password");
				try {
					String passwordS256 = encrypt(password);// encripto la pass
					String sql = "INSERT INTO user (email, password) VALUES (?, ?)";
					jdbcTemplate.update(sql, email, passwordS256);
					System.out.println("Usuario insertado correctamente en la base de datos");
					return "Usuario insertado correctamente en la base de datos.";

				} catch (DataAccessException e) {
					System.out.println("Usuario ya existe");
					return "El usuario ya existe";
				}
			} else
				return "email no válido";
		} else {
			return null;
		}
	}

	private String encrypt(String password) {

		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");

			byte[] hashInBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));

			// Convertir el hash a una cadena hexadecimal
			StringBuilder sb = new StringBuilder();
			for (byte b : hashInBytes) {
				sb.append(String.format("%02x", b));
			}

			String passwordS256 = sb.toString();
			return passwordS256;
		} catch (NoSuchAlgorithmException e) {
			System.out.println(e.getMessage());
			return password;

		}
	}

	@PostMapping("/deleteUsuario")
	public String deleteUsuario(@RequestBody Map<String, Object> data) {

		try {
			String email = (String) data.get("email");
			String sql = "DELETE FROM user WHERE email = ?";
			jdbcTemplate.update(sql, email);

			System.out.println("Usuario Borrado");
			return "Usuario Borrado: " + email;
		} catch (DataAccessException e) {
			System.out.println("El usuario no existe");
			return "El usuario no existe";
		}
	}

	@RequestMapping(value = "/usuarios", method = { RequestMethod.GET, RequestMethod.POST })
	public List<Map<String, Object>> obtenerUsuarios(Model model) {
		String sql = "SELECT * FROM user";
		List<Map<String, Object>> usuarios = jdbcTemplate.queryForList(sql);
		return usuarios;
	}

	public boolean validarEmail(String email) {
		// Expresión regular para validar correo electrónico
		String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
		// Compilar la expresión regular en un patrón
		Pattern pattern = Pattern.compile(regex);
		// Crear un objeto Matcher con el correo electrónico a validar
		Matcher matcher = pattern.matcher(email);
		// Retornar verdadero si la expresión regular coincide con el correo electrónico
		return matcher.matches();
	}

}
