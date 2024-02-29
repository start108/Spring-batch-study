package com.spring.batch.csv;

import com.spring.batch.csv.custom.CustomBeanWrapperFieldExtractor;
import com.spring.batch.csv.dto.CsvFileDto;
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
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class csvJobWriterConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private static final int chunkSize = 5;

    @Bean
    public Job csvJobWriterBatchBuild() throws Exception {
        return new JobBuilder("csvJobWriterBatchBuild", jobRepository)
                .start(csvJobWriterBatchStep())
                .build();
    }

    @Bean
    public Step csvJobWriterBatchStep() throws Exception {
        return new StepBuilder("csvJobWriterBatchStep", jobRepository)
                .<CsvFileDto, CsvFileDto>chunk(chunkSize, transactionManager)
                .reader(csvFileWriterReader())
                .writer(csvFileWriter(new FileSystemResource("output/csvFileOutputSample.csv"))).build();
    }

    @Bean
    public FlatFileItemReader<CsvFileDto> csvFileWriterReader() {

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

    @Bean
    public FlatFileItemWriter<CsvFileDto> csvFileWriter(Resource resource) throws Exception {

        CustomBeanWrapperFieldExtractor<CsvFileDto> customBeanWrapperFieldExtractor = new CustomBeanWrapperFieldExtractor<>();

        customBeanWrapperFieldExtractor.setNames(new String[]{"one", "two"});
        customBeanWrapperFieldExtractor.afterPropertiesSet();

        DelimitedLineAggregator<CsvFileDto> delimitedLineAggregator = new DelimitedLineAggregator<>();

        delimitedLineAggregator.setDelimiter("@");
        delimitedLineAggregator.setFieldExtractor(customBeanWrapperFieldExtractor);

        return new FlatFileItemWriterBuilder<CsvFileDto>().name("csvFileWriter")
                .resource((WritableResource) resource)
                .lineAggregator(delimitedLineAggregator)
                .build();
    }
}
