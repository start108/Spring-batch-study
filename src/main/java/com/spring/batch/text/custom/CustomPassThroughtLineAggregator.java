package com.spring.batch.text.custom;

import org.springframework.batch.item.file.transform.LineAggregator;

public class CustomPassThroughtLineAggregator<T> implements LineAggregator<T> {

    @Override
    public String aggregate(T item) {
        return item.toString();
    }
}
