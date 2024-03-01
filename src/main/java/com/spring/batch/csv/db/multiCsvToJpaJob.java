package com.spring.batch.csv.db;

import com.spring.batch.common.domain.Dept;
import com.spring.batch.common.dto.CsvFileDto;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class multiCsvToJpaJob {

    private final ResourceLoader resourceLoader;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private static final int chunkSize = 10;

    @Bean
    public Job multiCsvToJpaJobBatch() {
        return new JobBuilder("multiCsvToJpaJobBatch", jobRepository)
                .start(multiCsvToJpaStep())
                .build();
    }

    @Bean
    public Step multiCsvToJpaStep() {
        return new StepBuilder("multiCsvToJpaStep", jobRepository)
                .<CsvFileDto, Dept>chunk(chunkSize, transactionManager)
                .reader(multiCsvToJpaReader())
                .processor(multiCsvToJpaProcessor())
                .writer(multiCsvToJpaWriter())
                .faultTolerant()
                .skip(FlatFileParseException.class)
                .skipLimit(100)
                .build();
    }

    @Bean
    public MultiResourceItemReader<CsvFileDto> multiCsvToJpaReader() {

        MultiResourceItemReader<CsvFileDto> multiResourceItemReader = new MultiResourceItemReader<>();

        try {
            multiResourceItemReader.setResources(
                    ResourcePatternUtils.getResourcePatternResolver(this.resourceLoader).getResources(
                            "classpath:sample/multiFile/*.txt"
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        multiResourceItemReader.setDelegate(multiCsvToJpaItemReader());

        return multiResourceItemReader;
    }

    @Bean
    public FlatFileItemReader<CsvFileDto> multiCsvToJpaItemReader() {

        FlatFileItemReader<CsvFileDto> flatFileItemReader = new FlatFileItemReader<>();

        flatFileItemReader.setLineMapper((line, lineNumber) -> {

            String[] lines = line.split("#");

            return new CsvFileDto(lines[0], lines[1]);
        });

        return flatFileItemReader;
    }

    private ItemProcessor<CsvFileDto, Dept> multiCsvToJpaProcessor() {
        return csvFileDto -> new Dept(Integer.parseInt(csvFileDto.getOne()), csvFileDto.getTwo(), "기타");
    }

    @Bean
    public JpaItemWriter<Dept> multiCsvToJpaWriter() {

        JpaItemWriter<Dept> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);

        return jpaItemWriter;
    }
}
