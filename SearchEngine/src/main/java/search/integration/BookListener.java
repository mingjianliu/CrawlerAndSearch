package search.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import search.model.Book;
import search.model.BookSearch;
import search.repository.BookRepository;
import search.repository.BookSearchRepository;

import java.util.Optional;

@Component
@Slf4j
@Transactional
public class BookListener {
    @Autowired
    BookRepository bookRepository;
    @Autowired
    BookSearchRepository bookSearchRepository;

    @StreamListener(Crawler.NEW_BOOKS)
    public void processNewBook(Long id) {
        Optional<Book> book = bookRepository.findById(id);
        if(!book.isPresent()) {
            return;
        }
        System.out.println("Mingjianliudebug "+book);
        BookSearch bookSearch = new BookSearch();
        bookSearch.setId(book.get().getId());
        bookSearch.setTitle(book.get().getBookName());
        bookSearch.setTags(book.get().getKeywords());
        bookSearchRepository.save(bookSearch);
    }
}
