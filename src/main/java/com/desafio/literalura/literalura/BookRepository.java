package com.desafio.literalura.literalura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("SELECT COUNT(b) FROM Book b WHERE LOWER(b.language) = LOWER(?1)")
    long countBooksByLanguage(String language);
}