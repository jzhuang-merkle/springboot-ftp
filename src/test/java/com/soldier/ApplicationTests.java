package com.soldier;

import com.soldier.util.FTPUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationTests {

    @Autowired
    private FTPUtils ftpUtils;

    @Test
    void contextLoads() throws Exception {
//        ftpUtils.uploadFile(new FileInputStream("/home/soldier/Documents/开发文档/renren-fast开发文档3.0_完整版.pdf"), System.currentTimeMillis()+"");
        ftpUtils.uploadDir("2020/08/03", "/home/soldier/Documents/开发文档/");
    }

}
