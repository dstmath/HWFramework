package android.icu.impl.duration.impl;

import android.icu.impl.locale.BaseLocale;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;

public class Utils {

    public static class ChineseDigits {
        public static final ChineseDigits DEBUG = null;
        public static final ChineseDigits KOREAN = null;
        public static final ChineseDigits SIMPLIFIED = null;
        public static final ChineseDigits TRADITIONAL = null;
        final char[] digits;
        final boolean ko;
        final char[] levels;
        final char liang;
        final char[] units;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.duration.impl.Utils.ChineseDigits.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.duration.impl.Utils.ChineseDigits.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.impl.Utils.ChineseDigits.<clinit>():void");
        }

        ChineseDigits(String digits, String units, String levels, char liang, boolean ko) {
            this.digits = digits.toCharArray();
            this.units = units.toCharArray();
            this.levels = levels.toCharArray();
            this.liang = liang;
            this.ko = ko;
        }
    }

    public Utils() {
    }

    public static final Locale localeFromString(String s) {
        String language = s;
        String region = XmlPullParser.NO_NAMESPACE;
        String variant = XmlPullParser.NO_NAMESPACE;
        int x = s.indexOf(BaseLocale.SEP);
        if (x != -1) {
            region = s.substring(x + 1);
            language = s.substring(0, x);
        }
        x = region.indexOf(BaseLocale.SEP);
        if (x != -1) {
            variant = region.substring(x + 1);
            region = region.substring(0, x);
        }
        return new Locale(language, region, variant);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String chineseNumber(long n, ChineseDigits zh) {
        if (n < 0) {
            n = -n;
        }
        if (n > 10) {
            int length;
            char[] buf = new char[40];
            char[] digits = String.valueOf(n).toCharArray();
            boolean inZero = true;
            boolean forcedZero = false;
            int x = buf.length;
            int i = digits.length;
            int l = -1;
            int u = -1;
            while (true) {
                i--;
                if (i < 0) {
                    break;
                }
                int u2;
                if (u == -1) {
                    if (l != -1) {
                        x--;
                        buf[x] = zh.levels[l];
                        inZero = true;
                        forcedZero = false;
                    }
                    u2 = u + 1;
                } else {
                    x--;
                    u2 = u + 1;
                    buf[x] = zh.units[u];
                    if (u2 == 3) {
                        u2 = -1;
                        l++;
                    }
                }
                int d = digits[i] - 48;
                if (d == 0) {
                    if (x < buf.length - 1 && u2 != 0) {
                        buf[x] = '*';
                    }
                    if (inZero || forcedZero) {
                        x--;
                        buf[x] = '*';
                    } else {
                        x--;
                        buf[x] = zh.digits[0];
                        inZero = true;
                        forcedZero = u2 == 1;
                    }
                } else {
                    inZero = false;
                    x--;
                    buf[x] = zh.digits[d];
                }
                u = u2;
            }
            if (n > 1000000) {
                boolean last = true;
                i = buf.length - 3;
                while (buf[i] != '0') {
                    i -= 8;
                    last = !last;
                    if (i <= x) {
                        break;
                    }
                }
                i = buf.length - 7;
                do {
                    if (buf[i] == zh.digits[0] && !last) {
                        buf[i] = '*';
                    }
                    i -= 8;
                    last = !last;
                } while (i > x);
                if (n >= 100000000) {
                    i = buf.length - 8;
                    do {
                        boolean empty = true;
                        int e = Math.max(x - 1, i - 8);
                        for (int j = i - 1; j > e; j--) {
                            if (buf[j] != '*') {
                                empty = false;
                                break;
                            }
                        }
                        if (empty) {
                            if (buf[i + 1] != '*') {
                                if (buf[i + 1] != zh.digits[0]) {
                                    buf[i] = zh.digits[0];
                                }
                            }
                            buf[i] = '*';
                        }
                        i -= 8;
                    } while (i > x);
                }
            }
            i = x;
            while (true) {
                length = buf.length;
                if (i >= r0) {
                    break;
                }
                if (buf[i] == zh.digits[2]) {
                    if (i < buf.length - 1) {
                        if (buf[i + 1] == zh.units[0]) {
                        }
                    }
                    if (i > x) {
                        if (buf[i - 1] != zh.units[0]) {
                            if (buf[i - 1] != zh.digits[0]) {
                                if (buf[i - 1] == '*') {
                                }
                            }
                        }
                    }
                    buf[i] = zh.liang;
                }
                i++;
            }
            if (buf[x] == zh.digits[1]) {
                if (!zh.ko) {
                }
                x++;
            }
            int w = x;
            int r = x;
            while (true) {
                length = buf.length;
                if (r < r0) {
                    if (buf[r] != '*') {
                        int w2 = w + 1;
                        buf[w] = buf[r];
                        w = w2;
                    }
                    r++;
                } else {
                    return new String(buf, x, w - x);
                }
            }
        } else if (n == 2) {
            return String.valueOf(zh.liang);
        } else {
            return String.valueOf(zh.digits[(int) n]);
        }
    }
}
