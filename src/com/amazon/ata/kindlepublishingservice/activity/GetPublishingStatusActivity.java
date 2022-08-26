package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.exceptions.PublishingStatusNotFoundException;
import com.amazon.ata.kindlepublishingservice.models.PublishingStatusRecord;
import com.amazon.ata.kindlepublishingservice.models.requests.GetPublishingStatusRequest;
import com.amazon.ata.kindlepublishingservice.models.response.GetPublishingStatusResponse;
import com.amazonaws.services.lambda.runtime.Context;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;

public class GetPublishingStatusActivity {

    private final PublishingStatusDao publishingdao;
    @Inject
    public GetPublishingStatusActivity(PublishingStatusDao dao) {
        this.publishingdao = dao;
    }

    public GetPublishingStatusResponse execute(GetPublishingStatusRequest publishingStatusRequest) {
        String id = publishingStatusRequest.getPublishingRecordId();

        List<PublishingStatusItem> items = this.publishingdao.getPublishingStatuses(id);

        if(items.size() == 0) {
            throw new PublishingStatusNotFoundException("NO PUBLISHING STATUS FOUND!");
        }
        List<PublishingStatusRecord> records = new LinkedList<PublishingStatusRecord>();

        for(PublishingStatusItem item : items) {
            records.add(
                    PublishingStatusRecord.builder()
                            .withStatus(item.getStatus().toString())
                            .withStatusMessage(item.getStatusMessage())
                            .withBookId(item.getBookId())
                            .build()
            );
        }


        return GetPublishingStatusResponse.builder()
                .withPublishingStatusHistory(records)
                .build();
    }
}
