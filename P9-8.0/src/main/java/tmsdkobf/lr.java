package tmsdkobf;

import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import java.util.Date;
import java.util.GregorianCalendar;

public class lr {
    public static GregorianCalendar a(GregorianCalendar gregorianCalendar) {
        gregorianCalendar.clear(11);
        gregorianCalendar.clear(10);
        gregorianCalendar.clear(12);
        gregorianCalendar.clear(13);
        gregorianCalendar.clear(14);
        return gregorianCalendar;
    }

    public static GregorianCalendar a(GregorianCalendar gregorianCalendar, int i) {
        GregorianCalendar gregorianCalendar2 = new GregorianCalendar();
        gregorianCalendar2.setTimeInMillis(gregorianCalendar.getTimeInMillis());
        if (gregorianCalendar2.get(5) != i) {
            if (gregorianCalendar2.get(5) <= i) {
                int actualMaximum = gregorianCalendar2.getActualMaximum(5);
                if (actualMaximum >= i || gregorianCalendar2.get(5) != actualMaximum) {
                    gregorianCalendar2.add(2, -1);
                }
                gregorianCalendar2 = b(gregorianCalendar2, i);
            } else {
                gregorianCalendar2.set(5, i);
            }
        }
        return a(gregorianCalendar2);
    }

    public static boolean a(long j, long j2, int i) {
        return !(((j - j2) > ((long) ((i * 60) * CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY)) ? 1 : ((j - j2) == ((long) ((i * 60) * CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY)) ? 0 : -1)) <= 0);
    }

    public static boolean a(Date date, Date date2) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        GregorianCalendar gregorianCalendar2 = new GregorianCalendar();
        gregorianCalendar.setTime(date);
        gregorianCalendar = a(gregorianCalendar);
        gregorianCalendar2.setTime(date2);
        return gregorianCalendar.getTimeInMillis() == a(gregorianCalendar2).getTimeInMillis();
    }

    private static GregorianCalendar b(GregorianCalendar gregorianCalendar, int i) {
        int actualMaximum = gregorianCalendar.getActualMaximum(5);
        if (actualMaximum < i) {
            gregorianCalendar.set(5, actualMaximum);
        } else {
            gregorianCalendar.set(5, i);
        }
        return gregorianCalendar;
    }
}
