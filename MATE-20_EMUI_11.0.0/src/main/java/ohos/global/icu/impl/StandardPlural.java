package ohos.global.icu.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import ohos.global.icu.text.PluralRules;

public enum StandardPlural {
    ZERO(PluralRules.KEYWORD_ZERO),
    ONE(PluralRules.KEYWORD_ONE),
    TWO(PluralRules.KEYWORD_TWO),
    FEW(PluralRules.KEYWORD_FEW),
    MANY(PluralRules.KEYWORD_MANY),
    OTHER("other");
    
    public static final int COUNT = VALUES.size();
    public static final int OTHER_INDEX = OTHER.ordinal();
    public static final List<StandardPlural> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
    private final String keyword;

    private StandardPlural(String str) {
        this.keyword = str;
    }

    public final String getKeyword() {
        return this.keyword;
    }

    public static final StandardPlural orNullFromString(CharSequence charSequence) {
        int length = charSequence.length();
        if (length != 3) {
            if (length != 4) {
                if (length == 5 && "other".contentEquals(charSequence)) {
                    return OTHER;
                }
                return null;
            } else if (PluralRules.KEYWORD_MANY.contentEquals(charSequence)) {
                return MANY;
            } else {
                if (PluralRules.KEYWORD_ZERO.contentEquals(charSequence)) {
                    return ZERO;
                }
                return null;
            }
        } else if (PluralRules.KEYWORD_ONE.contentEquals(charSequence)) {
            return ONE;
        } else {
            if (PluralRules.KEYWORD_TWO.contentEquals(charSequence)) {
                return TWO;
            }
            if (PluralRules.KEYWORD_FEW.contentEquals(charSequence)) {
                return FEW;
            }
            return null;
        }
    }

    public static final StandardPlural orOtherFromString(CharSequence charSequence) {
        StandardPlural orNullFromString = orNullFromString(charSequence);
        return orNullFromString != null ? orNullFromString : OTHER;
    }

    public static final StandardPlural fromString(CharSequence charSequence) {
        StandardPlural orNullFromString = orNullFromString(charSequence);
        if (orNullFromString != null) {
            return orNullFromString;
        }
        throw new IllegalArgumentException(charSequence.toString());
    }

    public static final int indexOrNegativeFromString(CharSequence charSequence) {
        StandardPlural orNullFromString = orNullFromString(charSequence);
        if (orNullFromString != null) {
            return orNullFromString.ordinal();
        }
        return -1;
    }

    public static final int indexOrOtherIndexFromString(CharSequence charSequence) {
        StandardPlural orNullFromString = orNullFromString(charSequence);
        if (orNullFromString == null) {
            orNullFromString = OTHER;
        }
        return orNullFromString.ordinal();
    }

    public static final int indexFromString(CharSequence charSequence) {
        StandardPlural orNullFromString = orNullFromString(charSequence);
        if (orNullFromString != null) {
            return orNullFromString.ordinal();
        }
        throw new IllegalArgumentException(charSequence.toString());
    }
}
