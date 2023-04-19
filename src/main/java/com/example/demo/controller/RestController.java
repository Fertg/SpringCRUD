package com.example.demo.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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
					System.out.println("Error--> "+e.getMessage());
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

	@RequestMapping(value = "/usuarios", method = { RequestMethod.GET})
	public List<Map<String, Object>> obtenerUsuarios() {
		String sql = "SELECT * FROM user";
		List<Map<String, Object>> usuarios = jdbcTemplate.queryForList(sql);
		return usuarios;
	}
	
	
	@RequestMapping(value = "/pokemon", method = { RequestMethod.GET})
	public List<Map<String, Object>> obtenerPokemons() {
		String sql = "SELECT numero_pokedex,nombre FROM pokemon";
		List<Map<String, Object>> pokemon = jdbcTemplate.queryForList(sql);
		return pokemon;
	}
	
	@PutMapping("/updateUser")
	public String updateUser(@RequestBody Map<String, Object> data) {

		if (data != null) {
			String email = (String) data.get("email");
			String emailN = (String) data.get("emailN");
			if (validarEmail(email)) {
			try {
				String sql = "UPDATE user SET email = ? WHERE email = ?";
                jdbcTemplate.update(sql, emailN, email);
				
                return "Actualizado";
				} catch (DataAccessException e) {
					System.out.println("Usuario ya existe");
					return "El usuario no existe";
				}
			} else
				return "email no válido";
		} else {
			return null;
		}
	}
	

	@GetMapping("/users/{email}")
	public ResponseEntity<Map<String, Object>> checkUserExists(@PathVariable String email, @RequestParam String password) {
	    String sql = "SELECT COUNT(*) FROM user WHERE email = ? AND password = ?";
	    int count = jdbcTemplate.queryForObject(sql, Integer.class, email, encrypt(password));
	    if (count > 0) {
	        Map<String, Object> response = new HashMap<>();
	        response.put("exists", true);
	        return ResponseEntity.ok(response);
	    } else {
	        return ResponseEntity.notFound().build();
	    }
	}
	
	@GetMapping("/buscarEvolucion")
	   public List<Map<String, Object>> buscarPokemon(@RequestParam("nombre") String nombre) {
		String sql = "select p.nombre from pokemon p where p.numero_pokedex="
				+ "(select ed.pokemon_evolucionado from  evoluciona_de ed  where "
				+ "ed.pokemon_origen=(select p.numero_pokedex from pokemon p "
				+ "where p.nombre=?) );";		
		List<Map<String, Object>> pokemon = jdbcTemplate.queryForList(sql,nombre);	
		  if (pokemon.isEmpty()) {
		        Map<String, Object> mensaje = new HashMap<>();
		        mensaje.put("nombre", "El pokemon no existe o no tiene evoluciones");
		        pokemon.add(mensaje);
		    }
		    return pokemon;
	}
	
	public boolean validarEmail(String email) {
		// Expresión regular para validar correo electrónico
		String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}

}
