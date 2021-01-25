package com.huawei.odmf.utils;

import com.huawei.odmf.exception.ODMFRuntimeException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Calendar;

public class BindUtils {
    private BindUtils() {
    }

    public static byte[] bindBlob(Blob blob) {
        try {
            long length = blob.length();
            int i = (int) length;
            if (((long) i) == length) {
                return blob.getBytes(1, i);
            }
            throw new IllegalArgumentException("The Blob is too long");
        } catch (SQLException unused) {
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
            int i = (int) length;
            if (((long) i) == length) {
                return clob.getSubString(1, i);
            }
            throw new IllegalArgumentException("The Clob is too long");
        } catch (SQLException unused) {
            LOG.logE("execute clobToString error : error happens when get string from clob.");
            throw new ODMFRuntimeException("error happens when get string from clob.");
        }
    }

    public static Calendar getCalendar(long j) {
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(j);
        return instance;
    }
}
