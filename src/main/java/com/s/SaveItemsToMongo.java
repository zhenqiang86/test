package com.s;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;

public class SaveItemsToMongo {

	private static Logger logger = LoggerFactory.getLogger(SaveItemsToMongo.class);
	private Connection conn=null;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
////	public Connection registerConn() throws SQLException,ClassNotFoundException {
////		Class.forName("oracle.jdbc.OracleDriver");
////		Connection conn=DriverManager.getConnection("192.168.5.117:1521/width117", "iflashbuy", "iflashbuy");
////		return conn;
////	}
////	
////	public Connection getConn(){
////		try {
////			if(conn==null){
////				conn=this.registerConn();
////			}
////			return conn;
////		} catch(Exception e){
////			logger.error("error occurred when getting connection ",e);
////			return null;
////		} 
////	}
//	
//	public List<EntityBean> executeQuery() throws Exception {
//		Connection conn = this.getConn();
//		Statement stmt = conn.createStatement(); 
//		ResultSet rst = stmt.executeQuery("select id,view_url from ent_upload where view_url not like '%group1%'");
//		List<EntityBean> list = new ArrayList<EntityBean>();
//		while(rst.next()){
//			EntityBean e = new EntityBean();
//			e.setId(rst.getInt(1));
//			e.setViewUrl(rst.getString(2));
//			list.add(e);
//		}
//		return list;
//	}
	
	public static void main(String[] args) {
		logger.debug("----------staring------------");
		long start = System.currentTimeMillis();
		BeanUtils beanUtils = new BeanUtils();
		MongoDao mdao = new MongoDao(beanUtils);
		List<EntityBean> listBean = mdao.queryList();
		for(EntityBean b : listBean){
			try {
				mdao.saveRecord(b);
			} catch(Exception e){
				logger.error("error occurred while saving entitybean["+b.toString()+"] to mongo", e);
			}
		}
		long finish = System.currentTimeMillis();
		logger.debug("----------finished------------, totaly millsecond: "+(finish-start));
	}
}
