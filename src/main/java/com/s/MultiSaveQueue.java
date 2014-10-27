package com.s;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiSaveQueue implements Runnable {

	protected static final Logger logger = LoggerFactory.getLogger(MultiSaveQueue.class);
		
	private MongoDao mdao;
	
	private volatile List<FutureTask<EntityBean>> futureTaskList;
	
	private ExecutorService exec;
	
	public MultiSaveQueue(ExecutorService exec,List<FutureTask<EntityBean>> futureTaskList){
		this.exec = exec;
		this.futureTaskList = futureTaskList;
	}
	
	@Override
	public void run() {
		int finishedCount = 0;
		for (FutureTask<EntityBean> tempFt : futureTaskList) {
			EntityBean ret = null;
			try {
				ret = tempFt.get();
				mdao.saveRecord(ret);
				finishedCount++;
			} catch (InterruptedException e) {
				logger.error("InterruptedException occurred in MultiThreadUpload.run() ",e);
			} catch (ExecutionException e) {
				logger.error("ExecutionException  occurred in MultiThreadUpload.run() ",e);
			} catch (Exception e){
				logger.error("exception occurred while execute mdao.update("+ret.toString()+")",e);
			}
		}
		
		this.exec.shutdown();
		
	}
}
