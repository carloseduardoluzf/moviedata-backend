package com.projecao.moviedata.controller;

import com.projecao.moviedata.dto.MovieDTO;
import com.projecao.moviedata.model.Movie;
import com.projecao.moviedata.model.User;
import com.projecao.moviedata.repository.MovieRepository;
import com.projecao.moviedata.repository.UserRepository;
import com.projecao.moviedata.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class MovieController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    MovieRepository movieRepository;

    @Autowired
    MovieService movieService;

    @GetMapping("/movies")
    public List<MovieDTO> getAllMovies() {
        List<Movie> movies = movieRepository.findAll();
        return movies.stream()
                .map(movieService::convertToDTO)
                .collect(Collectors.toList());
    }


    @GetMapping("/movies/{id}")
    public ResponseEntity<MovieDTO> findByIdMovie(@PathVariable Long id) {
        Optional<Movie> optionalMovie = movieRepository.findById(id);

        if (optionalMovie.isPresent()) {
            Movie movie = optionalMovie.get();
            MovieDTO movieDTO = new MovieDTO(
                    movie.getId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getYear(),
                    movie.getGenre(),
                    movie.getUser().getId() // Assumindo que userId é o ID do usuário associado ao filme
            );
            return ResponseEntity.ok().body(movieDTO);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Filme não encontrado com o ID: " + id);
        }
    }

    @PostMapping("/movies")
    @Transactional
    public ResponseEntity<?> createMovie(@RequestBody MovieDTO movieDTO) {
        Long userId = getCurrentUserId();

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado");
        }

        // Criar uma nova instância de Movie sem usar a lista de filmes do usuário
        Movie movie = new Movie(
                movieDTO.title(),
                movieDTO.director(),
                movieDTO.year(),
                movieDTO.genre(),
                userOptional.get()
        );

        // Adicionar o novo filme à lista de filmes do usuário manualmente
        userOptional.get().getMovies().add(movie);

        // Salvar o filme no banco de dados
        movieRepository.save(movie);

        // Retornar o filme criado para o cliente
        return ResponseEntity.status(HttpStatus.CREATED).body(movie);
    }

    @DeleteMapping("/movies/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        Long userId = getCurrentUserId();

        Optional<Movie> optionalMovie = movieRepository.findById(id);
        if (optionalMovie.isPresent()) {
            Movie movie = optionalMovie.get();
            if (movie.getUser().getId().equals(userId)) {
                movieRepository.delete(movie);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden
            }
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }

    @PutMapping("/movies/{id}")
    public ResponseEntity<Movie> updateMovie(@PathVariable Long id, @RequestBody MovieDTO movieDTO) {
        Long userId = getCurrentUserId();

        Optional<Movie> optionalMovie = movieRepository.findById(id);
        if (optionalMovie.isPresent()) {
            Movie movie = optionalMovie.get();
            if (movie.getUser().getId().equals(userId)) {
                movie.setTitle(movieDTO.title());
                movie.setDirector(movieDTO.director());
                movie.setYear(movieDTO.year());
                movie.setGenre(movieDTO.genre());
                return ResponseEntity.ok(movieRepository.save(movie));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden
            }
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // Obtém o nome de usuário do Authentication
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            return user.get().getId();
        } else {
            // Se o usuário não for encontrado, você pode lidar com isso de acordo com a sua lógica de negócios.
            throw new UsernameNotFoundException("Usuário não encontrado com o nome: " + email);
        }
    }



}
