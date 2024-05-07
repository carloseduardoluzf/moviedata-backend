package com.projecao.moviedata.dto;

public record MovieDTO(
        Long id,
        String title,
        String director,
        int year,
        String genre,
        Long userId
) {}