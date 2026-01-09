package com.eventverse.eventservice.sharding;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class ShardRoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        // Spring will use this key to pick the target DataSource
        return ShardContext.get();
    }
}