package ohos.global.icu.impl.number.parse;

import ohos.global.icu.impl.StaticUnicodeSets;
import ohos.global.icu.impl.StringSegment;
import ohos.global.icu.impl.number.parse.NumberParseMatcher;
import ohos.global.icu.text.UnicodeSet;

public class IgnorablesMatcher extends SymbolMatcher implements NumberParseMatcher.Flexible {
    private static final IgnorablesMatcher DEFAULT = new IgnorablesMatcher(StaticUnicodeSets.get(StaticUnicodeSets.Key.DEFAULT_IGNORABLES));
    private static final IgnorablesMatcher JAVA_COMPATIBILITY = new IgnorablesMatcher(StaticUnicodeSets.get(StaticUnicodeSets.Key.EMPTY));
    private static final IgnorablesMatcher STRICT = new IgnorablesMatcher(StaticUnicodeSets.get(StaticUnicodeSets.Key.STRICT_IGNORABLES));

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.number.parse.SymbolMatcher
    public void accept(StringSegment stringSegment, ParsedNumber parsedNumber) {
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.number.parse.SymbolMatcher
    public boolean isDisabled(ParsedNumber parsedNumber) {
        return false;
    }

    public String toString() {
        return "<IgnorablesMatcher>";
    }

    public static IgnorablesMatcher getInstance(int i) {
        if ((65536 & i) != 0) {
            return JAVA_COMPATIBILITY;
        }
        if ((i & 32768) != 0) {
            return STRICT;
        }
        return DEFAULT;
    }

    private IgnorablesMatcher(UnicodeSet unicodeSet) {
        super("", unicodeSet);
    }
}
