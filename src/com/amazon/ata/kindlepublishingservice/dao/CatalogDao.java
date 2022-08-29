package com.amazon.ata.kindlepublishingservice.dao;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.publishing.KindleFormattedBook;
import com.amazon.ata.kindlepublishingservice.utils.KindlePublishingUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import javax.inject.Inject;

public class CatalogDao {

    private final DynamoDBMapper dynamoDbMapper;

    /**
     * Instantiates a new CatalogDao object.
     *
     * @param dynamoDbMapper The {@link DynamoDBMapper} used to interact with the catalog table.
     */
    @Inject
    public CatalogDao(DynamoDBMapper dynamoDbMapper) {
        this.dynamoDbMapper = dynamoDbMapper;
    }

    /**
     * Returns the latest version of the book from the catalog corresponding to the specified book id.
     * Throws a BookNotFoundException if the latest version is not active or no version is found.
     * @param bookId Id associated with the book.
     * @return The corresponding CatalogItem from the catalog table.
     */
    public CatalogItemVersion getBookFromCatalog(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);

        if (book == null || book.isInactive()) {
            throw new BookNotFoundException(String.format("GETBOOKFROMCATALOG | No book found for id: %s", bookId));
        }

        return book;
    }

    public void deleteBookFromCatalog(String bookId) {
        CatalogItemVersion book = this.getLatestVersionOfBook(bookId);
        if(book == null || book.isInactive()) {
            throw new BookNotFoundException(String.format("DELETE | No book found for id: %s", bookId));
        }
        book.setInactive(true);
        this.dynamoDbMapper.save(book);
    }

    public boolean validateBookExists(String bookId) {
        CatalogItemVersion book = this.getLatestVersionOfBook(bookId);
        return book == null;
    }

    public CatalogItemVersion createOrUpdateBook(KindleFormattedBook book) {
        CatalogItemVersion item = null;
        if(book.getBookId() == null || book.getBookId().isBlank()) {
            item = new CatalogItemVersion();
            item.setBookId(KindlePublishingUtils.generateBookId());
            item.setTitle(book.getTitle());
            item.setAuthor(book.getAuthor());
            item.setGenre(book.getGenre());
            item.setText(book.getText());
            item.setVersion(1);

            this.dynamoDbMapper.save(item);
            return item;
        }

        item = this.getLatestVersionOfBook(book.getBookId());
        assert item != null; // should already throw exception

        CatalogItemVersion newItem = new CatalogItemVersion();
        newItem.setBookId(book.getBookId());
        newItem.setInactive(false);
        newItem.setAuthor(book.getAuthor());
        newItem.setText(book.getText());
        newItem.setTitle(book.getTitle());
        newItem.setGenre(book.getGenre());
        newItem.setVersion(item.getVersion() + 1);



        System.err.println("Got Here!");
        this.deleteBookFromCatalog(book.getBookId());
        this.dynamoDbMapper.save(newItem);

        return newItem;
    }

    // Returns null if no version exists for the provided bookId
    private CatalogItemVersion getLatestVersionOfBook(String bookId) {
        CatalogItemVersion book = new CatalogItemVersion();
        book.setBookId(bookId);

        DynamoDBQueryExpression<CatalogItemVersion> queryExpression = new DynamoDBQueryExpression()
            .withHashKeyValues(book)
            .withScanIndexForward(false)
            .withLimit(1);

        List<CatalogItemVersion> results = dynamoDbMapper.query(CatalogItemVersion.class, queryExpression);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }
}
