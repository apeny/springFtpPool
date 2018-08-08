package org.jiuzhou.policebd_inf.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FtpUtilPlus {

	private final Logger logger = LoggerFactory.getLogger(FtpUtilPlus.class);

	// stream byte[]
	// 通过properties文件自动注入
	private String host; // ftp服务器ip
	private int port; // ftp服务器端口
	private String username;// 用户名
	private String password;// 密码
	private String basePath;// 存放文件的基本路径
	// 测试的时候把这个构造函数打开，设置你的初始值，然后在代码后面的main方法运行测试

	// 两个功能其中一个使用的话另一个需要注释
	public static void main(String[] args) throws Exception {
		// 上传测试--------------------------------------

		FileInputStream in;
		String filepath = "13bb37701abe4b2487f2b51a62815ace";
		try {
			// in=new FileInputStream(new File("C:\\Users\\jj\\Desktop\\test.txt"));
			FtpUtilPlus ftputil = new FtpUtilPlus();
			String testByte = "woshi yige bytsdfsdfsdf";
			filepath = ftputil.fileUpload(null, testByte.getBytes());
			filepath = ftputil.fileUpload("car.png", "d:\\car.png");
			System.out.println(filepath);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
		// 下载测试--------------------------------------

		String localPath = "E:\\";
		FtpUtilPlus ftputil = new FtpUtilPlus();
		ftputil.downloadFile(new String(filepath.getBytes("UTF-8")), localPath);
		byte[] test = null;
		test = ftputil.downloadBytes(new String(filepath.getBytes("UTF-8")));
		InputStream out = ftputil.downloadStream(new String(filepath.getBytes("UTF-8")));

		System.out.println(out.available());

		// 删除测试--------------------------------------
		// FtpUtil ftputil=new FtpUtil();
		// boolean flag = ftputil.deleteFile(filepath);
		// System.out.println(flag);
	}

	// public static void main(String[] args) throws IOException {
	// FtpUtilPlus ftpUtilPlus = new FtpUtilPlus();
	// String img = "d:\\img\\pe.jpg";
	// // img = ImageUtil.getImageStr(img);
	// // String name = ftpUtilPlus.fileUpload(null, img.getBytes());
	// //
	// byte[] imgBytes =
	// ftpUtilPlus.downloadBytes("/2018/06/26/c893fe88325946c6a5c41f843708691f");
	// img = new String(imgBytes, "UTF-8");
	// System.out.println(img);
	//
	// }

	public FtpUtilPlus(String host, int port, String username, String password, String basePath) {
		super();
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.basePath = basePath;
	}

	public FtpUtilPlus() {
		// System.out.println(this.toString());
		host = "192.168.56.189";
		port = 21;
		username = "test";
		password = "123456";
		basePath = "/";
	}

	public static String getUUID32() {
		String uuid = UUID.randomUUID().toString().replace("-", "").toLowerCase();
		return uuid;
		// return UUID.randomUUID().toString().replace("-", "").toLowerCase();
	}

	/**
	 * @desc upload file
	 * @param docName
	 * @param localFile
	 * @return uri
	 */
	public String fileUpload(String docName, String localFile) {

		FileInputStream in;
		try {
			in = new FileInputStream(new File(localFile));
			String fileUrl = fileUpload(docName, in);
			return fileUrl;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		return null;
	}

	/**
	 * @desc upload byte[]
	 */
	public String fileUpload(String docName, byte[] byteStream) {

		InputStream in = null;
		try {
			in = new ByteArrayInputStream(byteStream);
			String fileUrl = fileUpload(docName, in);
			return fileUrl;
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

		return null;
	}

	/**
	 * @desc upload stream
	 * @param path
	 *            上传文件存放在服务器的路径
	 * @param filename
	 *            上传文件名
	 * @param input
	 *            输入流
	 * @return uri
	 */
	public String fileUpload(String docName, InputStream input) {
		if (input == null) {
			return null;
		}

		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH) + 1;
		int day = now.get(Calendar.DAY_OF_MONTH);
		String filename = getUUID32();
		if (docName != null) {
			String suffix = FilenameUtils.getExtension(docName);
			System.out.println("" + suffix);
			if (suffix != null && suffix.trim().length() > 0)
				filename = getUUID32() + "." + suffix;
		}

		final String pathTemplate = "/%04d/%02d/%02d";
		final String path = String.format(pathTemplate, year, month, day);

		FTPClient ftp = new FTPClient();
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
				throw new RuntimeException("根目录不存在，需要创建");
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
							throw new RuntimeException("子目录创建失败");
						}
						// 进入新创建的目录
						ftp.changeWorkingDirectory(dir);

					}
				}

			}
			// 设置上传文件的类型为二进制类型
			ftp.setFileType(FTP.BINARY_FILE_TYPE);

			// 上传文件

			if (!ftp.storeFile(filename, input)) {
				return null;
			}
			return path + "/" + filename;

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
	 * @desc get OutStream stream will prevent ftp close
	 * @param filename
	 *            文件名，注意！此处文件名为加路径文件名，如：/2015/06/04/aa.jpg
	 * @param localPath
	 *            存放到本地第地址
	 * @return
	 * @throws IOException
	 */

	public InputStream downloadStream(String filename) throws IOException {
		if (filename == null || filename.isEmpty()) {
			return null;
		}
		FTPClient ftp = new FTPClient();
		InputStream in = null, retStream = null;
		try {
			ftp.setControlEncoding("UTF-8");
			ftp.connect(host, port);
			ftp.login(username, password);
			// 设置文件编码格式

			// ftp通信有两种模式
			// PORT(主动模式)客户端开通一个新端口(>1024)并通过这个端口发送命令或传输数据,期间服务端只使用他开通的一个端口，例如21
			// PASV(被动模式)客户端向服务端发送一个PASV命令，服务端开启一个新端口(>1024),并使用这个端口与客户端的21端口传输数据
			// 由于客户端不可控，防火墙等原因，所以需要由服务端开启端口，需要设置被动模式
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

			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			in = ftp.retrieveFileStream(name);

			byte[] bytes = input2byte(in);
			retStream = new ByteArrayInputStream(bytes);
			in.close();
			ftp.completePendingCommand();

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
		return retStream;
	}

	/**
	 * @desc get OutStream
	 * @param filename
	 *            文件名，注意！此处文件名为加路径文件名，如：/2015/06/04/aa.jpg
	 * @param localPath
	 *            存放到本地第地址
	 * @return
	 * @throws IOException
	 */
	public byte[] downloadBytes(String filename) throws IOException {

		InputStream in = null;
		byte[] bytes = null;
		in = downloadStream(filename);
		if (in != null)
			bytes = input2byte(in);
		if (in != null)
			in.close();
		return bytes;

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
		try {

			ftp.setControlEncoding("UTF-8");
			ftp.connect(host, port);
			ftp.login(username, password);
			// 设置文件编码格式

			// ftp通信有两种模式
			// PORT(主动模式)客户端开通一个新端口(>1024)并通过这个端口发送命令或传输数据,期间服务端只使用他开通的一个端口，例如21
			// PASV(被动模式)客户端向服务端发送一个PASV命令，服务端开启一个新端口(>1024),并使用这个端口与客户端的21端口传输数据
			// 由于客户端不可控，防火墙等原因，所以需要由服务端开启端口，需要设置被动模式
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

	public boolean deleteFile(String filename) {
		FTPClient ftp = new FTPClient();
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

	/**
	 * 文件转成 byte[] <一句话功能简述> <功能详细描述>
	 * 
	 * @param inStream
	 * @return
	 * @throws IOException
	 * @see [类、类#方法、类#成员]
	 */
	public static byte[] input2byte(InputStream inStream) throws IOException {
		ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
		byte[] buff = new byte[100];
		int rc = 0;
		while ((rc = inStream.read(buff, 0, 100)) > 0) {
			swapStream.write(buff, 0, rc);
		}
		byte[] in2b = swapStream.toByteArray();

		swapStream.close();

		return in2b;
	}

}
