package br.com.fiap.librarybatchchunk;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

@SpringBootApplication
@EnableBatchProcessing
public class LibraryBatchChunkApplication {

	@Bean
	public FlatFileItemReader<Pessoa> itemReader(@Value("${file.input}") Resource resource) {
		//@formatter:off
		return new FlatFileItemReaderBuilder<Pessoa>()
				.name("file item reader")
				.targetType(Pessoa.class)
				.delimited().delimiter(";").names("nome", "cpf")
				.resource(resource)
				.build();
	}

	@Bean
	public ItemProcessor<Pessoa, Pessoa> itemProcessor() {
		return pessoa -> {
			pessoa.setNome(pessoa.getNome().toUpperCase());
			pessoa.setCpf(pessoa.getCpf().replaceAll("\\.", "").replace("-", ""));
			return pessoa;
		};
	}

	@Bean
	public JdbcBatchItemWriter<Pessoa> itemWriter(DataSource dataSource) {
		//@formatter:off
		return new JdbcBatchItemWriterBuilder<Pessoa>()
				.dataSource(dataSource)
				.sql("insert into TB_PESSOA(nome, cpf) values (:nome, :cpf)")
				.beanMapped()
				.build();
		//@formatter:on
	}

	@Bean
	public Step step ( StepBuilderFactory stepBuilderFactory,
					 ItemReader<Pessoa> itemReader,
					 ItemProcessor<Pessoa, Pessoa> itemProcessor,
					 ItemWriter<Pessoa> itemWriter) {

		return stepBuilderFactory.get("pessoa step")
								 .<Pessoa, Pessoa>chunk(100)
								 .reader(itemReader)
								 .processor(itemProcessor)
								 .writer(itemWriter)
								 .build();
	}

	@Bean
	public Job job(JobBuilderFactory jobBuilderFactory, Step step) {
		return jobBuilderFactory.get("processar pessoa job")
								.start(step)
								.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(LibraryBatchChunkApplication.class, args);
	}

}
