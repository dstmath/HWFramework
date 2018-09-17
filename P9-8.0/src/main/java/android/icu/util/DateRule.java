package android.icu.util;

import java.util.Date;

public interface DateRule {
    Date firstAfter(Date date);

    Date firstBetween(Date date, Date date2);

    boolean isBetween(Date date, Date date2);

    boolean isOn(Date date);
}
