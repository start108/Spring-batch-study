package com.spring.batch.sample;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class TaskletJob {

    @Bean
    public Job taskletJobBatchBuild(JobRepository jobRepository, Step step) {
        return new JobBuilder("taskletJobBatchBuild", jobRepository)
                .start(step)
                .build();
    }

    @Bean
    public Step taskletJobStepOne(JobRepository jobRepository, Tasklet taskletJob, PlatformTransactionManager transactionManager) {
        return new StepBuilder("taskletJobStepOne", jobRepository)
                .tasklet(taskletJob, transactionManager)
                .build();
    }

    /** Batch 4.X **/
    /*
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job taskletJobBatchBuild() {
        return jobBuilder.get("taskletJob")
                .start(taskletJobStepOne()).build();
    }

    @Bean
    public Step taskletJobStepOne() {
        return stepBuilder.get("taskletJobStepOne")
                .tasklet((a, b) -> {
                    log.debug("-> job -> [Step One]");
                    return RepeatStatus.FINISHED;
        }).build();
    }
    */
}
