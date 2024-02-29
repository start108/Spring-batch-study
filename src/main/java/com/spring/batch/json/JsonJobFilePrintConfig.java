package com.spring.batch.json;

import com.spring.batch.csv.dto.CsvFileDto;
import com.spring.batch.json.dto.CoinMarketDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JsonJobFilePrintConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private static final int chunkSize = 5;

    @Bean
    public Job jsonJobFilePrintBatchBuild() {
        return new JobBuilder("jsonJobFilePrintBatchBuild", jobRepository)
                .start(jsonJobPrintBatchStep())
                .build();
    }

    @Bean
    public Step jsonJobPrintBatchStep() {
        return new StepBuilder("jsonJobPrintBatchStep", jobRepository)
                .<CoinMarketDto, CoinMarketDto>chunk(chunkSize, transactionManager)
                .reader(jsonFilePrintReader())
                .writer(coinMarketDtos -> coinMarketDtos.forEach(coinMarketDto -> {
                    log.info(coinMarketDto.toString());
                })).build();
    }

    @Bean
    public JsonItemReader<CoinMarketDto> jsonFilePrintReader() {
        return new JsonItemReaderBuilder<CoinMarketDto>()
                .jsonObjectReader(new JacksonJsonObjectReader<>(CoinMarketDto.class))
                .resource(new ClassPathResource("sample/jsonFileSample.json"))
                .name("jsonFileReader")
                .build();
    }
}
