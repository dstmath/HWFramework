package android.icu.impl.duration.impl;

import android.icu.impl.locale.BaseLocale;
import java.util.Locale;

public class Utils {

    public static class ChineseDigits {
        public static final ChineseDigits DEBUG = new ChineseDigits("0123456789s", "sbq", "WYZ", 'L', false);
        public static final ChineseDigits KOREAN = new ChineseDigits("영일이삼사오육칠팔구십", "십백천", "만억?", 51060, true);
        public static final ChineseDigits SIMPLIFIED = new ChineseDigits("零一二三四五六七八九十", "十百千", "万亿兆", 20004, false);
        public static final ChineseDigits TRADITIONAL = new ChineseDigits("零一二三四五六七八九十", "十百千", "萬億兆", 20841, false);
        final char[] digits;
        final boolean ko;
        final char[] levels;
        final char liang;
        final char[] units;

        ChineseDigits(String digits, String units, String levels, char liang, boolean ko) {
            this.digits = digits.toCharArray();
            this.units = units.toCharArray();
            this.levels = levels.toCharArray();
            this.liang = liang;
            this.ko = ko;
        }
    }

    public static final Locale localeFromString(String s) {
        String language = s;
        String region = "";
        String variant = "";
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

    public static String chineseNumber(long n, ChineseDigits zh) {
        if (n < 0) {
            n = -n;
        }
        if (n > 10) {
            char[] buf = new char[40];
            char[] digits = String.valueOf(n).toCharArray();
            boolean inZero = true;
            boolean forcedZero = false;
            int x = buf.length;
            int i = digits.length;
            int u = -1;
            int l = -1;
            while (true) {
                int u2 = u;
                i--;
                if (i < 0) {
                    break;
                }
                if (u2 == -1) {
                    if (l != -1) {
                        x--;
                        buf[x] = zh.levels[l];
                        inZero = true;
                        forcedZero = false;
                    }
                    u = u2 + 1;
                } else {
                    x--;
                    u = u2 + 1;
                    buf[x] = zh.units[u2];
                    if (u == 3) {
                        u = -1;
                        l++;
                    }
                }
                int d = digits[i] - 48;
                if (d == 0) {
                    if (x < buf.length - 1 && u != 0) {
                        buf[x] = '*';
                    }
                    if (inZero || forcedZero) {
                        x--;
                        buf[x] = '*';
                    } else {
                        x--;
                        buf[x] = zh.digits[0];
                        inZero = true;
                        forcedZero = u == 1;
                    }
                } else {
                    inZero = false;
                    x--;
                    buf[x] = zh.digits[d];
                }
            }
            if (n > 1000000) {
                boolean last = true;
                i = buf.length - 3;
                while (buf[i] != '0') {
                    i -= 8;
                    last ^= 1;
                    if (i <= x) {
                        break;
                    }
                }
                i = buf.length - 7;
                do {
                    if (buf[i] == zh.digits[0] && (last ^ 1) != 0) {
                        buf[i] = '*';
                    }
                    i -= 8;
                    last ^= 1;
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
                            if (buf[i + 1] == '*' || buf[i + 1] == zh.digits[0]) {
                                buf[i] = '*';
                            } else {
                                buf[i] = zh.digits[0];
                            }
                        }
                        i -= 8;
                    } while (i > x);
                }
            }
            i = x;
            while (i < buf.length) {
                if (buf[i] == zh.digits[2] && ((i >= buf.length - 1 || buf[i + 1] != zh.units[0]) && (i <= x || !(buf[i - 1] == zh.units[0] || buf[i - 1] == zh.digits[0] || buf[i - 1] == '*')))) {
                    buf[i] = zh.liang;
                }
                i++;
            }
            if (buf[x] == zh.digits[1] && (zh.ko || buf[x + 1] == zh.units[0])) {
                x++;
            }
            int w = x;
            for (int r = x; r < buf.length; r++) {
                if (buf[r] != '*') {
                    int w2 = w + 1;
                    buf[w] = buf[r];
                    w = w2;
                }
            }
            return new String(buf, x, w - x);
        } else if (n == 2) {
            return String.valueOf(zh.liang);
        } else {
            return String.valueOf(zh.digits[(int) n]);
        }
    }
}
