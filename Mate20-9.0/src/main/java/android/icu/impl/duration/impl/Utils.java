package android.icu.impl.duration.impl;

import android.icu.impl.locale.BaseLocale;
import java.util.Locale;

public class Utils {

    public static class ChineseDigits {
        public static final ChineseDigits DEBUG;
        public static final ChineseDigits KOREAN;
        public static final ChineseDigits SIMPLIFIED;
        public static final ChineseDigits TRADITIONAL;
        final char[] digits;
        final boolean ko;
        final char[] levels;
        final char liang;
        final char[] units;

        ChineseDigits(String digits2, String units2, String levels2, char liang2, boolean ko2) {
            this.digits = digits2.toCharArray();
            this.units = units2.toCharArray();
            this.levels = levels2.toCharArray();
            this.liang = liang2;
            this.ko = ko2;
        }

        static {
            ChineseDigits chineseDigits = new ChineseDigits("0123456789s", "sbq", "WYZ", 'L', false);
            DEBUG = chineseDigits;
            ChineseDigits chineseDigits2 = new ChineseDigits("零一二三四五六七八九十", "十百千", "萬億兆", 20841, false);
            TRADITIONAL = chineseDigits2;
            ChineseDigits chineseDigits3 = new ChineseDigits("零一二三四五六七八九十", "十百千", "万亿兆", 20004, false);
            SIMPLIFIED = chineseDigits3;
            ChineseDigits chineseDigits4 = new ChineseDigits("영일이삼사오육칠팔구십", "십백천", "만억?", 51060, true);
            KOREAN = chineseDigits4;
        }
    }

    public static final Locale localeFromString(String s) {
        String language = s;
        String region = "";
        String variant = "";
        int x = language.indexOf(BaseLocale.SEP);
        if (x != -1) {
            region = language.substring(x + 1);
            language = language.substring(0, x);
        }
        int x2 = region.indexOf(BaseLocale.SEP);
        if (x2 != -1) {
            variant = region.substring(x2 + 1);
            region = region.substring(0, x2);
        }
        return new Locale(language, region, variant);
    }

    public static String chineseNumber(long n, ChineseDigits zh) {
        long n2 = n;
        ChineseDigits chineseDigits = zh;
        if (n2 < 0) {
            n2 = -n2;
        }
        if (n2 > 10) {
            char[] buf = new char[40];
            char[] digits = String.valueOf(n2).toCharArray();
            int x = buf.length;
            int i = digits.length;
            int u = -1;
            int i2 = -1;
            boolean forcedZero = false;
            boolean inZero = true;
            int l = -1;
            while (true) {
                i += i2;
                boolean z = true;
                if (i < 0) {
                    break;
                }
                if (u == i2) {
                    if (l != i2) {
                        x--;
                        buf[x] = chineseDigits.levels[l];
                        inZero = true;
                        forcedZero = false;
                    }
                    u++;
                } else {
                    x--;
                    int u2 = u + 1;
                    buf[x] = chineseDigits.units[u];
                    if (u2 == 3) {
                        u = -1;
                        l++;
                    } else {
                        u = u2;
                    }
                }
                int d = digits[i] - '0';
                if (d == 0) {
                    if (x < buf.length - 1 && u != 0) {
                        buf[x] = '*';
                    }
                    if (inZero || forcedZero) {
                        x--;
                        buf[x] = '*';
                    } else {
                        x--;
                        buf[x] = chineseDigits.digits[0];
                        inZero = true;
                        if (u != 1) {
                            z = false;
                        }
                        forcedZero = z;
                    }
                } else {
                    inZero = false;
                    x--;
                    buf[x] = chineseDigits.digits[d];
                }
                i2 = -1;
            }
            if (n2 > 1000000) {
                boolean last = true;
                int i3 = buf.length - 3;
                while (buf[i3] != '0') {
                    i3 -= 8;
                    last = !last;
                    if (i3 <= x) {
                        break;
                    }
                }
                int i4 = buf.length - 7;
                do {
                    if (buf[i4] == chineseDigits.digits[0] && !last) {
                        buf[i4] = '*';
                    }
                    i4 -= 8;
                    last = !last;
                } while (i4 > x);
                if (n2 >= 100000000) {
                    int i5 = buf.length - 8;
                    do {
                        boolean empty = true;
                        int j = i5 - 1;
                        int e = Math.max(x - 1, i5 - 8);
                        while (true) {
                            if (j <= e) {
                                break;
                            } else if (buf[j] != '*') {
                                empty = false;
                                break;
                            } else {
                                j--;
                            }
                        }
                        if (empty) {
                            if (buf[i5 + 1] == '*' || buf[i5 + 1] == chineseDigits.digits[0]) {
                                buf[i5] = '*';
                            } else {
                                buf[i5] = chineseDigits.digits[0];
                            }
                        }
                        i5 -= 8;
                    } while (i5 > x);
                }
            }
            for (int i6 = x; i6 < buf.length; i6++) {
                if (buf[i6] == chineseDigits.digits[2] && ((i6 >= buf.length - 1 || buf[i6 + 1] != chineseDigits.units[0]) && (i6 <= x || !(buf[i6 - 1] == chineseDigits.units[0] || buf[i6 - 1] == chineseDigits.digits[0] || buf[i6 - 1] == '*')))) {
                    buf[i6] = chineseDigits.liang;
                }
            }
            if (buf[x] == chineseDigits.digits[1] && (chineseDigits.ko || buf[x + 1] == chineseDigits.units[0])) {
                x++;
            }
            int r = x;
            int w = r;
            while (r < buf.length) {
                if (buf[r] != '*') {
                    buf[w] = buf[r];
                    w++;
                }
                r++;
            }
            return new String(buf, x, w - x);
        } else if (n2 == 2) {
            return String.valueOf(chineseDigits.liang);
        } else {
            return String.valueOf(chineseDigits.digits[(int) n2]);
        }
    }
}
