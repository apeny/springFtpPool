
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.jiuzhou.policebd_inf.utils.ftppool.client.FtpUtilInit;
import org.jiuzhou.policebd_inf.utils.ftppool.client.FtpUtilPlus;

/**
 * 试验 Java 的 Future 用法
 */
public class FutureTest {

	private static FtpUtilPlus ftpUtil = FtpUtilInit.getFtpUtil();
	// private static FtpUtil ftpUtil = new FtpUtil();

	public static class upLoadTask implements Callable<String> {
		private String param;

		public upLoadTask(String param) {
			super();
			this.param = param;
		}

		@Override
		public String call() throws Exception {
			String file = null;
			System.out.println("upload begin");
			file = ftpUtil.fileUpload(null, "d:/car.png");

			System.out.println("upload compelete");
			return file;
		}
	}

	public static class downLoadTask implements Callable<String> {
		private String param;

		public downLoadTask(String param) {
			super();
			this.param = param;
		}

		@Override
		public String call() throws Exception {
			System.out.println("down load begin");
			ftpUtil.downloadFile(param, "d:/lidx");
			System.out.println("down load stream begin");
			InputStream is = ftpUtil.downloadStream(param);
			System.out.println("down load stream end");
			System.out.println(is.available());
			byte[] barray = new byte[is.available()];
			is.read(barray, 0, is.available());
			FileUtils.writeByteArrayToFile(new File("d://lidx//" + param), barray);

			barray = ftpUtil.downloadBytes(param);
			FileUtils.writeByteArrayToFile(new File("d://lidx//" + param), barray);
			System.out.println("download end");
			return "";
		}
	}

	public static void main(String[] args) throws InterruptedException, ExecutionException {

		List<Future<String>> results = new ArrayList<Future<String>>();
		ExecutorService es = Executors.newCachedThreadPool();

		for (int i = 0; i < 20; i++) {
			results.add(es.submit(new upLoadTask("d:/car.png")));
			Thread.sleep(200);
		}

		for (Future<String> res : results) {

			Future<String> ret = es.submit(new downLoadTask(res.get()));

			Thread.sleep(200);
		}

	}
}
