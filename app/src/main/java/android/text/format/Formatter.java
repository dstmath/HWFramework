package android.text.format;

import android.content.Context;
import android.content.res.Resources;
import android.net.NetworkUtils;
import android.text.BidiFormatter;
import android.text.TextUtils;
import android.view.WindowManager.LayoutParams;
import com.android.internal.R;

public final class Formatter {
    public static final int FLAG_CALCULATE_ROUNDED = 2;
    public static final int FLAG_SHORTER = 1;
    private static final int MILLIS_PER_MINUTE = 60000;
    private static final int SECONDS_PER_DAY = 86400;
    private static final int SECONDS_PER_HOUR = 3600;
    private static final int SECONDS_PER_MINUTE = 60;

    public static class BytesResult {
        public final long roundedBytes;
        public final String units;
        public final String value;

        public BytesResult(String value, String units, long roundedBytes) {
            this.value = value;
            this.units = units;
            this.roundedBytes = roundedBytes;
        }
    }

    private static String bidiWrap(Context context, String source) {
        if (TextUtils.getLayoutDirectionFromLocale(context.getResources().getConfiguration().locale) == FLAG_SHORTER) {
            return BidiFormatter.getInstance(true).unicodeWrap(source);
        }
        return source;
    }

    public static String formatFileSize(Context context, long sizeBytes) {
        if (context == null) {
            return "";
        }
        BytesResult res = formatBytes(context.getResources(), sizeBytes, 0);
        Object[] objArr = new Object[FLAG_CALCULATE_ROUNDED];
        objArr[0] = res.value;
        objArr[FLAG_SHORTER] = res.units;
        return bidiWrap(context, context.getString(R.string.fileSizeSuffix, objArr));
    }

    public static String formatShortFileSize(Context context, long sizeBytes) {
        if (context == null) {
            return "";
        }
        BytesResult res = formatBytes(context.getResources(), sizeBytes, FLAG_SHORTER);
        Object[] objArr = new Object[FLAG_CALCULATE_ROUNDED];
        objArr[0] = res.value;
        objArr[FLAG_SHORTER] = res.units;
        return bidiWrap(context, context.getString(R.string.fileSizeSuffix, objArr));
    }

    public static BytesResult formatBytes(Resources res, long sizeBytes, int flags) {
        int roundFactor;
        String roundFormat;
        long roundedBytes;
        boolean isNegative = sizeBytes < 0;
        if (isNegative) {
            sizeBytes = -sizeBytes;
        }
        float result = (float) sizeBytes;
        int suffix = R.string.byteShort;
        long mult = 1;
        if (result > 900.0f) {
            suffix = R.string.kilobyteShort;
            mult = 1024;
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = R.string.megabyteShort;
            mult = 1048576;
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = R.string.gigabyteShort;
            mult = 1073741824;
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = R.string.terabyteShort;
            mult = 1099511627776L;
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = R.string.petabyteShort;
            mult = 1125899906842624L;
            result /= 1024.0f;
        }
        if (mult == 1 || result >= 100.0f) {
            roundFactor = FLAG_SHORTER;
            roundFormat = "%.0f";
        } else if (result < LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
            roundFactor = 100;
            roundFormat = "%.2f";
        } else if (result < 10.0f) {
            if ((flags & FLAG_SHORTER) != 0) {
                roundFactor = 10;
                roundFormat = "%.1f";
            } else {
                roundFactor = 100;
                roundFormat = "%.2f";
            }
        } else if ((flags & FLAG_SHORTER) != 0) {
            roundFactor = FLAG_SHORTER;
            roundFormat = "%.0f";
        } else {
            roundFactor = 100;
            roundFormat = "%.2f";
        }
        if (isNegative) {
            result = -result;
        }
        Object[] objArr = new Object[FLAG_SHORTER];
        objArr[0] = Float.valueOf(result);
        String roundedString = String.format(roundFormat, objArr);
        if ((flags & FLAG_CALCULATE_ROUNDED) == 0) {
            roundedBytes = 0;
        } else {
            roundedBytes = (((long) Math.round(((float) roundFactor) * result)) * mult) / ((long) roundFactor);
        }
        return new BytesResult(roundedString, res.getString(suffix), roundedBytes);
    }

    @Deprecated
    public static String formatIpAddress(int ipv4Address) {
        return NetworkUtils.intToInetAddress(ipv4Address).getHostAddress();
    }

    public static String formatShortElapsedTime(Context context, long millis) {
        long secondsLong = millis / 1000;
        int days = 0;
        int hours = 0;
        int minutes = 0;
        if (secondsLong >= 86400) {
            days = (int) (secondsLong / 86400);
            secondsLong -= (long) (SECONDS_PER_DAY * days);
        }
        if (secondsLong >= 3600) {
            hours = (int) (secondsLong / 3600);
            secondsLong -= (long) (hours * SECONDS_PER_HOUR);
        }
        if (secondsLong >= 60) {
            minutes = (int) (secondsLong / 60);
            secondsLong -= (long) (minutes * SECONDS_PER_MINUTE);
        }
        int seconds = (int) secondsLong;
        Object[] objArr;
        if (days >= FLAG_CALCULATE_ROUNDED) {
            objArr = new Object[FLAG_SHORTER];
            objArr[0] = Integer.valueOf(days + ((hours + 12) / 24));
            return context.getString(R.string.durationDays, objArr);
        } else if (days > 0) {
            if (hours == FLAG_SHORTER) {
                objArr = new Object[FLAG_CALCULATE_ROUNDED];
                objArr[0] = Integer.valueOf(days);
                objArr[FLAG_SHORTER] = Integer.valueOf(hours);
                return context.getString(R.string.durationDayHour, objArr);
            }
            objArr = new Object[FLAG_CALCULATE_ROUNDED];
            objArr[0] = Integer.valueOf(days);
            objArr[FLAG_SHORTER] = Integer.valueOf(hours);
            return context.getString(R.string.durationDayHours, objArr);
        } else if (hours >= FLAG_CALCULATE_ROUNDED) {
            objArr = new Object[FLAG_SHORTER];
            objArr[0] = Integer.valueOf(hours + ((minutes + 30) / SECONDS_PER_MINUTE));
            return context.getString(R.string.durationHours, objArr);
        } else if (hours > 0) {
            if (minutes == FLAG_SHORTER) {
                objArr = new Object[FLAG_CALCULATE_ROUNDED];
                objArr[0] = Integer.valueOf(hours);
                objArr[FLAG_SHORTER] = Integer.valueOf(minutes);
                return context.getString(R.string.durationHourMinute, objArr);
            }
            objArr = new Object[FLAG_CALCULATE_ROUNDED];
            objArr[0] = Integer.valueOf(hours);
            objArr[FLAG_SHORTER] = Integer.valueOf(minutes);
            return context.getString(R.string.durationHourMinutes, objArr);
        } else if (minutes >= FLAG_CALCULATE_ROUNDED) {
            objArr = new Object[FLAG_SHORTER];
            objArr[0] = Integer.valueOf(minutes + ((seconds + 30) / SECONDS_PER_MINUTE));
            return context.getString(R.string.durationMinutes, objArr);
        } else if (minutes > 0) {
            if (seconds == FLAG_SHORTER) {
                objArr = new Object[FLAG_CALCULATE_ROUNDED];
                objArr[0] = Integer.valueOf(minutes);
                objArr[FLAG_SHORTER] = Integer.valueOf(seconds);
                return context.getString(R.string.durationMinuteSecond, objArr);
            }
            objArr = new Object[FLAG_CALCULATE_ROUNDED];
            objArr[0] = Integer.valueOf(minutes);
            objArr[FLAG_SHORTER] = Integer.valueOf(seconds);
            return context.getString(R.string.durationMinuteSeconds, objArr);
        } else if (seconds == FLAG_SHORTER) {
            objArr = new Object[FLAG_SHORTER];
            objArr[0] = Integer.valueOf(seconds);
            return context.getString(R.string.durationSecond, objArr);
        } else {
            objArr = new Object[FLAG_SHORTER];
            objArr[0] = Integer.valueOf(seconds);
            return context.getString(R.string.durationSeconds, objArr);
        }
    }

    public static String formatShortElapsedTimeRoundingUpToMinutes(Context context, long millis) {
        long minutesRoundedUp = ((millis + DateUtils.MINUTE_IN_MILLIS) - 1) / DateUtils.MINUTE_IN_MILLIS;
        Object[] objArr;
        if (minutesRoundedUp == 0) {
            objArr = new Object[FLAG_SHORTER];
            objArr[0] = Integer.valueOf(0);
            return context.getString(R.string.durationMinutes, objArr);
        } else if (minutesRoundedUp != 1) {
            return formatShortElapsedTime(context, minutesRoundedUp * DateUtils.MINUTE_IN_MILLIS);
        } else {
            objArr = new Object[FLAG_SHORTER];
            objArr[0] = Integer.valueOf(FLAG_SHORTER);
            return context.getString(R.string.durationMinute, objArr);
        }
    }
}
