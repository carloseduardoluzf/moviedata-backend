package com.projecao.moviedata.service;

import com.projecao.moviedata.dto.MovieDTO;
import com.projecao.moviedata.model.Movie;
import org.springframework.stereotype.Service;
@Service
public class MovieService {

    public MovieDTO convertToDTO(Movie movie) {
        return new MovieDTO(
                movie.getId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getYear(),
                movie.getGenre(),
                movie.getUser().getId()
        );
    }
}
