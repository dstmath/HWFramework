package android.webkit;

import android.content.Context;
import android.content.res.Resources;
import java.util.Calendar;
import java.util.Locale;
import libcore.icu.LocaleData;

public class DateSorter {
    public static final int DAY_COUNT = 5;
    private static final String LOGTAG = "webkit";
    private static final int NUM_DAYS_AGO = 7;
    private long[] mBins = new long[4];
    private String[] mLabels = new String[5];

    public DateSorter(Context context) {
        Resources resources = context.getResources();
        Calendar c = Calendar.getInstance();
        beginningOfDay(c);
        this.mBins[0] = c.getTimeInMillis();
        c.add(6, -1);
        this.mBins[1] = c.getTimeInMillis();
        c.add(6, -6);
        this.mBins[2] = c.getTimeInMillis();
        c.add(6, 7);
        c.add(2, -1);
        this.mBins[3] = c.getTimeInMillis();
        LocaleData localeData = LocaleData.get(resources.getConfiguration().locale == null ? Locale.getDefault() : resources.getConfiguration().locale);
        this.mLabels[0] = localeData.today;
        this.mLabels[1] = localeData.yesterday;
        this.mLabels[2] = String.format(resources.getQuantityString(18153494, 7), new Object[]{7});
        this.mLabels[3] = context.getString(17040319);
        this.mLabels[4] = context.getString(17040618);
    }

    public int getIndex(long time) {
        for (int i = 0; i < 4; i++) {
            if (time > this.mBins[i]) {
                return i;
            }
        }
        return 4;
    }

    public String getLabel(int index) {
        if (index < 0 || index >= 5) {
            return "";
        }
        return this.mLabels[index];
    }

    public long getBoundary(int index) {
        if (index < 0 || index > 4) {
            index = 0;
        }
        if (index == 4) {
            return Long.MIN_VALUE;
        }
        return this.mBins[index];
    }

    private void beginningOfDay(Calendar c) {
        c.set(11, 0);
        c.set(12, 0);
        c.set(13, 0);
        c.set(14, 0);
    }
}
