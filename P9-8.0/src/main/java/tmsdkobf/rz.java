package tmsdkobf;

import android.media.ExifInterface;
import android.os.Environment;
import android.text.TextUtils;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import tmsdk.common.utils.f;

public class rz {
    private static SimpleDateFormat Qe;
    public static final TimeZone Qf = TimeZone.getDefault();
    public static final String Qg = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String[] Qh = new String[]{"screenshot", "截屏"};
    private static final String[] Qi = new String[]{"xj", "androidgeek", "logo", "pt", "MYXJ", "C360"};
    private static String TAG = "MediaFileUtil";

    public static class a {
        public static final String[] Qj = new String[]{"mp4", "avi", "3gpp", "mkv", "wmv", "3gpp2", "mp2ts", "3gp", "mov", "flv", "rmvb", "flv"};
        public static final String[] Qk = new String[]{"jpg", "jpeg", "png", "gif", "bmp"};
        public static final String[] Ql = new String[]{"mp3", "wma", "flac", "wav", "mid", "m4a", "aac"};
        public static final String[] Qm = new String[]{"jpg", "jpeg"};
    }

    private static long a(ExifInterface exifInterface) {
        String attribute = exifInterface.getAttribute("DateTime");
        if (attribute == null) {
            return 0;
        }
        f.e(TAG, "exif time:" + attribute);
        ParsePosition parsePosition = new ParsePosition(0);
        try {
            if (Qe == null) {
                Qe = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                Qe.setTimeZone(TimeZone.getTimeZone("UTC"));
            }
            Date parse = Qe.parse(attribute, parsePosition);
            if (parse == null) {
                return 0;
            }
            long time = parse.getTime();
            return time - ((long) Qf.getOffset(time));
        } catch (Throwable e) {
            f.b(TAG, "exifDateTime", e);
            return 0;
        } catch (Exception e2) {
            return 0;
        }
    }

    public static boolean dA(String str) {
        String toLowerCase = str.toLowerCase();
        for (CharSequence contains : Qh) {
            if (toLowerCase.contains(contains)) {
                return true;
            }
        }
        return false;
    }

    public static boolean dB(String str) {
        String toLowerCase = dC(str).toLowerCase();
        for (String equals : a.Qk) {
            if (equals.equals(toLowerCase)) {
                return true;
            }
        }
        return false;
    }

    public static String dC(String str) {
        if (str == null) {
            return null;
        }
        String str2 = null;
        int lastIndexOf = str.lastIndexOf(".");
        if (lastIndexOf >= 0 && lastIndexOf < str.length() - 1) {
            str2 = str.substring(lastIndexOf + 1);
        }
        return str2;
    }

    public static boolean dD(String str) {
        if (str == null) {
            return false;
        }
        String toLowerCase = dC(str).toLowerCase();
        for (String equals : a.Qm) {
            if (equals.equals(toLowerCase)) {
                return true;
            }
        }
        return false;
    }

    public static boolean dE(String str) {
        return !TextUtils.isEmpty(str) ? str.startsWith(Qg) : false;
    }

    public static boolean dF(String str) {
        if (str != null) {
            for (String startsWith : Qi) {
                if (str.startsWith(startsWith)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static long dG(String str) {
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(str);
        } catch (Throwable th) {
            f.b(TAG, "getImageTakenTime", th);
        }
        return exifInterface == null ? 0 : a(exifInterface);
    }

    public static String di(String str) {
        if (str == null) {
            return str;
        }
        int lastIndexOf = str.lastIndexOf("/");
        return (lastIndexOf >= 0 && lastIndexOf < str.length() - 1) ? str.substring(lastIndexOf + 1, str.length()) : str;
    }
}
