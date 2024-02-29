package com.spring.batch.json;

import com.spring.batch.json.dto.CoinMarketDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.json.*;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JsonJobFileWriterConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private static final int chunkSize = 5;

    @Bean
    public Job jsonJobFileWriterBatchBuild() {
        return new JobBuilder("jsonJobFileWriterBatchBuild", jobRepository)
                .start(jsonJobFileWriterBatchStep())
                .build();
    }

    @Bean
    public Step jsonJobFileWriterBatchStep() {
        return new StepBuilder("jsonJobWriterBatchStep", jobRepository)
                .<CoinMarketDto, CoinMarketDto>chunk(chunkSize, transactionManager)
                .reader(jsonFileReader())
                .processor(jsonFileProcessor())
                .writer(jsonFileWriter())
                .build();
    }

    @Bean
    public JsonItemReader<CoinMarketDto> jsonFileReader() {
        return new JsonItemReaderBuilder<CoinMarketDto>()
                .jsonObjectReader(new JacksonJsonObjectReader<>(CoinMarketDto.class))
                .resource(new ClassPathResource("sample/jsonFileSample.json"))
                .name("jsonFileReader")
                .build();
    }

    private ItemProcessor<CoinMarketDto, CoinMarketDto> jsonFileProcessor() {
        return coinMarket -> {

            if (coinMarket.getMarket().startsWith("KRW-")) {
                return new CoinMarketDto(coinMarket.getMarket(), coinMarket.getKorean_name(), coinMarket.getEnglish_name(), coinMarket.getMarket_warning(), coinMarket.getMarket_event());
            } else {
                return null;
            }
        };
    }

    @Bean
    public JsonFileItemWriter<CoinMarketDto> jsonFileWriter() {
        return new JsonFileItemWriterBuilder<CoinMarketDto>()
                .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
                .resource(new FileSystemResource("output/jsonFileOutputSample.json"))
                .name("jsonFileWriter")
                .build();
    }
}
