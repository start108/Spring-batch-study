package com.spring.batch.multi.file;

import com.spring.batch.common.dto.CsvFileDto;
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
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.separator.SimpleRecordSeparatorPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class MultiJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private static final int chunkSize = 10;

    @Bean
    public Job multiJobBatch() {
        return new JobBuilder("multiJobBatch", jobRepository)
                .start(multiStep(null))
                .build();
    }

    @Bean
    @JobScope
    public Step multiStep(@Value("#{jobParameters[version]}") String version) {

        log.info("------------");
        log.info(version);
        log.info("------------");

        return new StepBuilder("multiStep", jobRepository)
                .<CsvFileDto, CsvFileDto>chunk(chunkSize, transactionManager)
                .reader(multiReader(null))
                .processor(multiProcessor(null))
                .writer(multiWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<CsvFileDto> multiReader(@Value("#{jobParameters[inFileName]}") String inFileName) {
        return new FlatFileItemReaderBuilder<CsvFileDto>()
                .name("multiReader")
                .resource(new ClassPathResource("sample/" + inFileName))
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

    @Bean
    @StepScope
    public ItemProcessor<CsvFileDto, CsvFileDto> multiProcessor(@Value("#{jobParameters[version]}") String version) {
        log.info("Processor : " + version);
        return csvFileDto -> new CsvFileDto(csvFileDto.getOne(), csvFileDto.getTwo());
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<CsvFileDto> multiWriter(@Value("#{jobParameters[outFileName]}") String outFileName) {
        return new FlatFileItemWriterBuilder<CsvFileDto>()
                .name("multiWriter")
                .resource(new FileSystemResource("sample/" + outFileName))
                .lineAggregator(item -> item.getOne() + "-" + item.getTwo())
                .build();
    }
}
