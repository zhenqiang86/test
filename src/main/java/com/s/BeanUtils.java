package com.s;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

public class BeanUtils {
	
	private static Logger logger = LoggerFactory.getLogger(BeanUtils.class);
	
	private MongoTemplate mongoTemplate;
	
	private JdbcTemplate oracleTemplate;

	public BeanUtils() {
		
		String[] locations = {"applicationContext.xml"};
		
		@SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext(locations);
		mongoTemplate = (MongoTemplate)context.getBean("mongoTemplate");
		oracleTemplate = (JdbcTemplate)context.getBean("oracleTemplate");
		logger.info("applicationContext initalized!");
	}
	
	public MongoTemplate getMongo(){
		return this.mongoTemplate;
	}
	
	public JdbcTemplate getOracle(){
		return this.oracleTemplate;
	}
	
}
