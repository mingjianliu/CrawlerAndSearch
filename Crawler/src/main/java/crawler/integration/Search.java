package crawler.integration;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface Search {
    String NEW_BOOKS = "newBooks";

    @Output
    MessageChannel newBooks();
}
