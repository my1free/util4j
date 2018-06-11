package com.michealyang.util4j.excel.util;

import com.google.common.base.Strings;
import org.apache.commons.lang3.time.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author michealyang
 * @version 1.0
 * @created 18/6/11
 * 开始眼保健操： →_→  ↑_↑  ←_←  ↓_↓
 */
public class DateTimeUtils {

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";


    public static String convertDateToString(Date date, String format) throws ParseException {
        if (date == null) {
            return "";
        }

        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    public static Date convertDate(String date, String format) throws ParseException {
        if (Strings.isNullOrEmpty(date)) {
            return null;
        }
        if (format == null) {
            format = DATE_TIME_FORMAT;
        }

        return DateUtils.parseDate(date, format);
    }
}