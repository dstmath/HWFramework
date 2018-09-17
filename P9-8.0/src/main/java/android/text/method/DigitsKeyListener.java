package android.text.method;

import android.icu.lang.UCharacter;
import android.icu.text.DecimalFormatSymbols;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.LogException;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.ArrayUtils;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;

public class DigitsKeyListener extends NumberKeyListener {
    private static final char[][] COMPATIBILITY_CHARACTERS = new char[][]{new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'}, new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', HYPHEN_MINUS, '+'}, new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.'}, new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', HYPHEN_MINUS, '+', '.'}};
    private static final int DECIMAL = 2;
    private static final String DEFAULT_DECIMAL_POINT_CHARS = ".";
    private static final String DEFAULT_SIGN_CHARS = "-+";
    private static final char EN_DASH = '–';
    private static final char HYPHEN_MINUS = '-';
    private static final char MINUS_SIGN = '−';
    private static final int SIGN = 1;
    private static final Object sLocaleCacheLock = new Object();
    @GuardedBy("sLocaleCacheLock")
    private static final HashMap<Locale, DigitsKeyListener[]> sLocaleInstanceCache = new HashMap();
    private static final Object sStringCacheLock = new Object();
    @GuardedBy("sStringCacheLock")
    private static final HashMap<String, DigitsKeyListener> sStringInstanceCache = new HashMap();
    private char[] mAccepted;
    private final boolean mDecimal;
    private String mDecimalPointChars;
    private final Locale mLocale;
    private boolean mNeedsAdvancedInput;
    private final boolean mSign;
    private String mSignChars;
    private final boolean mStringMode;

    protected char[] getAcceptedChars() {
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
        int i;
        this.mDecimalPointChars = DEFAULT_DECIMAL_POINT_CHARS;
        this.mSignChars = DEFAULT_SIGN_CHARS;
        if (this.mSign) {
            i = 1;
        } else {
            i = 0;
        }
        this.mAccepted = COMPATIBILITY_CHARACTERS[i | (this.mDecimal ? 2 : 0)];
        this.mNeedsAdvancedInput = false;
    }

    private void calculateNeedForAdvancedInput() {
        int i;
        int i2 = 0;
        if (this.mSign) {
            i = 1;
        } else {
            i = 0;
        }
        if (this.mDecimal) {
            i2 = 2;
        }
        this.mNeedsAdvancedInput = ArrayUtils.containsAll(COMPATIBILITY_CHARACTERS[i | i2], this.mAccepted) ^ 1;
    }

    private static String stripBidiControls(String sign) {
        String result = LogException.NO_VALUE;
        for (int i = 0; i < sign.length(); i++) {
            char c = sign.charAt(i);
            if (!UCharacter.hasBinaryProperty(c, 2)) {
                if (result.isEmpty()) {
                    result = String.valueOf(c);
                } else {
                    result = result + c;
                }
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
        LinkedHashSet<Character> chars = new LinkedHashSet();
        if (NumberKeyListener.addDigits(chars, locale)) {
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
                    this.mSignChars = LogException.NO_VALUE + minus + plus;
                    if (minus == MINUS_SIGN || minus == EN_DASH) {
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
            return;
        }
        setToCompat();
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
        int i;
        int i2 = 0;
        if (sign) {
            i = 1;
        } else {
            i = 0;
        }
        if (decimal) {
            i2 = 2;
        }
        int kind = i | i2;
        synchronized (sLocaleCacheLock) {
            DigitsKeyListener[] cachedValue = (DigitsKeyListener[]) sLocaleInstanceCache.get(locale);
            DigitsKeyListener digitsKeyListener;
            if (cachedValue == null || cachedValue[kind] == null) {
                if (cachedValue == null) {
                    cachedValue = new DigitsKeyListener[4];
                    sLocaleInstanceCache.put(locale, cachedValue);
                }
                digitsKeyListener = new DigitsKeyListener(locale, sign, decimal);
                cachedValue[kind] = digitsKeyListener;
                return digitsKeyListener;
            }
            digitsKeyListener = cachedValue[kind];
            return digitsKeyListener;
        }
    }

    public static DigitsKeyListener getInstance(String accepted) {
        DigitsKeyListener result;
        synchronized (sStringCacheLock) {
            result = (DigitsKeyListener) sStringInstanceCache.get(accepted);
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

    public int getInputType() {
        if (this.mNeedsAdvancedInput) {
            return 1;
        }
        int contentType = 2;
        if (this.mSign) {
            contentType = 4098;
        }
        if (this.mDecimal) {
            return contentType | 8192;
        }
        return contentType;
    }

    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        CharSequence out = super.filter(source, start, end, dest, dstart, dend);
        if (!this.mSign && !this.mDecimal) {
            return out;
        }
        int i;
        char c;
        if (out != null) {
            source = out;
            start = 0;
            end = out.length();
        }
        int sign = -1;
        int decimal = -1;
        int dlen = dest.length();
        for (i = 0; i < dstart; i++) {
            c = dest.charAt(i);
            if (isSignChar(c)) {
                sign = i;
            } else if (isDecimalPointChar(c)) {
                decimal = i;
            }
        }
        for (i = dend; i < dlen; i++) {
            c = dest.charAt(i);
            if (isSignChar(c)) {
                return LogException.NO_VALUE;
            }
            if (isDecimalPointChar(c)) {
                decimal = i;
            }
        }
        CharSequence stripped = null;
        for (i = end - 1; i >= start; i--) {
            c = source.charAt(i);
            boolean strip = false;
            if (isSignChar(c)) {
                if (i != start || dstart != 0) {
                    strip = true;
                } else if (sign >= 0) {
                    strip = true;
                } else {
                    sign = i;
                }
            } else if (isDecimalPointChar(c)) {
                if (decimal >= 0) {
                    strip = true;
                } else {
                    decimal = i;
                }
            }
            if (strip) {
                if (end == start + 1) {
                    return LogException.NO_VALUE;
                }
                if (stripped == null) {
                    stripped = new SpannableStringBuilder(source, start, end);
                }
                stripped.delete(i - start, (i + 1) - start);
            }
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
