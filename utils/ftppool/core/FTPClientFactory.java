package org.jiuzhou.policebd_inf.utils.ftppool.core;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.jiuzhou.policebd_inf.utils.ftppool.config.FTPPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FTPClientFactory extends BasePooledObjectFactory<FTPClient> {
	private static Logger logger = LoggerFactory.getLogger(FTPClientFactory.class);

	private FTPPoolConfig ftpConfig;

	public FTPClientFactory(FTPPoolConfig ftpConfig) {
		this.ftpConfig = ftpConfig;
	}

	/**
	 * 新建对象
	 */
	@Override
	public FTPClient create() throws Exception {
		FTPClient ftpClient = new FTPClient();
		ftpClient.setConnectTimeout(ftpConfig.getClientTimeout());
		try {
			logger.info("1");
			ftpClient.connect(ftpConfig.getHost(), ftpConfig.getPort());
			int reply = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftpClient.disconnect();
				logger.error("FTPServer 拒绝连接");
				return null;
			}
			logger.info("2");
			boolean result = ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
			if (!result) {
				logger.error("ftpClient登陆失败!");
				throw new Exception("ftpClient登陆失败! userName:" + ftpConfig.getUsername() + " ; password:"
						+ ftpConfig.getPassword());
			}
			logger.info("3");
			ftpClient.setFileType(ftpConfig.getTransferFileType());
			ftpClient.setBufferSize(ftpConfig.getBufferSize());
			ftpClient.setControlEncoding(ftpConfig.getEncoding());
			logger.info("4");
			if (ftpConfig.getPassiveMode()) {
				ftpClient.enterLocalPassiveMode();
			}
			logger.info("5");
			ftpClient.changeWorkingDirectory(ftpConfig.getWorkingDirectory());
		} catch (IOException e) {

			logger.error("FTP连接失败：", e);
		}
		return ftpClient;
	}

	@Override
	public PooledObject<FTPClient> wrap(FTPClient ftpClient) {
		return new DefaultPooledObject<FTPClient>(ftpClient);
	}

	/**
	 * 销毁对象
	 */
	@Override
	public void destroyObject(PooledObject<FTPClient> p) throws Exception {
		FTPClient ftpClient = p.getObject();
		ftpClient.logout();
		super.destroyObject(p);
	}

	/**
	 * 验证对象
	 */
	@Override
	public boolean validateObject(PooledObject<FTPClient> p) {
		FTPClient ftpClient = p.getObject();
		boolean connect = false;
		try {
			connect = ftpClient.sendNoOp();
			if (connect) {
				ftpClient.changeWorkingDirectory(ftpConfig.getWorkingDirectory());
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				ftpClient.logout();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				ftpClient.connect(ftpConfig.getHost(), ftpConfig.getPort());
				int reply = ftpClient.getReplyCode();
				if (!FTPReply.isPositiveCompletion(reply)) {
					ftpClient.disconnect();
					logger.error("FTPServer 拒绝连接");
					return connect;
				}
				boolean result = ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
				if (!result) {
					logger.error("ftpClient登陆失败! userName:" + ftpConfig.getUsername() + " ; password:"
							+ ftpConfig.getPassword());
					return connect;
				}
				ftpClient.setFileType(ftpConfig.getTransferFileType());
				ftpClient.setBufferSize(ftpConfig.getBufferSize());
				ftpClient.setControlEncoding(ftpConfig.getEncoding());
				if (ftpConfig.getPassiveMode()) {
					ftpClient.enterLocalPassiveMode();
				}
				ftpClient.changeWorkingDirectory(ftpConfig.getWorkingDirectory());
				return true;
			} catch (IOException ex) {
				logger.error("FTP连接失败：", ex);
			}
		}
		return connect;
	}
}