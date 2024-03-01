package com.spring.batch.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TextFileDto {

    private String oneLine;

    @Override
    public String toString() {
        return oneLine;
    }
}
