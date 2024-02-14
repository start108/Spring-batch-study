package com.spring.batch.sample;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@StepScope
@Component
public class CustomTasklet implements Tasklet {

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("-> job -> [Step One]");
        return RepeatStatus.FINISHED;
    }

    public RepeatStatus execute(@Value("#{jobParameters[date]}") String date, StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("-> [Step One] -> [Step Two] : " + date);
        return RepeatStatus.FINISHED;
    }
}
