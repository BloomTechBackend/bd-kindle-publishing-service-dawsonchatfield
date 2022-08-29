package com.amazon.ata.kindlepublishingservice.publishing;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.Queue;

public class BookPublishRequestManager {
    private static Queue<BookPublishRequest> queue = new LinkedList<BookPublishRequest>();

    @Inject
    public BookPublishRequestManager() {}

    public void addBookPublishRequest(BookPublishRequest request) {
        queue.add(request);
    }

    public BookPublishRequest getBookPublishRequestToProcess() {
        return queue.poll();
    }
}
