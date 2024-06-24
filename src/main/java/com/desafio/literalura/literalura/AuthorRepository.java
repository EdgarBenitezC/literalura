package com.desafio.literalura.literalura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    @Query("SELECT a FROM Author a WHERE ?1 BETWEEN a.birthYear AND a.deathYear OR (a.deathYear IS NULL AND ?1 >= a.birthYear)")
    List<Author> findAuthorsByLivingYear(int year);

    Author findByName(String name);
}