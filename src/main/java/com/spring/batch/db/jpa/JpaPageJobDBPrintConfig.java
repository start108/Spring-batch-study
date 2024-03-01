package com.spring.batch.db.jpa;

import com.spring.batch.common.domain.Dept;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JpaPageJobDBPrintConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private static final int chunkSize = 10;

    @Bean
    public Job jpaPageJobPrintBatchBuild() {
        return new JobBuilder("jpaPageJobPrintBatchBuild", jobRepository)
                .start(jpaPageJobPrintStep())
                .build();
    }

    @Bean
    public Step jpaPageJobPrintStep() {
        return new StepBuilder("jpaPageJobPrintStep", jobRepository)
                .<Dept, Dept>chunk(chunkSize, transactionManager)
                .reader(jpaPageJobDBPrintItemReader())
                .writer(jpaPageJobDBPrintItemWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Dept> jpaPageJobDBPrintItemReader() {
        return new JpaPagingItemReaderBuilder<Dept>()
                .name("jpaPageJobDBPrintItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT dept FROM Dept dept ORDER BY deptNo ASC")
                .build();
    }

    @Bean
    public ItemWriter<Dept> jpaPageJobDBPrintItemWriter() {
        return list -> {
            for(Dept dept : list) {
                log.info(dept.toString());
            }
        };
    }
}
