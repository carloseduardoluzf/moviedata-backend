package com.projecao.moviedata.repository;

import com.projecao.moviedata.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}
