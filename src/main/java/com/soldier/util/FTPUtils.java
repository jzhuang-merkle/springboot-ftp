package com.soldier.util;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * @Author soldier
 * @Date 20-8-8 下午5:09
 * @Email:583406411@qq.com
 * @Version 1.0
 * @Description:
 */
@Component
public class FTPUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(FTPUtils.class);

    /**
     * FTP 配置
     */
    @Value("${ftp.host}")
    private String host;//ip地址
    @Value("${ftp.port}")
    private int port;//端口号
    @Value("${ftp.username}")
    private String username;//用户名
    @Value("${ftp.password}")
    private String password;//密码
    @Value("${ftp.remoteDir}")
    private String remoteDir;//远程服务器文件保存的目录
    @Value("${ftp.localDir}")
    private String localDir;//本地文件保存的目录

    /**
     * ftp登录初始化
     */
    private FTPClient connectFtpServer() {
        FTPClient ftpClient = new FTPClient();
        ftpClient.setConnectTimeout(1000 * 30);//设置连接超时时间
        ftpClient.setControlEncoding("utf-8");//设置ftp字符集
        ftpClient.enterLocalPassiveMode();//设置被动模式，文件传输端口设置
        try {
            ftpClient.connect(host, port);
            ftpClient.login(username, password);
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                LOGGER.error("connect ftp {} failed", host);
                ftpClient.disconnect();
                return null;
            }
            //设置文件传输模式为二进制，可以保证传输的内容不会被改变
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            LOGGER.info("replyCode==========={}", replyCode);
        } catch (IOException e) {
            LOGGER.error("connect fail ------->>>{}", e.getMessage());
            return null;
        }
        return ftpClient;
    }

    /**
     * 文件上传
     *
     * @param inputStream 待上传文件的输入流
     * @param appendDir   文件保存前要拼接的路径
     * @param originName  文件保存时的名字(包含后缀)
     */
    public void uploadFile(InputStream inputStream, String appendDir, String originName) {
        FTPClient client = connectFtpServer();
        if (client == null) {
            return;
        }
        try {
            // 文件夹不存在时新建
            String realDir = remoteDir + appendDir;
            if (!changeWorkingDir(client, realDir)) {
                // 直接进入finally
                return;
            }
            //保存文件
            if (!client.storeFile(originName, inputStream)) {
                // 抛出自定义异常
//                throw new BusinessException(ResponseCode.UPLOAD_FILE_FAIL_CODE,originName+"---》上传失败！");
                LOGGER.error("{}---》上传失败！", originName);
            }
            LOGGER.info("{}---》上传成功！", originName);

            // 退出登录
            client.logout();
        } catch (IOException e) {
            LOGGER.error("{}---》上传失败！", originName);
            // 抛出自定义异常
//            throw new BusinessException(ResponseCode.UPLOAD_FILE_FAIL_CODE,originName+"上传失败！");
        } finally {
            if (client.isConnected()) {
                try {
                    client.disconnect();
                } catch (IOException e) {
                    LOGGER.error("disconnect fail ------->>>{}", e.getMessage());
                }
            }
        }
    }

    /**
     * 上传文件夹到ftp上
     *
     * @param appendDir 文件保存前要拼接的路径
     * @param localPath 本地上传的文件夹路径名称
     */
    public void uploadDir(String appendDir, String localPath) {
        FTPClient client = connectFtpServer();
        localPath = localPath.replace("\\\\", "/");
        File file = new File(localPath);
        FileInputStream fileInputStream = null;
        if (file.exists()) {
            try {
                // 文件夹不存在时新建
                String realDir = remoteDir + appendDir;
                if (!changeWorkingDir(client, realDir)) {
                    // 直接进入finally
                    return;
                }
                File[] files = file.listFiles();
                if (null != files) {
                    String ftpFileName = null;
                    for (File f : files) {
                        if (f.isDirectory() && !f.getName().equals(".") && !f.getName().equals("..")) {
                            uploadDir(appendDir, f.getPath());
                        } else if (f.isFile()) {
                            fileInputStream = new FileInputStream(f);
                            ftpFileName = f.getName();
                            if (client.storeFile(ftpFileName, fileInputStream)) {
                                LOGGER.info("{}---》上传成功！保存路径为：【{}】", ftpFileName, realDir);
                            } else {
                                LOGGER.error("{}---》上传失败！", ftpFileName);
                            }
                        }
                    }
                }

                // 退出登录
                client.logout();
            } catch (IOException e) {
                LOGGER.error("文件夹：{}---》上传失败！", localPath);
                // 抛出自定义异常
//            throw new BusinessException(ResponseCode.UPLOAD_FILE_FAIL_CODE,originName+"上传失败！");
            } finally {
                if (client.isConnected()) {
                    try {
                        client.disconnect();
                    } catch (IOException e) {
                        LOGGER.error("disconnect fail ------->>>{}", e.getMessage());
                    }
                }
                // 关闭文件输入流
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        LOGGER.error("fileInputStream close fail -------- {}", e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * 读ftp上的文件，并将其转换成base64
     *
     * @param remoteFileName ftp服务器上的文件名
     */
    public String readFileToBase64(String remoteFileName) {
        FTPClient client = connectFtpServer();
        if (client == null) {
            return null;
        }

        String base64 = "";
        InputStream inputStream = null;

        try {
            client.changeWorkingDirectory(remoteDir);
            FTPFile[] ftpFiles = client.listFiles(remoteDir);
            Boolean flag = false;
            //遍历当前目录下的文件，判断要读取的文件是否在当前目录下
            for (FTPFile ftpFile : ftpFiles) {
                if (ftpFile.getName().equals(remoteFileName)) {
                    flag = true;
                }
            }

            if (!flag) {
                LOGGER.error("directory：{}下没有 {}", remoteDir, remoteFileName);
                return null;
            }
            //获取待读文件输入流
            inputStream = client.retrieveFileStream(remoteDir + remoteFileName);

            //inputStream.available() 获取返回在不阻塞的情况下能读取的字节数，正常情况是文件的大小
            byte[] bytes = new byte[inputStream.available()];

            //将文件数据读到字节数组中
            inputStream.read(bytes);
            BASE64Encoder base64Encoder = new BASE64Encoder();
            //将字节数组转成base64字符串
            base64 = base64Encoder.encode(bytes);

            LOGGER.info("read file {} success", remoteFileName);

            // 退出登录
            client.logout();
        } catch (IOException e) {
            LOGGER.error("read file fail ----->>>{}", e.getMessage());
            return null;
        } finally {
            // 关闭ftp客户端
            if (client.isConnected()) {
                try {
                    client.disconnect();
                } catch (IOException e) {
                    LOGGER.error("disconnect fail ------->>>{}", e.getMessage());
                }
            }
            // 关闭文件输入流
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error("inputStream close fail -------- {}", e.getMessage());
                }
            }
        }
        return base64;

    }

    /**
     * 文件下载到本地文件夹
     *
     * @param remoteFileName ftp上的文件名
     * @param localFileName  本地文件名
     */
    public void download2Local(String remoteFileName, String localFileName) {
        FTPClient client = connectFtpServer();
        if (client == null) {
            return;
        }

        OutputStream outputStream = null;

        try {
            client.changeWorkingDirectory(remoteDir);
            FTPFile[] ftpFiles = client.listFiles(remoteDir);
            Boolean flag = false;
            //遍历当前目录下的文件，判断是否存在待下载的文件
            for (FTPFile ftpFile : ftpFiles) {
                if (ftpFile.getName().equals(remoteFileName)) {
                    flag = true;
                    break;
                }
            }

            if (!flag) {
                LOGGER.error("directory：{}下没有 {}", remoteDir, remoteFileName);
                return;
            }

            // 创建文件输出流
            outputStream = new FileOutputStream(localDir + localFileName);

            //下载文件
            Boolean isSuccess = client.retrieveFile(remoteFileName, outputStream);
            if (!isSuccess) {
                LOGGER.error("download file 【{}】 fail", remoteFileName);
            }
            LOGGER.info("download file success");

            // 退出登录
            client.logout();
        } catch (IOException e) {
            LOGGER.error("download file 【{}】 fail ------->>>{}", remoteFileName, e.getMessage());
        } finally {
            if (client.isConnected()) {
                try {
                    client.disconnect();
                } catch (IOException e) {
                    LOGGER.error("disconnect fail ------->>>{}", e.getMessage());
                }
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    LOGGER.error("outputStream close fail ------->>>{}", e.getMessage());
                }
            }
        }
    }

    /**
     * 文件下载到客户端浏览器
     *
     * @param response response对象
     * @param realPath 文件路径
     */
    public void download2Browser(final HttpServletResponse response, String realPath) {
        FTPClient client = connectFtpServer();
        if (client == null) {
            return;
        }

        OutputStream outputStream = null;

        try {
            // 获取客户端浏览器文件输出流
            outputStream = response.getOutputStream();

            // 下载文件
            if (!client.retrieveFile(realPath, outputStream)) {
                LOGGER.error("download file path: 【{}】 fail", realPath);
            }
            LOGGER.info("download file success");

            // 退出登录
            client.logout();
        } catch (IOException e) {
            LOGGER.error("download file path 【{}】 fail ------->>>{}", realPath, e.getMessage());
        } finally {
            if (client.isConnected()) {
                try {
                    client.disconnect();
                } catch (IOException e) {
                    LOGGER.error("disconnect fail ------->>>{}", e.getMessage());
                }
            }
        }
    }

    /**
     * 改变工作目录(有则切换目录，没有则创建后切换目录)
     *
     * @param subDirectory 子目录
     * @return boolean 改变成功返回true
     */
    public boolean changeWorkingDir(FTPClient ftpClient, String subDirectory) throws IOException {
        if (!ftpClient.isConnected()) {
            return false;
        }
        String dir;
        try {
            //目录编码，解决中文路径问题
            dir = new String(subDirectory.toString().getBytes("GBK"),"iso-8859-1");
            //尝试切入目录
            if(ftpClient.changeWorkingDirectory(dir)) return true;
            dir = StringExtend.trimStart(dir, "/");
            dir = StringExtend.trimEnd(dir, "/");
            String[] arr =  dir.split("/");
            StringBuffer sbfDir=new StringBuffer();
            //循环生成子目录
            for(String s : arr){
                sbfDir.append("/");
                sbfDir.append(s);
                //目录编码，解决中文路径问题
                dir = new String(sbfDir.toString().getBytes("GBK"),"iso-8859-1");
                //尝试切入目录
                if(ftpClient.changeWorkingDirectory(dir)) continue;
                if(!ftpClient.makeDirectory(dir)){
                    LOGGER.error("[失败]ftp创建目录：{}", sbfDir.toString());
                    return false;
                }
                LOGGER.info("[成功]ftp创建目录：{}", sbfDir.toString());
            }
            //将目录切换至指定路径
            return ftpClient.changeWorkingDirectory(dir);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
