package org.apache.http.protocol;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@Deprecated
public class HttpDateGenerator {
    public static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    public static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private long dateAsLong = 0;
    private String dateAsText = null;
    private final DateFormat dateformat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

    public HttpDateGenerator() {
        this.dateformat.setTimeZone(GMT);
    }

    public synchronized String getCurrentDate() {
        long now = System.currentTimeMillis();
        if (now - this.dateAsLong > 1000) {
            this.dateAsText = this.dateformat.format(new Date(now));
            this.dateAsLong = now;
        }
        return this.dateAsText;
    }
}
