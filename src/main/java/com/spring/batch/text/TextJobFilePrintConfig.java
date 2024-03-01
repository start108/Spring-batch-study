package com.spring.batch.text;

import com.spring.batch.common.dto.TextFileDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class TextJobFilePrintConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private static final int chunkSize = 5;

    @Bean
    public Job textJobFilePrintBatchBuild() {
        return new JobBuilder("textJobFilePrintBatchBuild", jobRepository)
                .start(textJobFilePrintBatchStep())
                .build();
    }

    @Bean
    public Step textJobFilePrintBatchStep() {
        return new StepBuilder("textJobFilePrintBatchStep", jobRepository)
                .<TextFileDto, TextFileDto>chunk(chunkSize, transactionManager)
                .reader(textJobFilePrintReader())
                .writer(oneDto -> oneDto.forEach(i -> {
                    log.info(i.toString());
                })).build();
    }

    @Bean
    public FlatFileItemReader<TextFileDto> textJobFilePrintReader() {

        FlatFileItemReader<TextFileDto> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setResource(new ClassPathResource("sample/txtFileSample.txt"));
        flatFileItemReader.setLineMapper((line, lineNumber) -> new TextFileDto(line));

        return flatFileItemReader;
    }
}
