package com.android.internal.telephony;

import android.telephony.Rlog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.nano.TelephonyProto;
import java.util.Calendar;
import java.util.TimeZone;

@VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
public final class NitzData {
    private static final String LOG_TAG = "NitzData";
    private static final int MAX_NITZ_YEAR = 2037;
    private static final int MS_PER_HOUR = 3600000;
    private static final int MS_PER_QUARTER_HOUR = 900000;
    private final long mCurrentTimeMillis;
    private final Integer mDstOffset;
    private final TimeZone mEmulatorHostTimeZone;
    private final String mOriginalString;
    private final int mZoneOffset;

    private NitzData(String originalString, int zoneOffsetMillis, Integer dstOffsetMillis, long utcTimeMillis, TimeZone emulatorHostTimeZone) {
        if (originalString != null) {
            this.mOriginalString = originalString;
            this.mZoneOffset = zoneOffsetMillis;
            this.mDstOffset = dstOffsetMillis;
            this.mCurrentTimeMillis = utcTimeMillis;
            this.mEmulatorHostTimeZone = emulatorHostTimeZone;
            return;
        }
        throw new NullPointerException("originalString==null");
    }

    public static NitzData parse(String nitz) {
        Integer dstAdjustmentMillis;
        TimeZone zone;
        try {
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            c.clear();
            boolean sign = false;
            c.set(16, 0);
            String[] nitzSubs = nitz.split("[/:,+-]");
            int year = Integer.parseInt(nitzSubs[0]) + TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_MIP_FA_REASON_UNSPECIFIED;
            if (year > 2037) {
                Rlog.e(LOG_TAG, "NITZ year: " + year + " exceeds limit, skip NITZ time update");
                return null;
            }
            int i = 1;
            c.set(1, year);
            c.set(2, Integer.parseInt(nitzSubs[1]) - 1);
            c.set(5, Integer.parseInt(nitzSubs[2]));
            c.set(10, Integer.parseInt(nitzSubs[3]));
            c.set(12, Integer.parseInt(nitzSubs[4]));
            c.set(13, Integer.parseInt(nitzSubs[5]));
            if (nitz.indexOf(45) == -1) {
                sign = true;
            }
            int totalUtcOffsetQuarterHours = Integer.parseInt(nitzSubs[6]);
            if (!sign) {
                i = -1;
            }
            int totalUtcOffsetMillis = i * totalUtcOffsetQuarterHours * MS_PER_QUARTER_HOUR;
            Integer dstAdjustmentHours = nitzSubs.length >= 8 ? Integer.valueOf(Integer.parseInt(nitzSubs[7])) : null;
            if (dstAdjustmentHours != null) {
                dstAdjustmentMillis = Integer.valueOf(dstAdjustmentHours.intValue() * MS_PER_HOUR);
            } else {
                dstAdjustmentMillis = null;
            }
            if (nitzSubs.length >= 9) {
                zone = TimeZone.getTimeZone(nitzSubs[8].replace('!', '/'));
            } else {
                zone = null;
            }
            return new NitzData(nitz, totalUtcOffsetMillis, dstAdjustmentMillis, c.getTimeInMillis(), zone);
        } catch (RuntimeException ex) {
            Rlog.e(LOG_TAG, "NITZ: Parsing NITZ time " + nitz + " ex=" + ex);
            return null;
        }
    }

    public static NitzData createForTests(int zoneOffsetMillis, Integer dstOffsetMillis, long utcTimeMillis, TimeZone emulatorHostTimeZone) {
        return new NitzData("Test data", zoneOffsetMillis, dstOffsetMillis, utcTimeMillis, emulatorHostTimeZone);
    }

    public long getCurrentTimeInMillis() {
        return this.mCurrentTimeMillis;
    }

    public int getLocalOffsetMillis() {
        return this.mZoneOffset;
    }

    public Integer getDstAdjustmentMillis() {
        return this.mDstOffset;
    }

    public boolean isDst() {
        Integer num = this.mDstOffset;
        return (num == null || num.intValue() == 0) ? false : true;
    }

    public TimeZone getEmulatorHostTimeZone() {
        return this.mEmulatorHostTimeZone;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NitzData nitzData = (NitzData) o;
        if (this.mZoneOffset != nitzData.mZoneOffset || this.mCurrentTimeMillis != nitzData.mCurrentTimeMillis || !this.mOriginalString.equals(nitzData.mOriginalString)) {
            return false;
        }
        Integer num = this.mDstOffset;
        if (num == null ? nitzData.mDstOffset != null : !num.equals(nitzData.mDstOffset)) {
            return false;
        }
        TimeZone timeZone = this.mEmulatorHostTimeZone;
        if (timeZone != null) {
            return timeZone.equals(nitzData.mEmulatorHostTimeZone);
        }
        if (nitzData.mEmulatorHostTimeZone == null) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = ((this.mOriginalString.hashCode() * 31) + this.mZoneOffset) * 31;
        Integer num = this.mDstOffset;
        int i = 0;
        int hashCode = num != null ? num.hashCode() : 0;
        long j = this.mCurrentTimeMillis;
        int result2 = (((result + hashCode) * 31) + ((int) (j ^ (j >>> 32)))) * 31;
        TimeZone timeZone = this.mEmulatorHostTimeZone;
        if (timeZone != null) {
            i = timeZone.hashCode();
        }
        return result2 + i;
    }

    public String toString() {
        return "NitzData{mOriginalString=" + this.mOriginalString + ", mZoneOffset=" + this.mZoneOffset + ", mDstOffset=" + this.mDstOffset + ", mCurrentTimeMillis=" + this.mCurrentTimeMillis + ", mEmulatorHostTimeZone=" + this.mEmulatorHostTimeZone + '}';
    }
}
