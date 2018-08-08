package org.jiuzhou.policebd_inf.utils;

import org.jiuzhou.policebd_inf.common.vo.DataResultVO;

import java.io.*;
import java.util.Base64;

/**
 * Created by xujl on 2018/7/12.
 */
public class DataUtil {

    /**
     * 将base64编码的字符串数据解码转成原始数据，把原始数据文件上传到ftp服务器
     * @param base64Data  base64编码字符串，如：data:image/jpeg;base64,......
     * @param ftpHost     ftp服务器地址
     * @param ftpPort     ftp服务器端口号
     * @param userName    访问ftp服务器的用户名
     * @param password    访问ftp服务器的密码
     * @return  DataResultVO.code值为200表示上传成功，DataResultVo.data表示数据存放地址，如ftp://192.168.100.56.123:21/2018/06/12/dataExample.jpeg
     */
    public DataResultVO transferBase64DataToFtpServer(String base64Data,
                                                      String ftpHost,
                                                      int    ftpPort,
                                                      String userName,
                                                      String password)
    {
        String expectedCode = "base64";
        int index = base64Data.indexOf("/");
        int eindex = base64Data.indexOf(";");
        String fileType = "." + base64Data.substring(index + 1, eindex);
        index = eindex;
        eindex = base64Data.indexOf(",");
        String enCode = base64Data.substring(index+1,eindex);

        if (!enCode.equals(expectedCode)){
            return new DataResultVO(DataResultVO.ERROR_CODE,"数据不是Base64编码");
        }

        String data =  base64Data.substring(eindex+1);
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
            return new DataResultVO(DataResultVO.ERROR_CODE,"Base64解码失败");
        }

        String uploadFilePath = "";
        try{
            FtpUtilPlus ftpUtilPlus = new FtpUtilPlus(ftpHost,ftpPort,userName,password,"/");
            uploadFilePath = ftpUtilPlus.fileUpload(fileType,b);
        }catch (Exception e) {
            return new DataResultVO(DataResultVO.ERROR_CODE,"上传ftp发生异常");
        }

        if (uploadFilePath == null){
            return new DataResultVO(DataResultVO.ERROR_CODE,"上传ftp失败");
        }

        String strFormat = "ftp://%s:%d%s";
        String fullFtpPath = String.format(strFormat,ftpHost,ftpPort,uploadFilePath);
        return new DataResultVO(DataResultVO.SUCCESS_CODE,DataResultVO.OPERATION_SUCCESS,fullFtpPath);
    }


    /**
     * 将base64编码的字符串数据解码转成原始数据，把原始数据文件上传到ftp服务器
     * @param base64File  保存base64编码数据的文件全路径，如/rootDir/image.txt，文件内容要求为UTF8编码格式
     * @param ftpHost     ftp服务器地址
     * @param ftpPort     ftp服务器端口号
     * @param userName    访问ftp服务器的用户名
     * @param password    访问ftp服务器的密码
     * @return  DataResultVO.code值为200表示上传成功，DataResultVo.data表示数据存放地址，如ftp://192.168.100.56.123:21/2018/06/12/dataExample.jpeg
     */
    public DataResultVO transferBase64FileToFtpServer(String base64File,
                                                      String ftpHost,
                                                      int    ftpPort,
                                                      String userName,
                                                      String password)
    {
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
            return new DataResultVO(DataResultVO.ERROR_CODE,"base64文件不存在");
        } catch (IOException e) {
            e.printStackTrace();
            return new DataResultVO(DataResultVO.ERROR_CODE,"读取base64文件失败");
        }
        try {
            strFileContent = new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            return new DataResultVO(DataResultVO.ERROR_CODE,"base64文件非UTF8格式");
        }

        DataResultVO resultVO = transferBase64DataToFtpServer(strFileContent,ftpHost,ftpPort,userName,password);
        return resultVO;
    }

}
