package com.spring.batch.db.jpa;

import com.spring.batch.common.domain.Dept;
import com.spring.batch.common.domain.TargetDept;
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
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ChunkSizeTest {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job chunkSizeTestBatch() {
        return new JobBuilder("chunkSizeJobBatch", jobRepository)
                .start(chunkSizeTestStep(0))
                .build();
    }

    @Bean
    @JobScope
    public Step chunkSizeTestStep(@Value("#{jobParameters[chunkSize]}") int chunkSize) {
        return new StepBuilder("jpaPageJobPrintStep", jobRepository)
                .<Dept, TargetDept>chunk(chunkSize, transactionManager)
                .reader(chunkSizeTestItemReader(chunkSize))
                .processor(chunkSizeTestProcessor())
                .writer(chunkSizeTestItemWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Dept> chunkSizeTestItemReader(@Value("#{jobParameters[chunkSize]}") int chunkSize) {
        return new JpaPagingItemReaderBuilder<Dept>()
                .name("chunkSizeTestItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT dept FROM Dept dept ORDER BY deptNo ASC")
                .build();
    }

    private ItemProcessor<Dept, TargetDept> chunkSizeTestProcessor() {
        return dept -> new TargetDept(dept.getDeptNo(), "NEW" + dept.getDName(), "NEW" + dept.getLoc());
    }

    @Bean
    public JpaItemWriter<TargetDept> chunkSizeTestItemWriter() {

        JpaItemWriter<TargetDept> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);

        return jpaItemWriter;
    }
}
