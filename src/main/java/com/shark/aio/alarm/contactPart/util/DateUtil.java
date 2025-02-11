package com.shark.aio.alarm.contactPart.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    /**
     * 当前日期
     */
    public static SimpleDateFormat DataFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
    public static String Data = DataFormat.format(new Date());

    /**
     * 输入框格式
     */
    public static SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    /**
     * 时间戳格式
     */
    public static SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * 页面展示的格式
     */
    public static SimpleDateFormat pageFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
    /**
     * 文件名的格式
     */
    public static SimpleDateFormat fileFormat = new SimpleDateFormat("yyyy-MM-dd-_HH-mm-ss");

    /**
     * 输入框格式转化为时间戳格式
     * @param date 输入框格式日期和时间
     * @return 时间戳格式日期和时间
     */
    public static String toTimestamp(String date) throws ParseException {
        Date inputDate = inputDateFormat.parse(date);
        return timestampFormat.format(inputDate);
    }


    public static String timestampToPageFormat(String timestamp) throws ParseException {
        Date timestampDate = timestampFormat.parse(timestamp);
        return pageFormat.format(timestampDate);
    }
}
