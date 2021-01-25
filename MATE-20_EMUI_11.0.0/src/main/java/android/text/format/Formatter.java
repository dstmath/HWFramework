package android.text.format;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.Resources;
import android.icu.text.MeasureFormat;
import android.icu.util.Measure;
import android.icu.util.MeasureUnit;
import android.net.NetworkUtils;
import android.text.BidiFormatter;
import android.text.TextUtils;
import com.android.internal.R;
import java.util.Locale;

public final class Formatter {
    public static final int FLAG_CALCULATE_ROUNDED = 2;
    public static final int FLAG_IEC_UNITS = 8;
    public static final int FLAG_SHORTER = 1;
    public static final int FLAG_SI_UNITS = 4;
    private static final int MILLIS_PER_MINUTE = 60000;
    private static final int SECONDS_PER_DAY = 86400;
    private static final int SECONDS_PER_HOUR = 3600;
    private static final int SECONDS_PER_MINUTE = 60;

    public static class BytesResult {
        public final long roundedBytes;
        public final String units;
        public final String value;

        public BytesResult(String value2, String units2, long roundedBytes2) {
            this.value = value2;
            this.units = units2;
            this.roundedBytes = roundedBytes2;
        }
    }

    private static Locale localeFromContext(Context context) {
        return context.getResources().getConfiguration().getLocales().get(0);
    }

    private static String bidiWrap(Context context, String source) {
        if (TextUtils.getLayoutDirectionFromLocale(localeFromContext(context)) == 1) {
            return BidiFormatter.getInstance(true).unicodeWrap(source);
        }
        return source;
    }

    public static String formatFileSize(Context context, long sizeBytes) {
        return formatFileSize(context, sizeBytes, 4);
    }

    public static String formatFileSize(Context context, long sizeBytes, int flags) {
        if (context == null) {
            return "";
        }
        BytesResult res = formatBytes(context.getResources(), sizeBytes, flags);
        return bidiWrap(context, context.getString(R.string.fileSizeSuffix, res.value, res.units));
    }

    public static String formatShortFileSize(Context context, long sizeBytes) {
        if (context == null) {
            return "";
        }
        BytesResult res = formatBytes(context.getResources(), sizeBytes, 5);
        return bidiWrap(context, context.getString(R.string.fileSizeSuffix, res.value, res.units));
    }

    @UnsupportedAppUsage
    public static BytesResult formatBytes(Resources res, long sizeBytes, int flags) {
        String roundFormat;
        int roundFactor;
        int unit = (flags & 8) != 0 ? 1024 : 1000;
        long roundedBytes = 0;
        boolean isNegative = sizeBytes < 0;
        float result = isNegative ? (float) (-sizeBytes) : (float) sizeBytes;
        int suffix = R.string.byteShort;
        long mult = 1;
        if (result > 900.0f) {
            suffix = R.string.kilobyteShort;
            mult = (long) unit;
            result /= (float) unit;
        }
        if (result > 900.0f) {
            suffix = R.string.megabyteShort;
            mult *= (long) unit;
            result /= (float) unit;
        }
        if (result > 900.0f) {
            suffix = R.string.gigabyteShort;
            mult *= (long) unit;
            result /= (float) unit;
        }
        if (result > 900.0f) {
            suffix = R.string.terabyteShort;
            mult *= (long) unit;
            result /= (float) unit;
        }
        if (result > 900.0f) {
            suffix = R.string.petabyteShort;
            mult *= (long) unit;
            result /= (float) unit;
        }
        if (mult == 1 || result >= 100.0f) {
            roundFactor = 1;
            roundFormat = "%.0f";
        } else if (result < 1.0f) {
            roundFactor = 100;
            roundFormat = "%.2f";
        } else if (result < 10.0f) {
            if ((flags & 1) != 0) {
                roundFactor = 10;
                roundFormat = "%.1f";
            } else {
                roundFactor = 100;
                roundFormat = "%.2f";
            }
        } else if ((flags & 1) != 0) {
            roundFactor = 1;
            roundFormat = "%.0f";
        } else {
            roundFactor = 100;
            roundFormat = "%.2f";
        }
        if (isNegative) {
            result = -result;
        }
        String roundedString = String.format(roundFormat, Float.valueOf(result));
        if ((flags & 2) != 0) {
            roundedBytes = (((long) Math.round(((float) roundFactor) * result)) * mult) / ((long) roundFactor);
        }
        return new BytesResult(roundedString, res.getString(suffix), roundedBytes);
    }

    @Deprecated
    public static String formatIpAddress(int ipv4Address) {
        return NetworkUtils.intToInetAddress(ipv4Address).getHostAddress();
    }

    @UnsupportedAppUsage
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
            secondsLong -= (long) (hours * 3600);
        }
        if (secondsLong >= 60) {
            minutes = (int) (secondsLong / 60);
            secondsLong -= (long) (minutes * 60);
        }
        int seconds = (int) secondsLong;
        MeasureFormat measureFormat = MeasureFormat.getInstance(localeFromContext(context), MeasureFormat.FormatWidth.SHORT);
        if (days >= 2 || (days > 0 && hours == 0)) {
            return measureFormat.format(new Measure(Integer.valueOf(days + ((hours + 12) / 24)), MeasureUnit.DAY));
        }
        if (days > 0) {
            return measureFormat.formatMeasures(new Measure(Integer.valueOf(days), MeasureUnit.DAY), new Measure(Integer.valueOf(hours), MeasureUnit.HOUR));
        }
        if (hours >= 2 || (hours > 0 && minutes == 0)) {
            return measureFormat.format(new Measure(Integer.valueOf(hours + ((minutes + 30) / 60)), MeasureUnit.HOUR));
        }
        if (hours > 0) {
            return measureFormat.formatMeasures(new Measure(Integer.valueOf(hours), MeasureUnit.HOUR), new Measure(Integer.valueOf(minutes), MeasureUnit.MINUTE));
        }
        if (minutes >= 2 || (minutes > 0 && seconds == 0)) {
            return measureFormat.format(new Measure(Integer.valueOf(minutes + ((seconds + 30) / 60)), MeasureUnit.MINUTE));
        }
        if (minutes > 0) {
            return measureFormat.formatMeasures(new Measure(Integer.valueOf(minutes), MeasureUnit.MINUTE), new Measure(Integer.valueOf(seconds), MeasureUnit.SECOND));
        }
        return measureFormat.format(new Measure(Integer.valueOf(seconds), MeasureUnit.SECOND));
    }

    @UnsupportedAppUsage
    public static String formatShortElapsedTimeRoundingUpToMinutes(Context context, long millis) {
        long minutesRoundedUp = ((millis + 60000) - 1) / 60000;
        if (minutesRoundedUp == 0 || minutesRoundedUp == 1) {
            return MeasureFormat.getInstance(localeFromContext(context), MeasureFormat.FormatWidth.SHORT).format(new Measure(Long.valueOf(minutesRoundedUp), MeasureUnit.MINUTE));
        }
        return formatShortElapsedTime(context, 60000 * minutesRoundedUp);
    }
}
