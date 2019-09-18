package com.huawei.odmf.utils;

import com.huawei.odmf.exception.ODMFRuntimeException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Calendar;

public class BindUtils {
    public static byte[] bindBlob(Blob blob) {
        try {
            long length = blob.length();
            int intLength = (int) length;
            if (((long) intLength) == length) {
                return blob.getBytes(1, intLength);
            }
            throw new IllegalArgumentException("The Blob is too long");
        } catch (SQLException e) {
            LOG.logE("execute bindBlob error : error happens when get bytes from blob.");
            throw new ODMFRuntimeException("error happens when get bytes from blob.");
        }
    }

    public static String clobToString(Clob clob) {
        if (clob instanceof com.huawei.odmf.data.Clob) {
            return clob.toString();
        }
        try {
            long length = clob.length();
            int intLength = (int) length;
            if (((long) intLength) == length) {
                return clob.getSubString(1, intLength);
            }
            throw new IllegalArgumentException("The Clob is too long");
        } catch (SQLException e) {
            LOG.logE("execute clobToString error : error happens when get string from clob.");
            throw new ODMFRuntimeException("error happens when get string from clob.");
        }
    }

    public static Calendar getCalendar(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar;
    }
}
