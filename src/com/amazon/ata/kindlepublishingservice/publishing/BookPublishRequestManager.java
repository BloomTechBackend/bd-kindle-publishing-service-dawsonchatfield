package com.amazon.ata.kindlepublishingservice.publishing;

import javax.inject.Inject;
import java.util.PriorityQueue;

public class BookPublishRequestManager {
    private final PriorityQueue<BookPublishRequest> queue = new PriorityQueue<BookPublishRequest>();

    @Inject
    public BookPublishRequestManager() {}

    public void addBookPublishRequest(BookPublishRequest request) {
        this.queue.add(request);
    }

    public BookPublishRequest getBookPublishRequestToProcess() {
        return this.queue.peek();
    }
}
