package com.spring.batch.json.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CoinMarketDto {

    private String market;
    private String korean_name;
    private String english_name;
    private String market_warning;
    private Object market_event;
}
