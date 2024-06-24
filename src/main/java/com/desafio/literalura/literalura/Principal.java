package com.desafio.literalura.literalura;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class Principal implements CommandLineRunner {

    private static final String API_URL = "https://gutendex.com/books/";
    private static final String SEARCH_URL = API_URL + "?search=";
    private List<Book> catalog = new ArrayList<>();
    private Scanner scanner = new Scanner(System.in);

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Override
    public void run(String... args) {
        displayMenu();
    }

    private void displayMenu() {
        while (true) {
            System.out.println("======= Menú =======");
            System.out.println("1. Buscar libro por título");
            System.out.println("2. Listar todos los libros");
            System.out.println("3. Filtrar libros por idioma");
            System.out.println("4. Listar autores de libros buscados");
            System.out.println("5. Listar autores vivos en un año específico");
            System.out.println("6. Mostrar cantidad de libros en un idioma específico");
            System.out.println("0. Salir");

            System.out.print("Selecciona una opción: ");
            int option = scanner.nextInt();
            scanner.nextLine(); // Consume la nueva línea después de nextInt()

            switch (option) {
                case 1:
                    searchBookByTitle();
                    break;
                case 2:
                    listAllBooks();
                    break;
                case 3:
                    filterBooksByLanguage();
                    break;
                case 4:
                    listAuthors();
                    break;
                case 5:
                    listLivingAuthorsInYear();
                    break;
                case 6:
                    showBookCountByLanguage();
                    break;
                case 0:
                    System.out.println("Saliendo del programa.");
                    return;
                default:
                    System.out.println("Opción no válida. Por favor, selecciona una opción válida.");
            }
        }
    }

    private void searchBookByTitle() {
        System.out.print("Introduce el título del libro a buscar: ");
        String title = scanner.nextLine().trim();

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SEARCH_URL + title))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                parseAndSaveBook(response.body());
            } else {
                System.out.println("Error al buscar el libro. Código de estado: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Error al buscar el libro: " + e.getMessage());
        }
    }

    private void parseAndSaveBook(String responseBody) {
        try {
            List<Book> books = new ArrayList<>();

            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);

            JsonNode resultsNode = root.path("results");
            if (resultsNode.isArray()) {
                for (JsonNode bookNode : resultsNode) {
                    Book book = new Book();
                    book.setTitle(bookNode.path("title").asText());
                    book.setDownloadCount(bookNode.path("formats").size()); // Ejemplo de atributo, ajustar según la respuesta API

                    // Obtener información del autor (asumiendo un autor por libro para simplicidad)
                    JsonNode authorsNode = bookNode.path("authors");
                    if (authorsNode.isArray() && authorsNode.size() > 0) {
                        JsonNode authorNode = authorsNode.get(0);
                        Author author = new Author();
                        author.setName(authorNode.path("name").asText());
                        // Agregar más atributos como año de nacimiento, año de fallecimiento si están disponibles en la respuesta API

                        // Guardar autor en la base de datos (verificar si ya existe)
                        Author savedAuthor = authorRepository.findByName(author.getName());
                        if (savedAuthor == null) {
                            savedAuthor = authorRepository.save(author);
                        }
                        book.setAuthor(savedAuthor);
                    }

                    books.add(book);
                }
            }

            // Guardar libros en la base de datos
            bookRepository.saveAll(books);
            System.out.println("Libros guardados en la base de datos.");

        } catch (Exception e) {
            System.err.println("Error al procesar y guardar el libro: " + e.getMessage());
        }
    }

    private void listAllBooks() {
        List<Book> books = bookRepository.findAll();
        if (books.isEmpty()) {
            System.out.println("No hay libros en la base de datos.");
        } else {
            System.out.println("Listado de todos los libros:");
            for (Book book : books) {
                System.out.println("Título: " + book.getTitle());
                System.out.println("Autor: " + book.getAuthor().getName());
                // Mostrar más atributos según sea necesario
                System.out.println();
            }
        }
    }

    private void filterBooksByLanguage() {
        // Implementar según necesidades
    }

    private void listAuthors() {
        List<Book> books = bookRepository.findAll();
        if (books.isEmpty()) {
            System.out.println("No hay libros en la base de datos.");
        } else {
            System.out.println("Listado de autores de libros buscados:");
            for (Book book : books) {
                System.out.println("Autor: " + book.getAuthor().getName());
            }
        }
    }

    private void listLivingAuthorsInYear() {
        System.out.print("Introduce el año para listar autores vivos: ");
        int year = scanner.nextInt();
        scanner.nextLine(); // Consume la nueva línea después de nextInt()

        List<Author> authors = authorRepository.findAuthorsByLivingYear(year);

        if (authors.isEmpty()) {
            System.out.println("No se encontraron autores vivos en el año especificado.");
        } else {
            System.out.println("Autores vivos en el año " + year + ":");
            for (Author author : authors) {
                System.out.println("Autor: " + author.getName());
            }
        }
    }

    private void showBookCountByLanguage() {
        System.out.print("Introduce el idioma (en minúsculas) para consultar la cantidad de libros: ");
        String language = scanner.nextLine().trim();

        long bookCount = bookRepository.countBooksByLanguage(language);

        if (bookCount == 0) {
            System.out.println("No se encontraron libros en el idioma especificado.");
        } else {
            System.out.println("Cantidad de libros en el idioma '" + language + "': " + bookCount);
        }
    }
}