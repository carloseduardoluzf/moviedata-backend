package com.projecao.moviedata.controller;

import com.projecao.moviedata.dto.ChangePasswordDTO;
import com.projecao.moviedata.dto.LoginRequest;
import com.projecao.moviedata.dto.RegisterRequest;
import com.projecao.moviedata.model.User;
import com.projecao.moviedata.repository.UserRepository;
import com.projecao.moviedata.service.JwtUserDetailsService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

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
            UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(loginRequest.email());

            // Verifica se o usuário existe e se a senha está correta
            if (!passwordEncoder.matches(loginRequest.password(), userDetails.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário ou senha inválidos.");
            }

            Long userId = getUserIdByEmail(loginRequest.email());
            // Gera o token JWT
            String token = Jwts.builder()
                    .setSubject(loginRequest.email())
                    .claim("email", userDetails.getUsername())
                    .claim("id", userId)
                    .setIssuedAt(Date.from(Instant.now()))
                    .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                    .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                    .compact();

            // Retorna o token JWT junto com a resposta
            return ResponseEntity.ok(Map.of("token", token));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou senha inválidos.");
        }
    }

    public Long getUserIdByEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            return userOptional.get().getId();
        } else {
            throw new UsernameNotFoundException("Usuário não encontrado com o email: " + email);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> saveUser(@RequestBody RegisterRequest registerRequest) {
        try {
            jwtUserDetailsService.save(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/user")
    public ResponseEntity<?> deleteUser() {
        // Obtenha o email do usuário atualmente autenticado no contexto de segurança
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // Encontre o usuário atual no banco de dados com base no email
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Delete o usuário atual do banco de dados
        userRepository.delete(currentUser);

        return ResponseEntity.ok("Usuário deletado com sucesso.");
    }


    /*@PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado");
        }

        // Verifica se a senha antiga está correta
        if (!passwordEncoder.matches(changePasswordDTO.oldPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Senha antiga incorreta");
        }

        // Atualiza a senha do usuário
        user.setPassword(passwordEncoder.encode(changePasswordDTO.newPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("Senha alterada com sucesso");
    }*/


    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "Usuário não encontrado"));
        }

        // Verifica se a senha antiga está correta
        if (!passwordEncoder.matches(changePasswordDTO.oldPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Senha antiga incorreta"));
        }

        // Atualiza a senha do usuário
        user.setPassword(passwordEncoder.encode(changePasswordDTO.newPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(Collections.singletonMap("message", "Senha alterada com sucesso"));
    }


    @GetMapping("/user/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        // Busca o usuário pelo ID no banco de dados
        User user = userRepository.findById(id).orElse(null);

        // Verifica se o usuário foi encontrado
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado");
        }
    }
}
