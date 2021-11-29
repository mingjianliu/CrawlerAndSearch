package search.integration;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface Crawler {
    public String NEW_BOOKS = "newBooks";

    @Input(NEW_BOOKS)
    SubscribableChannel newBooks();
}
