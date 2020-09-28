package android.text.method;

import android.icu.lang.UCharacter;
import android.icu.text.DecimalFormatSymbols;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.ArrayUtils;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;

public class DigitsKeyListener extends NumberKeyListener {
    private static final char[][] COMPATIBILITY_CHARACTERS = {new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'}, new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', HYPHEN_MINUS, '+'}, new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.'}, new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', HYPHEN_MINUS, '+', '.'}};
    private static final int DECIMAL = 2;
    private static final String DEFAULT_DECIMAL_POINT_CHARS = ".";
    private static final String DEFAULT_SIGN_CHARS = "-+";
    private static final char EN_DASH = 8211;
    private static final char HYPHEN_MINUS = '-';
    private static final char MINUS_SIGN = 8722;
    private static final int SIGN = 1;
    private static final Object sLocaleCacheLock = new Object();
    @GuardedBy({"sLocaleCacheLock"})
    private static final HashMap<Locale, DigitsKeyListener[]> sLocaleInstanceCache = new HashMap<>();
    private static final Object sStringCacheLock = new Object();
    @GuardedBy({"sStringCacheLock"})
    private static final HashMap<String, DigitsKeyListener> sStringInstanceCache = new HashMap<>();
    private char[] mAccepted;
    private final boolean mDecimal;
    private String mDecimalPointChars;
    private final Locale mLocale;
    private boolean mNeedsAdvancedInput;
    private final boolean mSign;
    private String mSignChars;
    private final boolean mStringMode;

    /* access modifiers changed from: protected */
    @Override // android.text.method.NumberKeyListener
    public char[] getAcceptedChars() {
        return this.mAccepted;
    }

    private boolean isSignChar(char c) {
        return this.mSignChars.indexOf(c) != -1;
    }

    private boolean isDecimalPointChar(char c) {
        return this.mDecimalPointChars.indexOf(c) != -1;
    }

    @Deprecated
    public DigitsKeyListener() {
        this(null, false, false);
    }

    @Deprecated
    public DigitsKeyListener(boolean sign, boolean decimal) {
        this(null, sign, decimal);
    }

    public DigitsKeyListener(Locale locale) {
        this(locale, false, false);
    }

    private void setToCompat() {
        this.mDecimalPointChars = DEFAULT_DECIMAL_POINT_CHARS;
        this.mSignChars = DEFAULT_SIGN_CHARS;
        this.mAccepted = COMPATIBILITY_CHARACTERS[(this.mSign ? 1 : 0) | (this.mDecimal ? (char) 2 : 0)];
        this.mNeedsAdvancedInput = false;
    }

    private void calculateNeedForAdvancedInput() {
        this.mNeedsAdvancedInput = !ArrayUtils.containsAll(COMPATIBILITY_CHARACTERS[(this.mSign ? 1 : 0) | (this.mDecimal ? (char) 2 : 0)], this.mAccepted);
    }

    private static String stripBidiControls(String sign) {
        String result = "";
        for (int i = 0; i < sign.length(); i++) {
            char c = sign.charAt(i);
            if (!UCharacter.hasBinaryProperty(c, 2)) {
                result = result.isEmpty() ? String.valueOf(c) : result + c;
            }
        }
        return result;
    }

    public DigitsKeyListener(Locale locale, boolean sign, boolean decimal) {
        this.mDecimalPointChars = DEFAULT_DECIMAL_POINT_CHARS;
        this.mSignChars = DEFAULT_SIGN_CHARS;
        this.mSign = sign;
        this.mDecimal = decimal;
        this.mStringMode = false;
        this.mLocale = locale;
        if (locale == null) {
            setToCompat();
            return;
        }
        LinkedHashSet<Character> chars = new LinkedHashSet<>();
        if (!NumberKeyListener.addDigits(chars, locale)) {
            setToCompat();
            return;
        }
        if (sign || decimal) {
            DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);
            if (sign) {
                String minusString = stripBidiControls(symbols.getMinusSignString());
                String plusString = stripBidiControls(symbols.getPlusSignString());
                if (minusString.length() > 1 || plusString.length() > 1) {
                    setToCompat();
                    return;
                }
                char minus = minusString.charAt(0);
                char plus = plusString.charAt(0);
                chars.add(Character.valueOf(minus));
                chars.add(Character.valueOf(plus));
                this.mSignChars = "" + minus + plus;
                if (minus == 8722 || minus == 8211) {
                    chars.add(Character.valueOf(HYPHEN_MINUS));
                    this.mSignChars += HYPHEN_MINUS;
                }
            }
            if (decimal) {
                String separatorString = symbols.getDecimalSeparatorString();
                if (separatorString.length() > 1) {
                    setToCompat();
                    return;
                }
                Character separatorChar = Character.valueOf(separatorString.charAt(0));
                chars.add(separatorChar);
                this.mDecimalPointChars = separatorChar.toString();
            }
        }
        this.mAccepted = NumberKeyListener.collectionToArray(chars);
        calculateNeedForAdvancedInput();
    }

    private DigitsKeyListener(String accepted) {
        this.mDecimalPointChars = DEFAULT_DECIMAL_POINT_CHARS;
        this.mSignChars = DEFAULT_SIGN_CHARS;
        this.mSign = false;
        this.mDecimal = false;
        this.mStringMode = true;
        this.mLocale = null;
        this.mAccepted = new char[accepted.length()];
        accepted.getChars(0, accepted.length(), this.mAccepted, 0);
        this.mNeedsAdvancedInput = false;
    }

    @Deprecated
    public static DigitsKeyListener getInstance() {
        return getInstance(false, false);
    }

    @Deprecated
    public static DigitsKeyListener getInstance(boolean sign, boolean decimal) {
        return getInstance(null, sign, decimal);
    }

    public static DigitsKeyListener getInstance(Locale locale) {
        return getInstance(locale, false, false);
    }

    public static DigitsKeyListener getInstance(Locale locale, boolean sign, boolean decimal) {
        int kind = (decimal ? (char) 2 : 0) | (sign ? 1 : 0);
        synchronized (sLocaleCacheLock) {
            DigitsKeyListener[] cachedValue = sLocaleInstanceCache.get(locale);
            if (cachedValue == null || cachedValue[kind] == null) {
                if (cachedValue == null) {
                    cachedValue = new DigitsKeyListener[4];
                    sLocaleInstanceCache.put(locale, cachedValue);
                }
                DigitsKeyListener digitsKeyListener = new DigitsKeyListener(locale, sign, decimal);
                cachedValue[kind] = digitsKeyListener;
                return digitsKeyListener;
            }
            return cachedValue[kind];
        }
    }

    public static DigitsKeyListener getInstance(String accepted) {
        DigitsKeyListener result;
        synchronized (sStringCacheLock) {
            result = sStringInstanceCache.get(accepted);
            if (result == null) {
                result = new DigitsKeyListener(accepted);
                sStringInstanceCache.put(accepted, result);
            }
        }
        return result;
    }

    public static DigitsKeyListener getInstance(Locale locale, DigitsKeyListener listener) {
        if (listener.mStringMode) {
            return listener;
        }
        return getInstance(locale, listener.mSign, listener.mDecimal);
    }

    @Override // android.text.method.KeyListener
    public int getInputType() {
        if (this.mNeedsAdvancedInput) {
            return 1;
        }
        int contentType = 2;
        if (this.mSign) {
            contentType = 2 | 4096;
        }
        if (this.mDecimal) {
            return contentType | 8192;
        }
        return contentType;
    }

    @Override // android.text.method.NumberKeyListener, android.text.InputFilter
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        int end2;
        int start2;
        CharSequence source2;
        DigitsKeyListener digitsKeyListener = this;
        CharSequence out = super.filter(source, start, end, dest, dstart, dend);
        if (!(digitsKeyListener.mSign || digitsKeyListener.mDecimal)) {
            return out;
        }
        if (out != null) {
            source2 = out;
            start2 = 0;
            end2 = out.length();
        } else {
            source2 = source;
            start2 = start;
            end2 = end;
        }
        int sign = -1;
        int decimal = -1;
        int dlen = dest.length();
        for (int i = 0; i < dstart; i++) {
            char c = dest.charAt(i);
            if (digitsKeyListener.isSignChar(c)) {
                sign = i;
            } else if (digitsKeyListener.isDecimalPointChar(c)) {
                decimal = i;
            }
        }
        for (int i2 = dend; i2 < dlen; i2++) {
            char c2 = dest.charAt(i2);
            if (digitsKeyListener.isSignChar(c2)) {
                return "";
            }
            if (digitsKeyListener.isDecimalPointChar(c2)) {
                decimal = i2;
            }
        }
        SpannableStringBuilder stripped = null;
        int i3 = end2 - 1;
        while (i3 >= start2) {
            char c3 = source2.charAt(i3);
            boolean strip = false;
            if (digitsKeyListener.isSignChar(c3)) {
                if (i3 != start2 || dstart != 0) {
                    strip = true;
                } else if (sign >= 0) {
                    strip = true;
                } else {
                    sign = i3;
                }
            } else if (digitsKeyListener.isDecimalPointChar(c3)) {
                if (decimal >= 0) {
                    strip = true;
                } else {
                    decimal = i3;
                }
            }
            if (strip) {
                if (end2 == start2 + 1) {
                    return "";
                }
                if (stripped == null) {
                    stripped = new SpannableStringBuilder(source2, start2, end2);
                }
                stripped.delete(i3 - start2, (i3 + 1) - start2);
            }
            i3--;
            digitsKeyListener = this;
        }
        if (stripped != null) {
            return stripped;
        }
        if (out != null) {
            return out;
        }
        return null;
    }
}
