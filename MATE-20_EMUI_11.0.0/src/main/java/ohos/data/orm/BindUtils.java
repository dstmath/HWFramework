package ohos.data.orm;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Calendar;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class BindUtils {
    static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "BindUtils");

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
            HiLog.error(LABEL, "execute bindBlob error : error happens when get bytes from blob.", new Object[0]);
            return null;
        }
    }

    public static String clobToString(Clob clob) {
        if (clob instanceof Clob) {
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
            HiLog.error(LABEL, "execute clobToString error : error happens when get string from clob.", new Object[0]);
            return null;
        }
    }

    public static Calendar getCalendar(long j) {
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(j);
        return instance;
    }
}
