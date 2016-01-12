package springbook.learningtest.spring.embededdb;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;

public class EmbededDbTest {

	EmbeddedDatabase db;
	SimpleJdbcTemplate template;
	
	@Before
	public void setUp(){
		db = new EmbeddedDatabaseBuilder()
				.setType(EmbeddedDatabaseType.HSQL)
				.addScript("classpath:/springbook/learningtest/spring/embededdb/schema.sql")
				.addScript("classpath:/springbook/learningtest/spring/embededdb/data.sql")
				.build();
		
		template = new SimpleJdbcTemplate(db);
	}
	
	@After
	public void tearDown(){
		db.shutdown();
	}
	
	@Test
	public void initData(){
		assertThat(template.queryForInt("select count(*) from sqlmap"), is(2));
		List<Map<String, Object>> list = template.queryForList("select * from sqlmap order by key_");
		assertThat((String)list.get(0).get("key_"), is("KEY1"));
		assertThat((String)list.get(0).get("sql_"), is("SQL1"));
		assertThat((String)list.get(1).get("key_"), is("KEY2"));
		assertThat((String)list.get(1).get("sql_"), is("SQL2"));
	}
	
	@Test
	public void insert(){
		template.update("insert into sqlmap(key_, sql_) values (?, ?)", "KEY3", "SQL3");
		
		assertThat(template.queryForInt("select count(*) from sqlmap"), is(3));
	}
	
	
}
