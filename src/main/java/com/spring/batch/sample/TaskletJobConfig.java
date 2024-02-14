package com.spring.batch.sample;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class TaskletJobConfig {

    private final JobRepository jobRepository;

    private final PlatformTransactionManager transactionManager;

    private final TaskletJobParameter taskletJobParameter;

    @Bean
    public Job taskletJobBatchBuild() {
        return new JobBuilder("taskletJobBatchBuild", jobRepository)
                .start(taskletJobStepOne())
                .next(taskletJobStepTwo())
                .build();
    }

    @Bean
    public Step taskletJobStepOne() {
        return new StepBuilder("taskletJobStepOne", jobRepository)
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        log.info("-> job -> [Step One]");
                        return RepeatStatus.FINISHED;
                    }
                }, transactionManager)
                .build();
    }

    @Bean
    public Step taskletJobStepTwo() {
        return new StepBuilder("taskletJobStepTwo", jobRepository)
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        log.info("-> [Step One] -> [Step Two] : " + taskletJobParameter.getDate());
                        return RepeatStatus.FINISHED;
                    }
                }, transactionManager)
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
