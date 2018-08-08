package org.jiuzhou.policebd_inf.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

//使用spring自动生成单例对象，
@Component
public class FtpUtil {
	private final Logger logger = LoggerFactory.getLogger(FtpUtil.class);

	private static final int TIMEOUT = 5000; // 5s判断连接超时
	// 通过properties文件自动注入
	private String host; // ftp服务器ip
	private int port; // ftp服务器端口
	private String username;// 用户名
	private String password;// 密码
	private String basePath;// 存放文件的基本路径
	// 测试的时候把这个构造函数打开，设置你的初始值，然后在代码后面的main方法运行测试

	public FtpUtil() {
		// System.out.println(this.toString());
		host = "192.168.56.189";
		port = 21;
		username = "test";
		password = "123456";
		basePath = "/";
	}

	public FtpUtil(String host, int port, String username, String password, String basePath) {
		super();
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.basePath = basePath;
	}

	public FtpUtil(String host, int port, String username, String password) {
		super();
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.basePath = "/";
	}

	/**
	 * 
	 * @param path
	 *            上传文件存放在服务器的路径
	 * @param filename
	 *            上传文件名
	 * @param input
	 *            输入流
	 * @return
	 */
	public boolean fileUpload(String path, String filename, InputStream input) {
		if (filename == null || filename.isEmpty()) {
			logger.error("file name is null");
			return false;
		}

		FTPClient ftp = new FTPClient();
		ftp.setConnectTimeout(TIMEOUT);
		try {
			ftp.setControlEncoding("UTF-8");
			ftp.connect(host, port);
			ftp.login(username, password);

			ftp.enterLocalPassiveMode();

			// 获取状态码，判断是否连接成功
			if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
				throw new RuntimeException("FTP服务器拒绝连接");
			}
			// 转到上传文件的根目录
			if (!ftp.changeWorkingDirectory(basePath)) {

				throw new RuntimeException("根目录不存在，需要创建" + basePath);
			}
			// 判断是否存在目录
			if (!ftp.changeWorkingDirectory(path)) {
				String[] dirs = path.split("/");
				// 创建目录
				for (String dir : dirs) {
					if (null == dir || "".equals(dir))
						continue;
					// 判断是否存在目录
					if (!ftp.changeWorkingDirectory(dir)) {
						// 不存在则创建
						if (!ftp.makeDirectory(dir)) {
							throw new RuntimeException("子目录创建失败" + dir);
						}
						// 进入新创建的目录
						ftp.changeWorkingDirectory(dir);

					}
				}

			}
			// 设置上传文件的类型为二进制类型 linux
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			// 上传文件
			if (!ftp.storeFile(filename, input)) {
				return false;
			}
			return true;

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {

			try {
				ftp.logout();
				if (ftp.isConnected())
					ftp.disconnect();

			} catch (IOException e) {
				throw new RuntimeException(e);
			}

		}
	}

	/**
	 * 
	 * @param filename
	 *            文件名，注意！此处文件名为加路径文件名，如：/2015/06/04/aa.jpg
	 * @param localPath
	 *            存放到本地第地址
	 * @return
	 */
	public boolean downloadFile(String filename, String localPath) {
		FTPClient ftp = new FTPClient();
		boolean bfileExist = false;
		ftp.setConnectTimeout(TIMEOUT);
		try {
			System.out.println("host:" + host);
			ftp.setControlEncoding("UTF-8");
			ftp.connect(host, port);
			ftp.login(username, password);

			ftp.enterLocalPassiveMode();
			// 设置传输方式为流方式
			ftp.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
			// 获取状态码，判断是否连接成功
			if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
				throw new RuntimeException("FTP服务器拒绝连接");
			}

			int index = filename.lastIndexOf("/");
			// 获取文件的路径
			String path = filename.substring(0, index);
			// 获取文件名
			String name = filename.substring(index + 1);
			// 判断是否存在目录
			if (!ftp.changeWorkingDirectory(basePath + path)) {
				throw new RuntimeException("文件路径不存在：" + basePath + path);
			}
			// 获取该目录所有文件

			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			File localFile = new File(localPath + "/" + name);
			OutputStream out = new FileOutputStream(localFile);
			bfileExist = ftp.retrieveFile(name, out);
			out.flush();
			out.close();

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {

			try {
				ftp.logout();
				if (ftp.isConnected())
					ftp.disconnect();

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return bfileExist;
	}

	public boolean downloadFile(String filename, String localPath, String fileName) {
		boolean bfileExist = false;
		FTPClient ftp = new FTPClient();
		ftp.setConnectTimeout(TIMEOUT);
		try {
			ftp.setControlEncoding("UTF-8");
			ftp.connect(host, port);
			ftp.login(username, password);

			ftp.enterLocalPassiveMode();
			// 设置传输方式为流方式
			ftp.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
			// 获取状态码，判断是否连接成功
			if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
				throw new RuntimeException("FTP服务器拒绝连接");
			}

			int index = filename.lastIndexOf("/");
			// 获取文件的路径
			String path = filename.substring(0, index);
			// 获取文件名
			String name = filename.substring(index + 1);
			// 判断是否存在目录
			if (!ftp.changeWorkingDirectory(basePath + path)) {
				throw new RuntimeException("文件路径不存在：" + basePath + path);
			}
			// 获取该目录所有文件

			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			File localFile = new File(localPath + "/" + fileName);

			OutputStream out = new FileOutputStream(localFile);
			bfileExist = ftp.retrieveFile(name, out);
			out.flush();
			out.close();

		} catch (

		Exception e) {
			throw new RuntimeException(e);
		} finally {

			try {
				ftp.logout();
				if (ftp.isConnected())
					ftp.disconnect();

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return bfileExist;
	}

	public boolean deleteFile(String filename) {
		FTPClient ftp = new FTPClient();
		ftp.setConnectTimeout(TIMEOUT);
		try {
			ftp.setControlEncoding("UTF-8");
			ftp.connect(host, port);
			ftp.login(username, password);
			// 设置编码格式
			ftp.enterLocalPassiveMode();
			// 获取状态码，判断是否连接成功
			if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
				throw new RuntimeException("FTP服务器拒绝连接");
			}
			int index = filename.lastIndexOf("/");
			// 获取文件的路径
			String path = filename.substring(0, index);
			// 获取文件名
			String name = filename.substring(index + 1);
			// 判断是否存在目录,不存在则说明文件存在
			if (!ftp.changeWorkingDirectory(basePath + path)) {
				return true;
			}
			if (ftp.deleteFile(name)) {
				clearDirectory(ftp, basePath, path);

				return true;
			}
			return false;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {

			try {
				ftp.logout();
				if (ftp.isConnected())
					ftp.disconnect();

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * 
	 * @param ftp
	 * @param basePath
	 * @param path
	 *            以path为根，递归清除上面所有空的文件夹，直到出现不为空的文件夹停止，最多清除到basePath结束
	 * @throws IOException
	 */
	private void clearDirectory(FTPClient ftp, String basePath, String path) throws IOException {
		// 如果路径长度小于2，说明到顶了
		if (path.length() < 2) {
			return;
		}
		// 如果当前目录文件数目小于1则删除此目录
		if (ftp.listNames(basePath + path).length < 1) {
			// 删除目录
			System.out.println("删除目录：" + basePath + path);
			ftp.removeDirectory(basePath + path);
			int index = path.lastIndexOf("/");
			// 路径向上一层
			path = path.substring(0, index);
			// 继续判断
			clearDirectory(ftp, basePath, path);
		}

	}

	// 两个功能其中一个使用的话另一个需要注释
	public static void main(String[] args) throws UnsupportedEncodingException {
		// 上传测试--------------------------------------

		FileInputStream in = null;
		try {
			in = new FileInputStream(new File("D:\\car.png"));
			FtpUtil ftputil = new FtpUtil();
			boolean flag = ftputil.fileUpload("/2018/05/30", "car.png", in);
			System.out.println(flag);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		// 下载测试--------------------------------------
		String filename = "/2018/05/30/car.png";
		String localPath = "E:\\";
		FtpUtil ftputil = new FtpUtil();
		ftputil.downloadFile(new String(filename.getBytes("UTF-8")), localPath);
		ftputil.downloadFile(new String(filename.getBytes("UTF-8")), localPath, "123123");
		// 删除测试--------------------------------------

		// FtpUtil ftputil = new FtpUtil();
		// boolean flag = ftputil.deleteFile("/2018/05/30/123.jpg");
		// System.out.println(flag);

	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	@Override
	public String toString() {
		return "FtpUtil [host=" + host + ", port=" + port + ", username=" + username + ", password=" + password
				+ ", basePath=" + basePath + "]";
	}

}