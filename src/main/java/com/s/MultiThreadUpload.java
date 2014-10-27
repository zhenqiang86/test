package com.s;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 多线程下载图片，下载完一个即将图片加入到队列里面
 * @author Administrator
 * 
 */
public class MultiThreadUpload implements Runnable {
	
	protected static final Logger logger = LoggerFactory.getLogger(MultiThreadUpload.class);
	
	protected final ConcurrentLinkedQueue<EntityBean> concurrentLinkedQueue; 
	
	protected static final String fpath="/tmp/123456/";
	
	protected static final long queue_timeout = 10*60*1000;
	
	protected static final String ENDSINGEL = "FINISHED";

	protected MongoDao mdao;

	public MultiThreadUpload(ConcurrentLinkedQueue<EntityBean> concurrentLinkedQueue, MongoDao mdao){
		this.concurrentLinkedQueue = concurrentLinkedQueue;
		this.mdao = mdao;
	}
	
	public void run() {
		int taskcount = 0;
		
		try {
			List<FutureTask<EntityBean>> futureTaskList = new ArrayList<FutureTask<EntityBean>>();
			ExecutorService exec = Executors.newFixedThreadPool(10);
			
			logger.info("ready for uploading----------------------------");
			
			long timemills = 0;
			
			while(true){
				EntityBean entity = concurrentLinkedQueue.poll();
				if(entity == null){
					if(timemills ==0){
						timemills = System.currentTimeMillis();
					} else {
						long endmills = System.currentTimeMillis();
						long diffmills = endmills - timemills;
						if(diffmills > queue_timeout) {
							logger.error("the queue is closed because of timeout, no more tasks to be submitted to the executor");
							break;
						}
					}
					logger.info("I am still alive, waiting for  ");
					Thread.sleep(1000);
					continue;
				} else if(ENDSINGEL.equals(entity.getStatus())) {
					logger.error("accepted FINISHED signal, ready to leave queue: " + entity.toString());
					break;
				} else {
					FutureTask<EntityBean> ft = new FutureTask<EntityBean>(new UploadImg(entity));
					logger.info("entity ready to submit upload task, info: " + entity.toString());
					futureTaskList.add(ft);
					exec.submit(ft);
					timemills = 0;
					taskcount++;
					logger.info(" task["+taskcount+"] is submitted");
				}
			}
			logger.info(" total submited "+ taskcount +" uploading tasks--------");
			//处理futureTask返回的值
//			long finishedCount = 0l;
//			for (FutureTask<EntityBean> tempFt : futureTaskList) {
//				EntityBean ret = null;
//				try {
//					ret = tempFt.get();
//					mdao.saveRecord(ret);
//					finishedCount++;
//				} catch (InterruptedException e) {
//					logger.error("InterruptedException occurred in MultiThreadUpload.run() ",e);
//				} catch (ExecutionException e) {
//					logger.error("ExecutionException  occurred in MultiThreadUpload.run() ",e);
//				} catch (Exception e){
//					logger.error("exception occurred while execute mdao.update("+ret.toString()+")",e);
//				}
//			}
			exec.shutdown();
		} catch(Exception e){
			e.printStackTrace();
			logger.error("", e);
		}	
		
	}


	/**
	 * 
	 * @author Administrator
	 * 
	 */
	class UploadImg implements Callable<EntityBean> {
	
		private EntityBean e;
	
		public UploadImg(EntityBean e) {
			this.e = e;
		}
	
		public EntityBean call() {
			MultiThreadUpload.logger.debug("----ready to execute upload ( "+e.getFilePath()+" / "+e.getFileTempName()+" )");
			
			String fid = FastdfsUtil.upload(e, e.getFilePath(), e.getFileTempName());
			if(fid!=null){	
				e.setFileId(fid);
				e.setStatus("success");
			}
			
			try {
				mdao.saveRecord(e);
			} catch (InterruptedException se) {
				logger.error("InterruptedException occurred in MultiThreadUpload.run() ",se);
			} catch (ExecutionException se) {
				logger.error("ExecutionException  occurred in MultiThreadUpload.run() ",se);
			} catch (Exception se){
				logger.error("exception occurred while execute mdao.update("+e.toString()+")",se);
			}
			
			MultiThreadUpload.logger.debug("----finished execute upload ( "+e.getFilePath()+" status: "+e.getStatus()+")");

			try {
				File f = new File(e.getFilePath());
				if(f.exists())
					f.delete();
			} catch(Exception ee){
				MultiThreadUpload.logger.error("delete file error in UploadImg.call, file path="+e.getFilePath(), ee);
			}
			return e;
		}
	
	}
}
