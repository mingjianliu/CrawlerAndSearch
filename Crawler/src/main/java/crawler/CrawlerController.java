package crawler;

import crawler.example.BookCrawler;
import crawler.integration.Search;
import crawler.model.Book;
import crawler.service.BookService;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringBootApplication
@EnableJpaRepositories
@Slf4j
@RestController
@RequestMapping("/book")
@EnableBinding(Search.class)
public class CrawlerController implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(CrawlerController.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String crawlStorageFolder = "~/data/crawl/root";
        int numberOfCrawlers = 7;

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);
        config.setMaxDepthOfCrawling(10);

        // Instantiate the controller for this crawl.
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        // For each crawl, you need to add some seed urls. These are the first
        // URLs that are fetched and then the crawler starts following links
        // which are found in these pages
        controller.addSeed("https://book.douban.com");

        // The factory which creates instances of crawlers.
        CrawlController.WebCrawlerFactory<BookCrawler> factory = BookCrawler::new;

        // Start the crawl. This is a blocking operation, meaning that your code
        // will reach the line after this only when crawling is finished.
        controller.start(factory, numberOfCrawlers);
    }

    @Autowired
    private BookService bookService;

    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Book create(@RequestBody Map<String, Object> payload) {
        if (!payload.containsKey("title") || ((String) payload.get("title")).isEmpty()) {
            System.out.println("Invalid input " + payload);
            return null;
        }
        Optional<Book> existingBook = bookService.getBookByName((String) payload.get("title"));
        // TODO: If created, update fetch status
        if (existingBook.isPresent()) {
            bookService.updateBook(payload, existingBook.get());
        } else {
            bookService.createBook(payload);
        }
        return bookService.getBookByName((String) payload.get("title")).get();
    }

    @GetMapping(path = "/all")
    public List<Book> getAllBook() {
        List<Book> result = bookService.getAllBook();
        return result;
    }
}