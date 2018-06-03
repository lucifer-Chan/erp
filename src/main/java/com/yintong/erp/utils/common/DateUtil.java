package com.yintong.erp.utils.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author lucifer.chan
 * @create 2018-05-14 上午1:12
 * 日期辅助类
 **/
public class DateUtil {

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static Date parseDateTime(String date) throws ParseException {
        return timeFormat.parse(date);
    }

    public static Date parseDate(String date) throws ParseException {
        return dateFormat.parse(date);
    }

    public static String getDateString(Date date) {
        return dateFormat.format(date);
    }

    public static String getDateTimeString(Date date) {
        return timeFormat.format(date);
    }
}
