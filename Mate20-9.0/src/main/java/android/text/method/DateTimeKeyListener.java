package android.text.method;

import android.text.format.DateFormat;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.ArrayUtils;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;

public class DateTimeKeyListener extends NumberKeyListener {
    public static final char[] CHARACTERS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', DateFormat.AM_PM, DateFormat.MINUTE, 'p', ':', '/', '-', ' '};
    private static final String SKELETON_12HOUR = "yMdhms";
    private static final String SKELETON_24HOUR = "yMdHms";
    private static final String SYMBOLS_TO_IGNORE = "yMLdahHKkms";
    @GuardedBy("sLock")
    private static final HashMap<Locale, DateTimeKeyListener> sInstanceCache = new HashMap<>();
    private static final Object sLock = new Object();
    private final char[] mCharacters;
    private final boolean mNeedsAdvancedInput;

    public int getInputType() {
        if (this.mNeedsAdvancedInput) {
            return 1;
        }
        return 4;
    }

    /* access modifiers changed from: protected */
    public char[] getAcceptedChars() {
        return this.mCharacters;
    }

    @Deprecated
    public DateTimeKeyListener() {
        this(null);
    }

    public DateTimeKeyListener(Locale locale) {
        LinkedHashSet<Character> chars = new LinkedHashSet<>();
        if (NumberKeyListener.addDigits(chars, locale) && NumberKeyListener.addAmPmChars(chars, locale) && NumberKeyListener.addFormatCharsFromSkeleton(chars, locale, SKELETON_12HOUR, SYMBOLS_TO_IGNORE) && NumberKeyListener.addFormatCharsFromSkeleton(chars, locale, SKELETON_24HOUR, SYMBOLS_TO_IGNORE)) {
            this.mCharacters = NumberKeyListener.collectionToArray(chars);
            if (locale == null || !"en".equals(locale.getLanguage())) {
                this.mNeedsAdvancedInput = true ^ ArrayUtils.containsAll(CHARACTERS, this.mCharacters);
            } else {
                this.mNeedsAdvancedInput = false;
            }
        } else {
            this.mCharacters = CHARACTERS;
            this.mNeedsAdvancedInput = false;
        }
    }

    @Deprecated
    public static DateTimeKeyListener getInstance() {
        return getInstance(null);
    }

    public static DateTimeKeyListener getInstance(Locale locale) {
        DateTimeKeyListener instance;
        synchronized (sLock) {
            instance = sInstanceCache.get(locale);
            if (instance == null) {
                instance = new DateTimeKeyListener(locale);
                sInstanceCache.put(locale, instance);
            }
        }
        return instance;
    }
}
