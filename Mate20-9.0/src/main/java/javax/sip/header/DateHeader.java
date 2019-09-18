package javax.sip.header;

import java.util.Calendar;

public interface DateHeader extends Header {
    public static final String NAME = "Date";

    Calendar getDate();

    void setDate(Calendar calendar);
}
