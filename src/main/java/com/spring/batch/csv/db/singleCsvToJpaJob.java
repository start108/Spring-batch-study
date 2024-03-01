package com.spring.batch.csv.db;

import com.spring.batch.common.dto.CsvFileDto;
import com.spring.batch.common.domain.Dept;
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
public class singleCsvToJpaJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private static final int chunkSize = 10;

    @Bean
    public Job singleCsvToJpaJobBatch() {
        return new JobBuilder("singleCsvToJpaJobBatch", jobRepository)
                .start(singleCsvToJpaStep())
                .build();
    }

    @Bean
    public Step singleCsvToJpaStep() {
        return new StepBuilder("singleCsvToJpaStep", jobRepository)
                .<CsvFileDto, Dept>chunk(chunkSize, transactionManager)
                .reader(singleCsvToJpaReader())
                .processor(singleCsvToJpaProcessor())
                .writer(singleCsvToJpaWriter())
                .build();
    }

    @Bean
    public FlatFileItemReader<CsvFileDto> singleCsvToJpaReader() {

        FlatFileItemReader<CsvFileDto> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setResource(new ClassPathResource("sample/singleCsvToJpaSample.csv"));

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

    private ItemProcessor<CsvFileDto, Dept> singleCsvToJpaProcessor() {
        return csvFileDto -> new Dept(Integer.parseInt(csvFileDto.getOne()), csvFileDto.getTwo(), "기타");
    }

    @Bean
    public JpaItemWriter<Dept> singleCsvToJpaWriter() {

        JpaItemWriter<Dept> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);

        return jpaItemWriter;
    }
}
