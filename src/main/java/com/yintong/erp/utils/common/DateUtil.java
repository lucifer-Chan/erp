package com.yintong.erp.utils.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.util.Assert;

/**
 * @author lucifer.chan
 * @create 2018-05-14 上午1:12
 * 日期辅助类
 **/
public class DateUtil {

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static Date parseDateTime(String date) {
        try {
            return timeFormat.parse(date);
        } catch (ParseException e){
            throw new IllegalArgumentException("[" + date + "]格式不正确");
        }
    }

    public static Date parseDate(String date) {
        try {
            return dateFormat.parse(date);
        } catch (ParseException e){
            throw new IllegalArgumentException("[" + date + "]格式不正确");
        }
    }

    public static String getDateString(Date date) {
        return dateFormat.format(date);
    }

    public static String getDateTimeString(Date date) {
        return timeFormat.format(date);
    }

    /**
     * 判断start-end 和 anotherStart-anotherEnd是否有交集
     * @param start
     * @param end
     * @param anotherStart
     * @param anotherEnd
     * @return
     */
    public static boolean isNotCross(Date start, Date end, Date anotherStart, Date anotherEnd) {
        Assert.notNull(start, "start must be not null");
        Assert.notNull(end, "end must be not null");
        Assert.notNull(anotherStart, "anotherStart must be not null");
        Assert.notNull(anotherEnd, "anotherEnd must be not null");
        Assert.isTrue(start.before(end) && anotherStart.before(anotherEnd), "开始时间必须小于结束时间");

        if(start.before(anotherStart)) {
            return anotherStart.after(end);
        }

        return !start.after(anotherStart) || anotherEnd.before(start);
    }
}
