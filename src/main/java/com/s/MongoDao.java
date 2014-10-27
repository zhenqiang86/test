package com.s;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.jdbc.core.RowMapper;

public class MongoDao {
	
	private final BeanUtils b;
	
	public MongoDao(BeanUtils b){
		this.b = b;
	}
	
	/**
	 * 更新EntityBean，记录下载和上传的情况
	 * @param e
	 * @throws Exception
	 */
	public void saveRecord(EntityBean e) throws Exception {
		Query query = new Query();
		Criteria criteria = new Criteria("id");
		criteria.is(e.getId());
		query.addCriteria(criteria);
		Update update=new Update();
		update.set("id",e.getId()).set("viewUrl", e.getViewUrl());
		if(e.getFileId()!=null)
			update.set("fileId", e.getFileId());
		if(e.getFilePath()!=null)
			update.set("filePath", e.getFilePath());
		if(e.getStatus()!=null)
			update.set("status", e.getStatus());
		if(e.getMsg()!=null)
			update.set("msg", e.getMsg());
		
		FindAndModifyOptions fo = new FindAndModifyOptions();
		fo.upsert(true);
		this.b.getMongo().findAndModify(query, update, fo, EntityBean.class, "irecord");
	}
	
	/**
	 * 更新EntityBean在mongo中的状态
	 * @param e
	 */
	@Deprecated
	public void updateStatus(EntityBean e) {
		Query query = new Query();
		Criteria criteria = new Criteria("id");
		criteria.is(e.getId());
		query.addCriteria(criteria);
		Update update=new Update();
		update.set("status",e.getStatus()).set("msg", e.getMsg()).set("filePath", e.getFilePath());
		FindAndModifyOptions fo = new FindAndModifyOptions();
		fo.upsert(true);
		this.b.getMongo().findAndModify(query, update, fo, EntityBean.class, "irecord");
	}

	/**
	 * 从mongo中获取待处理的图片信息
	 * @return
	 */
	public List<EntityBean> queryListFromMongo(){
		Query query = new Query();
		query.limit(10000);
		query.skip(5000);
		Field fields = query.fields();
		fields.include("_id").include("viewUrl");
		List<EntityBean> lst = this.b.getMongo().find(query, EntityBean.class, "irecord");
		return lst;
	}
	
	/**
	 * 获取要处理的图片信息，包含ID和url，从oracle获取
	 * @return
	 */
	public List<EntityBean> queryList(){
		String sql = "select id,view_url as viewUrl from ent_upload where status is null and view_url not like '%/.%'"; 
		EntityBeanRowMapper rowMapper = new EntityBeanRowMapper();
		List<EntityBean> list = this.b.getOracle().query(sql, rowMapper);
		return list;
	}
	
	public class EntityBeanRowMapper implements RowMapper<EntityBean> {

		public EntityBean mapRow(ResultSet rs, int rowNum) throws SQLException {
			// TODO Auto-generated method stub
			EntityBean e = new EntityBean();
			e.setId(rs.getInt("id"));
			e.setViewUrl(rs.getString("viewUrl"));
			return e;
		}		
	}
}
