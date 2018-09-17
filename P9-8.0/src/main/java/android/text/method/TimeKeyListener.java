package android.text.method;

import android.text.format.DateFormat;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.ArrayUtils;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;

public class TimeKeyListener extends NumberKeyListener {
    public static final char[] CHARACTERS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', DateFormat.AM_PM, DateFormat.MINUTE, 'p', ':'};
    private static final String SKELETON_12HOUR = "hms";
    private static final String SKELETON_24HOUR = "Hms";
    private static final String SYMBOLS_TO_IGNORE = "ahHKkms";
    @GuardedBy("sLock")
    private static final HashMap<Locale, TimeKeyListener> sInstanceCache = new HashMap();
    private static final Object sLock = new Object();
    private final char[] mCharacters;
    private final boolean mNeedsAdvancedInput;

    public int getInputType() {
        if (this.mNeedsAdvancedInput) {
            return 1;
        }
        return 36;
    }

    protected char[] getAcceptedChars() {
        return this.mCharacters;
    }

    @Deprecated
    public TimeKeyListener() {
        this(null);
    }

    public TimeKeyListener(Locale locale) {
        boolean success;
        LinkedHashSet<Character> chars = new LinkedHashSet();
        if (NumberKeyListener.addDigits(chars, locale) && NumberKeyListener.addAmPmChars(chars, locale) && NumberKeyListener.addFormatCharsFromSkeleton(chars, locale, SKELETON_12HOUR, SYMBOLS_TO_IGNORE)) {
            success = NumberKeyListener.addFormatCharsFromSkeleton(chars, locale, SKELETON_24HOUR, SYMBOLS_TO_IGNORE);
        } else {
            success = false;
        }
        if (success) {
            this.mCharacters = NumberKeyListener.collectionToArray(chars);
            if (locale == null || !"en".equals(locale.getLanguage())) {
                this.mNeedsAdvancedInput = ArrayUtils.containsAll(CHARACTERS, this.mCharacters) ^ 1;
                return;
            } else {
                this.mNeedsAdvancedInput = false;
                return;
            }
        }
        this.mCharacters = CHARACTERS;
        this.mNeedsAdvancedInput = false;
    }

    @Deprecated
    public static TimeKeyListener getInstance() {
        return getInstance(null);
    }

    public static TimeKeyListener getInstance(Locale locale) {
        TimeKeyListener instance;
        synchronized (sLock) {
            instance = (TimeKeyListener) sInstanceCache.get(locale);
            if (instance == null) {
                instance = new TimeKeyListener(locale);
                sInstanceCache.put(locale, instance);
            }
        }
        return instance;
    }
}
