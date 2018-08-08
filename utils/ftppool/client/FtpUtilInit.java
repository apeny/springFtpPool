package org.jiuzhou.policebd_inf.utils.ftppool.client;

import org.jiuzhou.policebd_inf.utils.ftppool.core.FTPClientPool;

/****
 * ftp pool 初始化函数 在resouces中添加 ftpPoolConfig.properties 线程池调用
 */

public class FtpUtilInit {

	private static FTPClientPool ftpClientPool;

	private static FtpUtilPlus ftpUtil;

	static {
		ftpClientPool = new FTPClientPool(
				Thread.currentThread().getContextClassLoader().getResourceAsStream("ftpPoolConfig.properties"));
		ftpUtil = new FtpUtilPlus();
		ftpUtil.setFtpClientPool(ftpClientPool);

	}

	public static FTPClientPool getFtpClientPool() {
		return ftpClientPool;
	}

	public static FtpUtilPlus getFtpUtil() {
		return ftpUtil;
	}

}
