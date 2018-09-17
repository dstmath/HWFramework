package android.text.method;

import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.ArrayUtils;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;

public class DateKeyListener extends NumberKeyListener {
    @Deprecated
    public static final char[] CHARACTERS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '/', '-', '.'};
    private static final String[] SKELETONS = new String[]{"yMd", "yM", "Md"};
    private static final String SYMBOLS_TO_IGNORE = "yMLd";
    @GuardedBy("sLock")
    private static final HashMap<Locale, DateKeyListener> sInstanceCache = new HashMap();
    private static final Object sLock = new Object();
    private final char[] mCharacters;
    private final boolean mNeedsAdvancedInput;

    public int getInputType() {
        if (this.mNeedsAdvancedInput) {
            return 1;
        }
        return 20;
    }

    protected char[] getAcceptedChars() {
        return this.mCharacters;
    }

    @Deprecated
    public DateKeyListener() {
        this(null);
    }

    public DateKeyListener(Locale locale) {
        boolean success;
        LinkedHashSet<Character> chars = new LinkedHashSet();
        if (NumberKeyListener.addDigits(chars, locale)) {
            success = NumberKeyListener.addFormatCharsFromSkeletons(chars, locale, SKELETONS, SYMBOLS_TO_IGNORE);
        } else {
            success = false;
        }
        if (success) {
            this.mCharacters = NumberKeyListener.collectionToArray(chars);
            this.mNeedsAdvancedInput = ArrayUtils.containsAll(CHARACTERS, this.mCharacters) ^ 1;
            return;
        }
        this.mCharacters = CHARACTERS;
        this.mNeedsAdvancedInput = false;
    }

    @Deprecated
    public static DateKeyListener getInstance() {
        return getInstance(null);
    }

    public static DateKeyListener getInstance(Locale locale) {
        DateKeyListener instance;
        synchronized (sLock) {
            instance = (DateKeyListener) sInstanceCache.get(locale);
            if (instance == null) {
                instance = new DateKeyListener(locale);
                sInstanceCache.put(locale, instance);
            }
        }
        return instance;
    }
}
