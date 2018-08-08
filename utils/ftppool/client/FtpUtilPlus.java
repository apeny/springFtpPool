package org.jiuzhou.policebd_inf.utils.ftppool.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Calendar;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.jiuzhou.policebd_inf.common.vo.DataResultVO;
import org.jiuzhou.policebd_inf.utils.ftppool.core.FTPClientPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FtpUtilPlus {

	private FTPClientPool ftpClientPool;

	private final Logger logger = LoggerFactory.getLogger(FtpUtilPlus.class);

	public void setFtpClientPool(FTPClientPool ftpClientPool) {
		this.ftpClientPool = ftpClientPool;
	}

	// 两个功能其中一个使用的话另一个需要注释
	// public static void main(String []args) throws Exception {
	// //上传测试--------------------------------------
	//
	// FileInputStream in;
	// String filepath = "13bb37701abe4b2487f2b51a62815ace";
	// try {
	//// in=new FileInputStream(new File("C:\\Users\\jj\\Desktop\\test.txt"));
	// FtpUtilPlus ftputil=new FtpUtilPlus();
	// String testByte = "woshi yige byte[]";
	// filepath=ftputil.fileUpload(null, testByte.getBytes());
	// System.out.println(filepath);
	// }catch (Exception e) {
	// e.printStackTrace();
	// }finally {
	//
	// }
	// //下载测试--------------------------------------
	//
	// String localPath="E:\\";
	// FtpUtilPlus ftputil=new FtpUtilPlus();
	// ftputil.downloadFile(new String(filepath.getBytes("UTF-8")), localPath);
	// byte[] test = null;
	// test = ftputil.downloadBytes(new String(filepath.getBytes("UTF-8")));
	//
	// System.out.println(test.toString());
	//
	//
	//
	// //删除测试--------------------------------------
	// // FtpUtil ftputil=new FtpUtil();
	// boolean flag=ftputil.deleteFile(filepath);
	// System.out.println(flag);
	// }

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
		if (localFile == null || localFile.isEmpty()) {
			logger.error("localFile is null");
			return null;
		}
		FileInputStream in = null;
		try {
			in = new FileInputStream(new File(localFile));
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
	 * @desc upload byte[]
	 */
	public String fileUpload(String docName, byte[] byteStream) {
		if (byteStream == null || byteStream.length == 0) {
			logger.error("byte is null or empty");
			return null;
		}
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
	 * @throws IOException
	 */
	public String fileUpload(String docName, InputStream input) throws IOException {
		if (input == null || input.available() == 0) {
			logger.error("input stream is null");
			return null;
		}
		FTPClient ftp = null;
		String basePath = ftpClientPool.getFtpPoolConfig().getWorkingDirectory();
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH) + 1;
		int day = now.get(Calendar.DAY_OF_MONTH);
		String filename = getUUID32();
		if (docName != null) {
			String suffix = FilenameUtils.getExtension(docName);
			if (suffix != null && suffix.trim().length() > 0)
				filename = getUUID32() + "." + suffix;
		}

		final String pathTemplate = "/%04d/%02d/%02d";
		final String path = String.format(pathTemplate, year, month, day);

		try {
			ftp = ftpClientPool.borrowObject();

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

			// 上传文件
			if (!ftp.storeFile(filename, input)) {
				return null;
			}

			return path + "/" + filename;

		} catch (Exception e) {
			logger.error("file upload error");
			throw new RuntimeException(e);
		} finally {
			if (ftp != null) {
				ftpClientPool.returnObject(ftp);
			}
		}
	}

	/**
	 * @warn stream will persist ftp close
	 * @desc get OutStream
	 * @param filename
	 *            文件名，注意！此处文件名为加路径文件名，如：/2015/06/04/aa.jpg
	 * @param localPath
	 *            存放到本地第地址
	 * @return
	 * @throws IOException
	 */

	public InputStream downloadStream(String filename) throws IOException {
		if (filename == null || filename.isEmpty()) {
			logger.error("file name is empty");
			return null;
		}
		FTPClient ftp = null;
		InputStream in = null, retStream = null;

		String basePath = ftpClientPool.getFtpPoolConfig().getWorkingDirectory();
		try {
			ftp = ftpClientPool.borrowObject();
			// 获取文件的路径
			String path = FilenameUtils.getPath(filename);
			// 获取文件名
			String name = FilenameUtils.getName(filename);
			// 判断是否存在目录
			String folder = basePath + "/" + path;

			if (!ftp.changeWorkingDirectory(folder)) {
				throw new RuntimeException("文件路径不存在：" + folder);
			}

			in = ftp.retrieveFileStream(name);
			if (in != null) {
				byte[] bytes = input2byte(in);
				retStream = new ByteArrayInputStream(bytes);
				in.close();
				ftp.completePendingCommand();
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (ftp != null) {
				ftpClientPool.returnObject(ftp);
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
		if (filename == null || filename.isEmpty()) {
			logger.error("file name is empty");
			return null;
		}
		FTPClient ftp = null;
		InputStream in = null;
		byte[] bytes = null;

		String basePath = ftpClientPool.getFtpPoolConfig().getWorkingDirectory();
		try {
			ftp = ftpClientPool.borrowObject();
			// 获取文件的路径
			String path = FilenameUtils.getPath(filename);
			// 获取文件名
			String name = FilenameUtils.getName(filename);
			// 判断是否存在目录
			String folder = basePath + "/" + path;

			if (!ftp.changeWorkingDirectory(folder)) {
				throw new RuntimeException("文件路径不存在：" + folder);
			}

			in = ftp.retrieveFileStream(name);
			if (in != null) {
				bytes = input2byte(in);
				in.close();
				ftp.completePendingCommand(); // in不为空的时候需要通知服务器下载完成，除retrieveFileStream操作以外其他方法加上这个操作会造成线程池不能正常return
												// // 从而阻塞borrow
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {

			if (ftp != null) {

				ftpClientPool.returnObject(ftp);
			}
		}

		return bytes;
	}

	/**
	 * 
	 * @param filename
	 *            文件名，注意！此处文件名为加路径文件名，如：/2015/06/04/aa.jpg
	 * @param localPath
	 *            存放到本地第地址
	 * @return
	 * @throws IOException
	 */
	public boolean downloadFile(String filename, String localPath) throws IOException {
		if (filename == null || filename.isEmpty()) {
			logger.error("filename is empty");
			return false;
		}
		FTPClient ftp = null;
		String basePath = ftpClientPool.getFtpPoolConfig().getWorkingDirectory();
		try {

			ftp = ftpClientPool.borrowObject();
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
			boolean bfileExist = false;
			FTPFile[] files = ftp.listFiles();
			for (FTPFile file : files) {
				// 判断是否有目标文件

				// System.out.println("文件名"+file.getName()+"---"+name);
				if (file.getName().equals(name)) {
					// 如果找到，将目标文件复制到本地
					File localFile = new File(localPath + "/" + file.getName());

					OutputStream out = new FileOutputStream(localFile);
					ftp.retrieveFile(file.getName(), out);
					out.close();
					bfileExist = true;
				}
			}
			return bfileExist;
		} catch (Exception e) {

			throw new RuntimeException(e);
		} finally {
			if (ftp != null) {
				ftpClientPool.returnObject(ftp);
			} else
				logger.info("ftp is null");
		}

	}

	public boolean deleteFile(String fileuri) {
		FTPClient ftp = null;
		String basePath = ftpClientPool.getFtpPoolConfig().getWorkingDirectory();
		try {
			ftp = ftpClientPool.borrowObject();

			int index = fileuri.lastIndexOf("/");
			// 获取文件的路径
			String path = fileuri.substring(0, index);
			// 获取文件名
			String name = fileuri.substring(index + 1);

			// 判断是否存在目录,不存在则说明文件不存在
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
			if (ftp != null) {
				ftpClientPool.returnObject(ftp);
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

	/**
	 * 将base64编码的字符串数据解码转成原始数据，把原始数据文件上传到ftp服务器
	 * 
	 * @param base64Data
	 *            base64编码字符串，如：data:image/jpeg;base64,......
	 * @return DataResultVO.code值为200表示上传成功，DataResultVo.data表示数据存放地址，如ftp://192.168.100.56.123:21/2018/06/12/dataExample.jpeg
	 */
	public DataResultVO transferBase64DataToServer(String base64Data) {
		String expectedCode = "base64";
		int index = base64Data.indexOf("/");
		int eindex = base64Data.indexOf(";");
		String fileType = "." + base64Data.substring(index + 1, eindex);
		index = eindex;
		eindex = base64Data.indexOf(",");
		String enCode = base64Data.substring(index + 1, eindex);

		if (!enCode.equals(expectedCode)) {
			return new DataResultVO(DataResultVO.ERROR_CODE, "数据不是Base64编码");
		}

		String data = base64Data.substring(eindex + 1);
		Base64.Decoder decoder = Base64.getDecoder();
		byte[] b;
		try {
			// 解密
			b = decoder.decode(data);
			// 处理数据
			for (int i = 0; i < b.length; ++i) {
				if (b[i] < 0) {
					b[i] += 256;
				}
			}
		} catch (Exception e) {
			return new DataResultVO(DataResultVO.ERROR_CODE, "Base64解码失败");
		}

		String uploadFilePath = "";
		try {
			uploadFilePath = fileUpload(fileType, b);
		} catch (Exception e) {
			return new DataResultVO(DataResultVO.ERROR_CODE, "上传ftp发生异常");
		}

		if (uploadFilePath == null) {
			return new DataResultVO(DataResultVO.ERROR_CODE, "上传ftp失败");
		}

		String format = "ftp://%s:%d%s";
		String ftpHost = ftpClientPool.getFtpPoolConfig().getHost();
		int ftpPort = ftpClientPool.getFtpPoolConfig().getPort();

		String fullFtpPath = String.format(format, ftpHost, ftpPort, uploadFilePath);
		return new DataResultVO(DataResultVO.SUCCESS_CODE, DataResultVO.OPERATION_SUCCESS, fullFtpPath);
	}

	/**
	 * 将base64编码的字符串数据解码转成原始数据，把原始数据文件上传到ftp服务器
	 * 
	 * @param base64File
	 *            保存base64编码数据的文件全路径，如/rootDir/image.txt，文件内容要求为UTF8编码格式
	 * @return DataResultVO.code值为200表示上传成功，DataResultVo.data表示数据存放地址，如ftp://192.168.100.56.123:21/2018/06/12/dataExample.jpeg
	 */
	public DataResultVO transferBase64FileToServer(String base64File) {
		String fileName = base64File;
		String encoding = "UTF-8";
		String strFileContent = "";
		File file = new File(fileName);
		Long filelength = file.length();
		byte[] filecontent = new byte[filelength.intValue()];
		try {
			FileInputStream in = new FileInputStream(file);
			in.read(filecontent);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return new DataResultVO(DataResultVO.ERROR_CODE, "base64文件不存在");
		} catch (IOException e) {
			e.printStackTrace();
			return new DataResultVO(DataResultVO.ERROR_CODE, "读取base64文件失败");
		}
		try {
			strFileContent = new String(filecontent, encoding);
		} catch (UnsupportedEncodingException e) {
			return new DataResultVO(DataResultVO.ERROR_CODE, "base64文件非UTF8格式");
		}

		DataResultVO resultVO = transferBase64DataToServer(strFileContent);
		return resultVO;
	}
}
