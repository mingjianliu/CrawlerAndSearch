package crawler.repository;

import crawler.model.Book;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface BookRepository extends CrudRepository<Book, Long> {
    Optional<Book> findByBookName(String name);
    Optional<Book> findByIsbn(String isbn);
}

