package com.s;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import net.mikesu.fastdfs.FastdfsClient;
import net.mikesu.fastdfs.FastdfsClientFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName: FdfsUtil
 * @Description: FastDFS client
 *
 * @Author: zhenqiang@iflashbuy.com
 * @Create date 
 * @Copyright (c) www.iflashbuy.com
 */
public class FastdfsUtil {

	private static Logger logger = LoggerFactory.getLogger(FastdfsUtil.class);

	public static String upload(String file, byte[] buffer){
		return null;
	}
	
	public static String upload(EntityBean entityBean, String fpath, String fileName) {
		File f=new File(fpath);
		return upload(entityBean, f, fileName);
	}
	
	
	public static String upload(EntityBean entityBean, File file, String filename){
		FastdfsClient fastdfsClient = null;
		String fileId = null;
		try {
			fastdfsClient = FastdfsClientFactory.getFastdfsClient();
			fileId=fastdfsClient.upload(file, filename);
		} catch (Exception e) {
			logger.error("上传图片发生异常", e);
			entityBean.setStatus("upload failure");
			entityBean.setMsg(e.getMessage());
		} finally {
			fastdfsClient.close();
		}
		return fileId;
	}
	
	public static String getUrl(String fileId) {
		FastdfsClient fastdfsClient = null;
		String url = null;
		try {
			fastdfsClient = FastdfsClientFactory.getFastdfsClient();
			url = fastdfsClient.getUrl(fileId);	
		} catch (Exception e) {
			logger.error("获取图片发生异常", e);
		} finally {
			fastdfsClient.close();
		}
		return url;
		
	}
	
	/**
	 * 根据返回的URL获取文件流
	 * @param path
	 * @return
	 */
	public static byte[] returnBitMap(String path) {  
	        URL url = null;  
	        InputStream is =null;  
	        byte[] in_b;
	        try {  
	            url = new URL(path);  
	        } catch (MalformedURLException e) {  
	            e.printStackTrace();  
	            logger.error("error parsing url "+path, e);
	         }  
	        try {  
	            HttpURLConnection conn = (HttpURLConnection) url.openConnection();//利用HttpURLConnection对象,我们可以从网络中获取网页数据.  
	            conn.setDoInput(true);  
	            conn.connect();  
	            is = conn.getInputStream(); //得到网络返回的输入流  
	              
		        int bytesize=is.available();
		        ByteArrayOutputStream outStream = new ByteArrayOutputStream();  
		        byte[] data = new byte[bytesize];  
		        int count = -1;  
		        while((count = is.read(data,0,bytesize)) != -1)  
		            outStream.write(data, 0, count);  
		          
		        data = null;  
		        in_b = outStream.toByteArray();
		        
		        is.close();
		        outStream.close();
		        return in_b;  
	        } catch (IOException e) {  
	            e.printStackTrace();
	            logger.error("error connecting url "+path, e);
	            return null;
	        } 
	 }    
	
	
}
