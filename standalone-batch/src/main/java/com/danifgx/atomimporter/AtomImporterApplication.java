package com.danifgx.atomimporter;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * Main class for the standalone Atom Importer application.
 * This is a non-Spring Boot application that uses Spring Batch to import atom files into a database.
 */
public class AtomImporterApplication {

    public static void main(String[] args) {
        // Create Spring context
        AbstractApplicationContext context = new AnnotationConfigApplicationContext(BatchConfig.class);

        try {
            // Get job launcher and job from context
            JobLauncher jobLauncher = context.getBean(JobLauncher.class);
            Job job = context.getBean("processAtomFilesJob", Job.class);

            // Create job parameters with a timestamp to ensure uniqueness
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            // Run the job
            System.out.println("Starting job: " + job.getName());
            JobExecution execution = jobLauncher.run(job, jobParameters);
            System.out.println("Job finished with status: " + execution.getStatus());
        } catch (Exception e) {
            System.err.println("Error running job: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close the Spring context
            context.close();
        }
    }
}