package android.icu.impl;

import android.icu.text.PluralRules;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum StandardPlural {
    ZERO(PluralRules.KEYWORD_ZERO),
    ONE(PluralRules.KEYWORD_ONE),
    TWO(PluralRules.KEYWORD_TWO),
    FEW(PluralRules.KEYWORD_FEW),
    MANY(PluralRules.KEYWORD_MANY),
    OTHER("other");
    
    public static final int COUNT = 0;
    public static final int OTHER_INDEX = 0;
    public static final List<StandardPlural> VALUES = null;
    private final String keyword;

    static {
        OTHER_INDEX = OTHER.ordinal();
        VALUES = Collections.unmodifiableList(Arrays.asList(values()));
        COUNT = VALUES.size();
    }

    private StandardPlural(String kw) {
        this.keyword = kw;
    }

    public final String getKeyword() {
        return this.keyword;
    }

    public static final StandardPlural orNullFromString(CharSequence keyword) {
        switch (keyword.length()) {
            case 3:
                if (PluralRules.KEYWORD_ONE.contentEquals(keyword)) {
                    return ONE;
                }
                if (PluralRules.KEYWORD_TWO.contentEquals(keyword)) {
                    return TWO;
                }
                if (PluralRules.KEYWORD_FEW.contentEquals(keyword)) {
                    return FEW;
                }
                break;
            case 4:
                if (PluralRules.KEYWORD_MANY.contentEquals(keyword)) {
                    return MANY;
                }
                if (PluralRules.KEYWORD_ZERO.contentEquals(keyword)) {
                    return ZERO;
                }
                break;
            case 5:
                if ("other".contentEquals(keyword)) {
                    return OTHER;
                }
                break;
        }
        return null;
    }

    public static final StandardPlural orOtherFromString(CharSequence keyword) {
        StandardPlural p = orNullFromString(keyword);
        return p != null ? p : OTHER;
    }

    public static final StandardPlural fromString(CharSequence keyword) {
        StandardPlural p = orNullFromString(keyword);
        if (p != null) {
            return p;
        }
        throw new IllegalArgumentException(keyword.toString());
    }

    public static final int indexOrNegativeFromString(CharSequence keyword) {
        StandardPlural p = orNullFromString(keyword);
        return p != null ? p.ordinal() : -1;
    }

    public static final int indexOrOtherIndexFromString(CharSequence keyword) {
        StandardPlural p = orNullFromString(keyword);
        return p != null ? p.ordinal() : OTHER.ordinal();
    }

    public static final int indexFromString(CharSequence keyword) {
        StandardPlural p = orNullFromString(keyword);
        if (p != null) {
            return p.ordinal();
        }
        throw new IllegalArgumentException(keyword.toString());
    }
}
