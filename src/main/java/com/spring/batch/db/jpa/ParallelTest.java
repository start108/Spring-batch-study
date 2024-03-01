package com.spring.batch.db.jpa;

import com.spring.batch.common.domain.Dept;
import com.spring.batch.common.domain.TargetDept;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ParallelTest {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private static final int chunkSize = 10;

    @Bean
    public Job parallelTestBatch() {

        Flow firstFlow = new FlowBuilder<Flow>("firstFlow")
                .start(parallelTestStepOne())
                .build();

        Flow secondFlow = new FlowBuilder<Flow>("secondFlow")
                .start(parallelTestStepTwo())
                .build();

        Flow parallelStepFlow = new FlowBuilder<Flow>("parallelStepFlow")
                .split(new SimpleAsyncTaskExecutor())
                .add(firstFlow, secondFlow)
                .build();

        return new JobBuilder("parallelTestBatch", jobRepository)
                .start(parallelStepFlow)
                .build()
                    .build();
    }

    @Bean
    public Step parallelTestStepOne() {
        return new StepBuilder("parallelTestStepOne", jobRepository)
                .<Dept, TargetDept>chunk(chunkSize, transactionManager)
                .reader(parallelTestItemReaderOne())
                .processor(parallelTestProcessorOne())
                .writer(parallelTestItemWriterOne())
                .build();
    }

    @Bean
    public Step parallelTestStepTwo() {
        return new StepBuilder("parallelTestStepTwo", jobRepository)
                .<Dept, TargetDept>chunk(chunkSize, transactionManager)
                .reader(parallelTestItemReaderTwo())
                .processor(parallelTestProcessorTwo())
                .writer(parallelTestItemWriterTwo())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Dept> parallelTestItemReaderOne() {
        return new JpaPagingItemReaderBuilder<Dept>()
                .name("parallelTestItemReaderOne")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT dept FROM Dept dept WHERE deptNo <= 5000 ORDER BY deptNo ASC")
                .build();
    }

    private ItemProcessor<Dept, TargetDept> parallelTestProcessorOne() {
        return dept -> new TargetDept(dept.getDeptNo(), "NEW_" + dept.getDName(), "NEW_" + dept.getLoc());
    }

    @Bean
    public JpaItemWriter<TargetDept> parallelTestItemWriterOne() {

        JpaItemWriter<TargetDept> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);

        return jpaItemWriter;
    }

    @Bean
    public JpaPagingItemReader<Dept> parallelTestItemReaderTwo() {
        return new JpaPagingItemReaderBuilder<Dept>()
                .name("parallelTestItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT dept FROM Dept dept WHERE deptNo > 5000 ORDER BY deptNo ASC")
                .build();
    }

    private ItemProcessor<Dept, TargetDept> parallelTestProcessorTwo() {
        return dept -> new TargetDept(dept.getDeptNo(), "NEW_" + dept.getDName(), "NEW_" + dept.getLoc());
    }

    @Bean
    public JpaItemWriter<TargetDept> parallelTestItemWriterTwo() {

        JpaItemWriter<TargetDept> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);

        return jpaItemWriter;
    }
}
