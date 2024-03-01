package com.spring.batch.multi.file;

import com.spring.batch.common.domain.TargetSample;
import com.spring.batch.common.dto.CsvFileDto;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.separator.SimpleRecordSeparatorPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class FileToDBJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private static final int chunkSize = 10;

    @Bean
    public Job fileToDBJobBatch() {
        return new JobBuilder("fileToDBJobBatch", jobRepository)
                .start(fileToDBStep())
                .build();
    }

    @Bean
    @JobScope
    public Step fileToDBStep() {
        return new StepBuilder("fileToDBStep", jobRepository)
                .<CsvFileDto, TargetSample>chunk(chunkSize, transactionManager)
                .reader(fileToDBReader(null))
                .processor(fileToDBProcessor())
                .writer(fileToDBWriter())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<CsvFileDto> fileToDBReader(@Value("#{jobParameters[inFileName]}") String inFileName) {
        return new FlatFileItemReaderBuilder<CsvFileDto>()
                .name("fileToDBReader")
                .resource(new FileSystemResource(inFileName))
                .delimited().delimiter(":")
                .names("one", "two")
                .targetType(CsvFileDto.class)
                .recordSeparatorPolicy(new SimpleRecordSeparatorPolicy() {

                    @Override
                    public String postProcess(String record) {

                        log.info("Reader(Policy) : " + record);

                        if (record.indexOf(":") == -1) {
                            return null;
                        }
                        return record.trim();
                    }
                })
                .build();
    }

    private ItemProcessor<CsvFileDto, TargetSample> fileToDBProcessor() {
        return csvFileDto -> new TargetSample(csvFileDto.getOne(), csvFileDto.getTwo());
    }

    @Bean
    @StepScope
    public JpaItemWriter<TargetSample> fileToDBWriter() {

        JpaItemWriter<TargetSample> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);

        return jpaItemWriter;
    }
}
