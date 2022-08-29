package com.amazon.ata.kindlepublishingservice.publishing;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;

import javax.inject.Inject;

public class BookPublishTask implements Runnable {
    private CatalogDao catalogDao;
    private PublishingStatusDao publishingStatusDao;
    private BookPublishRequestManager manager;

    @Inject
    public BookPublishTask(CatalogDao catalogDao, PublishingStatusDao publishingStatusDao, BookPublishRequestManager manager) {
        this.catalogDao = catalogDao;
        this.publishingStatusDao = publishingStatusDao;
        this.manager = manager;
    }

    @Override
    public void run() {
        BookPublishRequest request = this.manager.getBookPublishRequestToProcess();
        if(request == null) {
            return;
        }

        this.publishingStatusDao.setPublishingStatus(
                request.getPublishingRecordId(),
                PublishingRecordStatus.IN_PROGRESS,
                request.getBookId()
        );


        KindleFormattedBook book = KindleFormatConverter.format(request);
        CatalogItemVersion item = null;
        try {
            item = this.catalogDao.createOrUpdateBook(book);
        } catch(BookNotFoundException e) {
            // set new status
            this.publishingStatusDao.setPublishingStatus(
                    request.getPublishingRecordId(),
                    PublishingRecordStatus.FAILED,
                    request.getBookId(),
                    "BOOK NOT FOUND!"
            );
            return;
        }

        this.publishingStatusDao.setPublishingStatus(
                request.getPublishingRecordId(),
                PublishingRecordStatus.SUCCESSFUL,
                item.getBookId()
        );

    }
}
