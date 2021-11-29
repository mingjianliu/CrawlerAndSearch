package crawler.service;

import crawler.integration.Search;
import crawler.model.Book;
import crawler.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@CacheConfig(cacheNames = "BookCache")
public class BookService {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private Search search;

    @Cacheable
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    @Cacheable
    public Optional<Book> getBookByName(String name) {
        return bookRepository.findByBookName(name);
    }

    @Cacheable
    public Optional<Book> getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }

    @Cacheable
    public List<Book> getAllBook() {
        Iterable<Book> it = bookRepository.findAll();
        List<Book> result = new ArrayList<Book>();
        it.forEach(result::add);
        return result;
    }

    public void updateBook(Map<String, Object> payload, Book book) {
        book.setUpdateTime(new Date(System.currentTimeMillis()));
        book.setBookName((String) payload.get("title"));
        double r = getBookRating((String) payload.get("html"));
        book.setRating(r);
        book.setIsbn((String) payload.get("book:isbn"));
        book.setAuthor((String) payload.getOrDefault("book:author", ""));
        book.setImage((String) payload.getOrDefault("og:image", ""));
        book.setDescription((String) payload.getOrDefault("description", ""));
        book.setKeywords((String) payload.getOrDefault("keywords", ""));
        bookRepository.save(book);
    }

    public void createBook(Map<String, Object> payload) {
        System.out.println("received create book request");
        String name = (String) payload.get("title");
        double r = getBookRating((String) payload.get("html"));
        Book book = Book.builder()
                .createTime(new Date(System.currentTimeMillis()))
                .updateTime(new Date(System.currentTimeMillis()))
                .bookName(name)
                .rating(r)
                .isbn((String) payload.get("book:isbn"))
                .author((String) payload.getOrDefault("book:author", ""))
                .image((String) payload.getOrDefault("og:image", ""))
                .description((String) payload.getOrDefault("description", ""))
                .keywords((String) payload.getOrDefault("keywords", ""))
                .build();
        bookRepository.save(book);
        search.newBooks().send(MessageBuilder.withPayload(book.getId()).build());
    }

    double getBookRating(String html) {
        Document doc = Jsoup.parse(html);
        Elements es = doc.getElementsByAttributeValue("class", "ll rating_num");
        for (Element e : es) {
            TextNode e2 = (TextNode) e.childNode(0);
            String rating = e2.text();
            if (rating.trim().length() > 0) {
                return Double.parseDouble(rating);
            }
        }
        return 0d;
    }
}

