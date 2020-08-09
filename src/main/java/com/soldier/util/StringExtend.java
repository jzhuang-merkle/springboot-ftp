package com.soldier.util;

/**
 * @Author soldier
 * @Date 20-8-9 上午11:58
 * @Email:583406411@qq.com
 * @Version 1.0
 * @Description:
 */

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

public class StringExtend {

    /**
     * 获取换行符，区分不同系统
     * /r Mac；/n Unix/Linux；/r/n Windows
     */
    public static String getEnterMark() {
        return System.getProperty("line.separator");
    }

    /**
     * 清除首位空格
     */
    public static String trim(String msg) {
        if (msg == null)
            return null;
        return msg.trim();
    }

    /**
     * 字符串内容格式化输出，内部使用{0}\{1}\{2}...为参数占位符</br>
     * 参数格式：ArgumentIndex[,FormatType[,FormatStyle]] </br>
     * FormatType 取值:number,date，time，choice </br>
     * FormatType 样式：如：#.## </br>
     * 注：'{' 可输出左花括号(单写左花括号会报错，而单写右花括号将正常输出)</br>
     *
     * @param msg  格式化模板
     * @param args 不固定参数
     * @return
     */
    public static String format(String msg, Object... args) {
        return java.text.MessageFormat.format(msg, args);
    }

    /**
     * 转换字符串到
     *
     * @param num   指定位置
     */
    public static Integer getInt(String num) {
        if (num == null || num.trim().isEmpty())
            return 0;
        if (!num.matches("^(\\d|-)\\d{0,9}$"))
            return 0;
        try {
            return Integer.parseInt(num);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getString(Object obj) {
        return obj == null ? null : obj.toString();
    }

    public static String getString(Integer num) {
        return getString(num, "");
    }

    public static String getString(Integer num, String def) {
        if (num == null)
            return def;
        return num.toString();
    }

    /**
     * 字符串是否为空
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 比较两个字符串是否相等，忽略大小写
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        String tmp = str1 == null ? "" : str1;
        return tmp.equalsIgnoreCase(str2);
    }

    /**
     * md5 加密
     */
    public static String getMd5(String... str) {
        if (str == null || str.length == 0)
            return "";
        StringBuffer sbr = new StringBuffer();
        for (String item : str) {
            sbr.append(item);
        }
        // 生成一个MD5加密计算摘要
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(sbr.toString().getBytes());
            // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            return new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 删除起始字符
     *
     * @param str  被操作字符串
     * @param trim 正则
     */
    public static String trimStart(String str, String trim) {
        if (str == null)
            return null;
        return str.replaceAll("^(" + trim + ")+", "");
    }

    /**
     * 删除末尾字符
     *
     * @param str  被操作字符串
     * @param trim 正则
     */
    public static String trimEnd(String str, String trim) {
        if (str == null)
            return null;
        return str.replaceAll("(" + trim + ")+$", "");
    }

    /**
     * 判断字符串是否以字符开头
     */
    public static boolean startWith(String str, String s) {
        return str.startsWith(s);
    }

    /**
     * 判断字符串是否以字符结尾
     */
    public static boolean endWith(String str, String s) {
        return str.endsWith(s);
    }

    /**
     * 获取 boolean 值（1=true；True=true；）
     */
    public static boolean getBoolean(String str) {
        if (isNullOrEmpty(str))
            return false;

        Pattern pattern = Pattern.compile("(1)|(true)", Pattern.CASE_INSENSITIVE);
        if (pattern.matcher(str).matches())
            return true;

        return false;
    }

    /**
     * 隐藏银行账号后6位
     */
    public static String bankAccount(String str) {
        if (isNullOrEmpty(str)) {
            return null;
        }

        if (str.length() > 6) {
            return str.substring(0, str.length() - 6) + "xxxxxx";
        }
        return str;
    }
}
