package search.repository;

import org.springframework.data.repository.CrudRepository;
import search.model.Book;

import java.util.Optional;

public interface BookRepository extends CrudRepository<Book, Long> {
    Optional<Book> findByBookName(String name);

    Optional<Book> findByIsbn(String isbn);
}

