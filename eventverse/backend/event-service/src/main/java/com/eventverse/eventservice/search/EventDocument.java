package com.eventverse.eventservice.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "events")
public class EventDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String city;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String venue;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Date)
    private OffsetDateTime time;

    @Field(type = FieldType.Long)
    private Long popularityScore;

    @Field(type = FieldType.Keyword)
    private String imageUrl;
}
