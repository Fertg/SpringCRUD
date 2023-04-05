package com.example.demo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.web.bind.annotation.RestController
public class RestController {


	@Autowired
	private JdbcTemplate jdbcTemplate;

	@PostMapping("/insertarUsuario")
	public String insertarUsuario(@RequestBody Map<String, Object> data)  {

		try {
		String email = (String) data.get("email");
		String password = (String) data.get("password");
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashInBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));

			// Convertir el hash a una cadena hexadecimal
			StringBuilder sb = new StringBuilder();
			for (byte b : hashInBytes) {
				sb.append(String.format("%02x", b));
			}

			String passwordS256=sb.toString();

			String sql = "INSERT INTO user (email, password) VALUES (?, ?)";
			jdbcTemplate.update(sql, email, passwordS256);


		} catch (NoSuchAlgorithmException e) {
			System.out.println("Password no encriptada: "+e.getMessage()); 
			return password;
		}


		return "Usuario insertado correctamente en la base de datos.";
		}catch(DataAccessException e) {
			
			return"El usuario ya existe";	
		}
	}

}