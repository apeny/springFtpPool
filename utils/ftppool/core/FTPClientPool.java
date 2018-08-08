package org.jiuzhou.policebd_inf.utils.ftppool.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.jiuzhou.policebd_inf.utils.ftppool.config.FTPPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FTPClientPool {
	private GenericObjectPool<FTPClient> ftpClientPool;
	private FTPPoolConfig ftpConfig = new FTPPoolConfig();

	/**
	 * ftp客户端工厂
	 */

	private static Logger log = LoggerFactory.getLogger(FTPClientPool.class);

	public FTPClientPool(InputStream in) {
		Properties pro = new Properties();
		try {
			pro.load(in);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		// 初始化对象池配置
		GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
		poolConfig.setBlockWhenExhausted(Boolean.parseBoolean(pro.getProperty("ftpClient_blockWhenExhausted")));
		poolConfig.setMaxWaitMillis(Long.parseLong(pro.getProperty("ftpClient_maxWait")));
		poolConfig.setMinIdle(Integer.parseInt(pro.getProperty("ftpClient_minIdle")));
		poolConfig.setMaxIdle(Integer.parseInt(pro.getProperty("ftpClient_maxIdle")));
		poolConfig.setMaxTotal(Integer.parseInt(pro.getProperty("ftpClient_maxTotal")));
		poolConfig.setTestOnBorrow(Boolean.parseBoolean(pro.getProperty("ftpClient_testOnBorrow")));
		poolConfig.setTestOnReturn(Boolean.parseBoolean(pro.getProperty("ftpClient_testOnReturn")));
		poolConfig.setTestOnCreate(Boolean.parseBoolean(pro.getProperty("ftpClient_testOnCreate")));
		poolConfig.setTestWhileIdle(Boolean.parseBoolean(pro.getProperty("ftpClient_testWhileIdle")));
		poolConfig.setLifo(Boolean.parseBoolean(pro.getProperty("ftpClient_lifo")));

		ftpConfig.setHost(pro.getProperty("ftpClient_host"));
		ftpConfig.setPort(Integer.parseInt(pro.getProperty("ftpClient_port")));
		ftpConfig.setUsername(pro.getProperty("ftpClient_username"));
		ftpConfig.setPassword(pro.getProperty("ftpClient_pasword"));
		ftpConfig.setClientTimeout(Integer.parseInt(pro.getProperty("ftpClient_clientTimeout")));
		ftpConfig.setEncoding(pro.getProperty("ftpClient_encoding"));
		ftpConfig.setWorkingDirectory(pro.getProperty("ftpClient_workingDirectory"));
		ftpConfig.setPassiveMode(Boolean.parseBoolean(pro.getProperty("ftpClient_passiveMode")));
		ftpConfig.setRenameUploaded(Boolean.parseBoolean(pro.getProperty("ftpClient_renameUploaded")));
		ftpConfig.setRetryTimes(Integer.parseInt(pro.getProperty("ftpClient_retryTimes")));
		ftpConfig.setTransferFileType(Integer.parseInt(pro.getProperty("ftpClient_transferFileType")));
		ftpConfig.setBufferSize(Integer.parseInt(pro.getProperty("ftpClient_bufferSize")));
		// 初始化对象池
		ftpClientPool = new GenericObjectPool<FTPClient>(new FTPClientFactory(ftpConfig), poolConfig);

	}

	public FTPClient borrowObject() throws Exception {
		/*
		 * System.out.println("获取前"); System.out.println("活动" +
		 * ftpClientPool.getNumActive()); System.out.println("等待" +
		 * ftpClientPool.getNumWaiters()); System.out.println("----------");
		 */
		FTPClient ftpClient = null;
		Exception ex = null;
		// 获取连接最多尝试3次
		for (int i = 0; i < 3; i++) {
			try {
				ftpClient = ftpClientPool.borrowObject();
				break;
			} catch (Exception e) {
				ex = e;

			}
		}
		if (ftpClient == null) {
			log.error("borrow ftpClient error");
			throw new RuntimeException("Could not get a ftpClient from the pool", ex);
		}
		return ftpClient;

	}

	public void returnObject(FTPClient ftpClient) {
		if (ftpClient == null) {
			log.info("client is null");
			return;
		}

		try {

			ftpClientPool.returnObject(ftpClient);
		} catch (Exception e) {
			log.error("Could not return the ftpClient to the pool", e);
			// destoryFtpClient

			if (ftpClient.isAvailable()) {
				try {
					ftpClient.disconnect();
				} catch (IOException io) {
				}
			}
		}
		/**
		 * System.out.println("归还后"); System.out.println("活动" +
		 * ftpClientPool.getNumActive()); System.out.println("等待" +
		 * ftpClientPool.getNumWaiters()); System.out.println("----------");
		 */
	}

	public FTPPoolConfig getFtpPoolConfig() {

		return ftpConfig;
	}
}