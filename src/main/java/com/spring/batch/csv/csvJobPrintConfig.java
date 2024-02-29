package com.spring.batch.csv;

import com.spring.batch.csv.dto.CsvFileDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class csvJobPrintConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private static final int chunkSize = 5;

    @Bean
    public Job csvJobBatchBuild() {
        return new JobBuilder("csvJobBatchBuild", jobRepository)
                .start(csvJobBatchStep())
                .build();
    }

    @Bean
    public Step csvJobBatchStep() {
        return new StepBuilder("csvJobBatchStep", jobRepository)
                .<CsvFileDto, CsvFileDto>chunk(chunkSize, transactionManager)
                .reader(csvFileReader())
                .writer(csvFileDtos -> csvFileDtos.forEach(csvFileDto -> {
                    log.info(csvFileDto.toString());
                })).build();
    }

    @Bean
    public FlatFileItemReader<CsvFileDto> csvFileReader() {

        FlatFileItemReader<CsvFileDto> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setResource(new ClassPathResource("sample/csvFileSample.csv"));
        flatFileItemReader.setLinesToSkip(1);

        DefaultLineMapper<CsvFileDto> defaultLineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();

        delimitedLineTokenizer.setNames("one", "two");
        delimitedLineTokenizer.setDelimiter(":");

        BeanWrapperFieldSetMapper<CsvFileDto> beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<>();
        beanWrapperFieldSetMapper.setTargetType(CsvFileDto.class);

        defaultLineMapper.setLineTokenizer(delimitedLineTokenizer);
        defaultLineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);
        flatFileItemReader.setLineMapper(defaultLineMapper);

        return flatFileItemReader;
    }
}
