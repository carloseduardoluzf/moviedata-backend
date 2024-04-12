package com.projecao.moviedata;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.crypto.SecretKey;

@SpringBootApplication
public class MoviedataApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoviedataApplication.class, args);
		SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
		String base64Key = java.util.Base64.getEncoder().encodeToString(key.getEncoded());
		System.out.println("Nova chave gerada: " + base64Key);
	}

}
