package com.spring.batch.text;

import com.spring.batch.text.custom.CustomPassThroughtLineAggregator;
import com.spring.batch.common.dto.TextFileDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class TextJobFileWriterConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private static final int chunkSize = 5;

    @Bean
    public Job textJobFileWriterBatchBuild() {
        return new JobBuilder("textJobFileWriterBatchBuild", jobRepository)
                .start(textJobFileWriterBatchStep())
                .build();
    }

    @Bean
    public Step textJobFileWriterBatchStep() {
        return new StepBuilder("textJobFileWriterBatchStep", jobRepository)
                .<TextFileDto, TextFileDto>chunk(chunkSize, transactionManager)
                .reader(textJobFileReader())
                .writer(textJobFileWriter())
                .build();
    }

    @Bean
    public FlatFileItemReader<TextFileDto> textJobFileReader() {

        FlatFileItemReader<TextFileDto> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setResource(new ClassPathResource("sample/txtFileWriterSample.txt"));
        flatFileItemReader.setLineMapper((line, lineNumber) -> new TextFileDto(line));

        return flatFileItemReader;
    }

    @Bean
    public FlatFileItemWriter<TextFileDto> textJobFileWriter() {
        return new FlatFileItemWriterBuilder<TextFileDto>()
                .name("textJobFileWriter")
                .resource(new FileSystemResource("output/txtFileOutputSample.txt"))
                .lineAggregator(new CustomPassThroughtLineAggregator<>())
                .build();
    }
}
