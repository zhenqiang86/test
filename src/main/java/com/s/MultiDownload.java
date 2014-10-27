package com.s;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
public class MultiDownload implements Runnable {
	
	protected static final Logger logger = LoggerFactory.getLogger(MultiDownload.class);
	
	private final ConcurrentLinkedQueue<EntityBean> concurrentLinkedQueue; 
	
	protected static final String fpath="/tmp/123456/";
		
	protected static final String ENDSINGEL = "FINISHED";
	
	protected MongoDao mdao;
	
//	static {
//		BeanUtils beanUtils = new BeanUtils();
//		mdao = new MongoDao(beanUtils);
//	}
//	
	private List<EntityBean> listBean = null;

	public MultiDownload(List<EntityBean> listBean, ConcurrentLinkedQueue<EntityBean> concurrentLinkedQueue, MongoDao mdao){
		this.listBean = listBean;
		this.concurrentLinkedQueue = concurrentLinkedQueue;
		this.mdao = mdao;
	}
	
	public void run() {
		try {
			List<FutureTask<EntityBean>> futureTaskList = new ArrayList<FutureTask<EntityBean>>();
			ExecutorService exec = Executors.newFixedThreadPool(4);
			
			logger.info("----------------------starting downloading----------------------------");

			long cter = 0;
			for(EntityBean e : listBean){
				FutureTask<EntityBean> ft = new FutureTask<EntityBean>(new DownloadImg(e));
				futureTaskList.add(ft);
				exec.submit(ft);
				cter++;
				cter = cter%5000;
				if(cter==0){
					Thread.sleep(30000); //每下载5000个，休眠60s
					logger.info("sleep 30 seconds waiting for uploadThread to deal with files");
				} else {
					Thread.sleep(20);
				}
			}
	
			//处理futureTask返回的值
			int ct = 0;
			for (FutureTask<EntityBean> tempFt : futureTaskList) {
				try {
					EntityBean ret = tempFt.get();
					if(ret != null){
						logger.info("downloading finished task["+ct+"] info: "+ret.toString());
					}
					ct++;
				} catch (InterruptedException e) {
					logger.error("InterruptedException occurred in MultiDownload.run() ",e);
				} catch (ExecutionException e) {
					logger.error("ExecutionException  occurred in MultiDownload.run() ",e);
				}
			}
			logger.info("---------downloading finished, total downloading tasks executed ["+ct+"] ----------");
//			EntityBean et = new EntityBean();
//			et.setStatus(ENDSINGEL);
//			this.concurrentLinkedQueue.add(et);
			// 处理完毕，一定要记住关闭线程池，这个不能在统计之前关闭，因为如果线程多的话,执行中的可能被打断
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
	class DownloadImg implements Callable<EntityBean> {
	
		private EntityBean e;
	
		public DownloadImg(EntityBean e) {
			this.e = e;
		}
	
		public EntityBean call() {
			OutputStream os = null;
			InputStream is = null;
			String fp = null;
			try {
				String urlstr = this.e.getViewUrl();
				String fid = urlstr.substring(urlstr.lastIndexOf("/") + 1);
				long sid = Thread.currentThread().getId();
				Random rand = new Random();
				int ri = rand.nextInt(100);
				String prefix = new Long(sid).toString()+"_"+new Integer(ri).toString();
				fid = prefix + "_"+fid;
				String filePath = MultiDownload.fpath + fid;
				fp = filePath;
				URL url = new URL(urlstr);
				File outFile = new File(filePath);
				os = new FileOutputStream(outFile);
				is = url.openStream();
				byte[] buff = new byte[1024];
				while (true) {
					int readed = is.read(buff);
					if (readed == -1) {
						break;
					}
					byte[] temp = new byte[readed];
					System.arraycopy(buff, 0, temp, 0, readed);
					os.write(temp);
				}
				e.setFileTempName(fid);
				e.setFilePath(filePath);
				e.setStatus("downloaded");
				is.close();
				os.close();
				MultiDownload.this.concurrentLinkedQueue.add(e);
				logger.info("added to queue: "+e.toString());
				
				return e;
			} catch (Exception ee){
				MultiDownload.logger.error("", ee);
				e.setStatus("download failure");
				e.setMsg(ee.getMessage()+"  cause: "+ee.getCause());
				MultiDownload.this.mdao.updateStatus(e);
				if(fp!=null){
					File f = new File(fp);
					if(f.exists())
						f.delete();
				}
				MultiDownload.this.concurrentLinkedQueue.add(e);
				return null;
			} finally {
				try {
					if(is!=null){
						is.close();
					}
					if(os!=null){
						os.close();
					}
				} catch(Exception ee){
					
				}
			}	
		}
	
	}
}
