    package com.projecao.moviedata.model;

    import com.fasterxml.jackson.annotation.JsonBackReference;
    import jakarta.persistence.*;
    import lombok.*;

    import java.io.Serial;
    import java.io.Serializable;

    @Entity
    @Table(name = "movie")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public class Movie implements Serializable {
        @Serial
        private static final long serialVersionUID = 2510240878602139982L;

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String title;
        private String director;
        private int year;
        private String genre;


        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        public Movie(String title, String director, int year, String genre, User user) {
            this.title = title;
            this.director = director;
            this.year = year;
            this.genre = genre;
            this.user = user;
        }

        @Override
        public String toString() {
            return "Movie{" +
                    "id=" + id +
                    ", title='" + title + '\'' +
                    ", director='" + director + '\'' +
                    ", year=" + year +
                    ", genre='" + genre + '\'' +
                    ", user=" + user.getId() +
                    '}';
        }
    }
