package com.spring.batch.sample;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
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

    @Bean
    public Job taskletJobBatchBuild() {
        return new JobBuilder("taskletJobBatchBuild", jobRepository)
                .start(taskletJobStepOne())
                .next(taskletJobStepTwo(null))
                .build();
    }

    @Bean
    public Step taskletJobStepOne() {
        return new StepBuilder("taskletJobStepOne", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("[Job] -> [Step One]");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    @JobScope
    public Step taskletJobStepTwo(@Value("#{jobParameters[date]}") String date) {
        return new StepBuilder("taskletJobStepTwo", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("[Step One] -> [Step Two] : " + date);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    /** Batch 4.X **/
//    private final JobBuilderFactory jobBuilderFactory;
//    private final StepBuilderFactory stepBuilderFactory;
//
//    @Bean
//    public Job taskletJobBatchBuild() {
//        return jobBuilderFactory.get("taskletJob")
//                .start(taskletJobStepOne()).build();
//    }
//
//    @Bean
//    public Step taskletJobStepOne() {
//        return stepBuilderFactory.get("taskletJobStepOne")
//                .tasklet((a, b) -> {
//                    log.debug("-> job -> [Step One]");
//                    return RepeatStatus.FINISHED;
//        }).build();
//    }
}
