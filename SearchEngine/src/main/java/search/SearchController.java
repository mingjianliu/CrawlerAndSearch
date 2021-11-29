package search;

import search.integration.Crawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories
@SpringBootApplication
@EnableBinding(Crawler.class)
public class SearchController {

    public static void main(String[] args) {
        SpringApplication.run(SearchController.class, args);
    }

}
