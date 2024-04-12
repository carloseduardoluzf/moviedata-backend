package com.projecao.moviedata.controller;

import com.projecao.moviedata.dto.LoginRequest;
import com.projecao.moviedata.dto.RegisterRequest;
import com.projecao.moviedata.repository.UserRepository;
import com.projecao.moviedata.service.JwtUserDetailsService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Value("${jwt.secret}")
    private String secret;



    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Encontra o usuário no banco de dados pelo email
            UserDetails userDetails = jwtUserDetailsService.loadUserByEmail(loginRequest.email());

            // Verifica se o usuário existe e se a senha está correta
            if (!passwordEncoder.matches(loginRequest.password(), userDetails.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário ou senha inválidos.");
            }

            // Gera o token JWT
            String token = Jwts.builder()
                    .setSubject(loginRequest.email())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 3600))
                    .signWith(SignatureAlgorithm.HS512, secret)
                    .compact();

            // Retorna o token JWT junto com a resposta
            return ResponseEntity.ok(Map.of("token", token));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou senha inválidos.");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> saveUser(@RequestBody RegisterRequest registerRequest) {
        try {
            jwtUserDetailsService.save(registerRequest);
            return ResponseEntity.ok("Usuário cadastrado com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
