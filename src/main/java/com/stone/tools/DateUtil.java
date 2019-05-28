package com.stone.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author stone
 * @date 2019/5/23 15:24
 * description
 */
public class DateUtil {

    /**
     * 得到指定date的零时刻.
     */
    public static Date getDayBeginTime(Date d) {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd 00:00:00");

            return sdf.parse(sdf.format(d));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 得到指定date的偏移量零时刻.
     */
    public static Date getDayBeginTime(Date d, int offset) {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd 00:00:00");
            Date beginDate = sdf.parse(sdf.format(d));
            Calendar c = Calendar.getInstance();
            c.setTime(beginDate);
            c.add(Calendar.DAY_OF_MONTH, offset);

            return c.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 得到指定date所在周的起始时刻.
     */
    public static Date getWeekBeginTime(Date d) {

        try {
            //得到d的零时刻
            Date beginDate = getDayBeginTime(d);
            Calendar c = Calendar.getInstance();
            c.setTime(beginDate);
            int n = c.get(Calendar.DAY_OF_WEEK);
            c.add(Calendar.DAY_OF_MONTH, -(n - 1));

            return c.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 得到指定date所在周的起始时刻.
     */
    public static Date getWeekBeginTime(Date d, int offset) {

        try {
            //得到d的零时刻
            Date beginDate = getDayBeginTime(d);
            Calendar c = Calendar.getInstance();
            c.setTime(beginDate);
            int n = c.get(Calendar.DAY_OF_WEEK);

            //定位到本周第一天
            c.add(Calendar.DAY_OF_MONTH, -(n - 1));
            c.add(Calendar.DAY_OF_MONTH, offset * 7);

            return c.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 得到指定date所在月的起始时刻.
     */
    public static Date getMonthBeginTime(Date d) {

        try {
            //得到d的零时刻
            Date beginDate = getDayBeginTime(d);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/01 00:00:00");

            return sdf.parse(sdf.format(beginDate));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 得到指定date所在月的起始时刻.
     */
    public static Date getMonthBeginTime(Date d, int offset) {

        try {
            //得到d的零时刻
            Date beginDate = getDayBeginTime(d);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/01 00:00:00");

            //d所在月的第一天的零时刻
            Date firstDay = sdf.parse(sdf.format(beginDate));

            Calendar c = Calendar.getInstance();
            c.setTime(firstDay);

            //对月进行滚动
            c.add(Calendar.MONTH, offset);

            return c.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     *  将日期时间字符串2019-05-27 10:40:49转换为20190527
     * @param strDate 输入日期时间字符串，格式为：2019-05-27 10:40:49
     * @return 输出日期字符串，格式为20190527
     * @throws ParseException
     */
    public static String getDate(String strDate) throws ParseException {
        SimpleDateFormat sourceFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sourceFormatter.parse(strDate);

        SimpleDateFormat targetFormatter = new SimpleDateFormat("yyyyMMdd");
        return targetFormatter.format(date).toString();
    }

    public static void main(String[] args) {
       String strDate = "2019-05-27 10:40:49";
        try {
            System.out.println(DateUtil.getDate(strDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
