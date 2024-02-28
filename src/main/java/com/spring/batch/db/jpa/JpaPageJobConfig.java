package com.spring.batch.db.jpa;

import com.spring.batch.db.domain.Dept;
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
public class JpaPageJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    private int chunkSize = 10;

    @Bean
    public Job jpaPageJobBatchBuild() {
        return new JobBuilder("jpaPageJobBatchBuild", jobRepository)
                .start(jpaPageJobStepOne())
                .build();
    }

    @Bean
    public Step jpaPageJobStepOne() {
        return new StepBuilder("jpaPageJobStepOne", jobRepository)
                .<Dept, Dept>chunk(chunkSize, transactionManager)
                .reader(jpaPageJobDBItemReader())
                .writer(jpaPageJobPrintItemWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Dept> jpaPageJobDBItemReader() {
        return new JpaPagingItemReaderBuilder<Dept>()
                .name("jpaPageJobDBItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT dept FROM Dept dept ORDER BY deptNo ASC")
                .build();
    }

    @Bean
    public ItemWriter<Dept> jpaPageJobPrintItemWriter() {
        return list -> {
            for(Dept dept : list) {
                log.info(dept.toString());
            }
        };
    }
}
