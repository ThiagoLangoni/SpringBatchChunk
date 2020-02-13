package br.com.fiap.librarybatchchunk;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import javax.batch.runtime.BatchStatus;
import javax.sql.DataSource;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { LibraryBatchChunkApplication.class, BatchConfig.class })
class LibraryBatchChunkApplicationTests {

	@Autowired
	private JobLauncherTestUtils jobLauncherTest;

	@Autowired
	private Job job;

	@Autowired
	private DataSource dataSource;

	@Test
	public void testPessoaJob() throws JobExecutionAlreadyRunningException, JobRestartException,
			JobInstanceAlreadyCompleteException, JobParametersInvalidException, SQLException {
		JobExecution jobExecution = jobLauncherTest.getJobLauncher()
						.run(job, jobLauncherTest.getUniqueJobParameters());

		Assertions.assertNotNull(jobExecution);
		Assertions.assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

		ResultSet resultSet = dataSource.getConnection()
										.prepareStatement("select count(*) from TB_PESSOA")
										.executeQuery();

		Awaitility.await().atMost(10, TimeUnit.SECONDS)
				  .until(() -> {
					  resultSet.last();
					  return resultSet.getInt(1) == 3;
				  });

		Assertions.assertEquals(3, resultSet.getInt(1));
	}
}
