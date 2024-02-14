//package com.spring.batch.sample;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.StepContribution;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.core.scope.context.ChunkContext;
//import org.springframework.batch.core.step.builder.StepBuilder;
//import org.springframework.batch.core.step.tasklet.Tasklet;
//import org.springframework.batch.repeat.RepeatStatus;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.transaction.PlatformTransactionManager;
//
//@Slf4j
//@RequiredArgsConstructor
//@Configuration
//public class TaskletStepConfig {
//
//    private final TaskletJobParameter jobParameter;
//
//    @Bean
//    public Tasklet tasklet() {
//        return new CustomTasklet();
//    }
//
//    @Bean
//    public Step taskletJobStepOne(JobRepository jobRepository, Tasklet taskletJob, PlatformTransactionManager transactionManager) {
//        return new StepBuilder("taskletJobStepOne", jobRepository)
//                .tasklet(taskletJob, transactionManager)
//                .build();
//    }
//
//    @Bean
//    public Step taskletJobStepTwo(JobRepository jobRepository, Tasklet taskletJob, PlatformTransactionManager transactionManager) {
//        return new StepBuilder("taskletJobStepTwo", jobRepository)
//                .tasklet(taskletJob, transactionManager)
//                .build();
//    }
//}
