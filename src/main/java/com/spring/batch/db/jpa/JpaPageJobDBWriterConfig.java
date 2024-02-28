package com.spring.batch.db.jpa;

import com.spring.batch.db.domain.Dept;
import com.spring.batch.db.domain.TargetDept;
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
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JpaPageJobDBWriterConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    private static final int chunkSize = 10;

    @Bean
    public Job jpaPageJobDBWriterBatchBuild() {
        return new JobBuilder("jpaPageJobDBWriterBatchBuild", jobRepository)
                .start(jpaPageJobDBWriterStep())
                .build();
    }

    @Bean
    public Step jpaPageJobDBWriterStep() {
        return new StepBuilder("jpaPageJobDBWriterStepOne", jobRepository)
                .<Dept, TargetDept>chunk(chunkSize, transactionManager)
                .reader(jpaPageJobDBWriterItemReader())
                .processor(jpaPageJobDBItemWriterProcessor())
                .writer(jpaPageJobDBWriterItemWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Dept> jpaPageJobDBWriterItemReader() {
        return new JpaPagingItemReaderBuilder<Dept>()
                .name("jpaPageJobDBWriterItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT dept FROM Dept dept ORDER BY deptNo ASC")
                .build();
    }

    private ItemProcessor<Dept, TargetDept> jpaPageJobDBItemWriterProcessor() {
        return dept -> {
            return new TargetDept(dept.getDeptNo(), "NEW_" + dept.getDName(), "NEW_" + dept.getLoc());
        };
    }

    @Bean
    public JpaItemWriter<TargetDept> jpaPageJobDBWriterItemWriter() {

        JpaItemWriter<TargetDept> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);

        return jpaItemWriter;
    }
}
