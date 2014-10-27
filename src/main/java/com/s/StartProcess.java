package com.s;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 程序的入口，启动下载线程和上传线程
 * @author eric
 *
 */
public class StartProcess { 
	
	private static final ConcurrentLinkedQueue<EntityBean> concurrentLinkedQueue = new ConcurrentLinkedQueue<EntityBean>();
	
	public static void main(String args[]){
		
		BeanUtils beanUtils = new BeanUtils();
		MongoDao mdao = new MongoDao(beanUtils);
		List<EntityBean> listBean = mdao.queryList();
		
		MultiDownload download = new MultiDownload(listBean, concurrentLinkedQueue, mdao);
		MultiThreadUpload upload = new MultiThreadUpload(concurrentLinkedQueue, mdao);
		
		Thread downloadThread = new Thread(download);
		Thread uploadThread = new Thread(upload);
		
		uploadThread.start();
		downloadThread.start();
		
	}
	
}
