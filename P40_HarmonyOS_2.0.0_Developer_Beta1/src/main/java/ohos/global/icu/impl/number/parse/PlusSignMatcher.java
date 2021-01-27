package ohos.global.icu.impl.number.parse;

import ohos.global.icu.impl.StaticUnicodeSets;
import ohos.global.icu.impl.StringSegment;
import ohos.global.icu.text.DecimalFormatSymbols;

public class PlusSignMatcher extends SymbolMatcher {
    private static final PlusSignMatcher DEFAULT = new PlusSignMatcher(false);
    private static final PlusSignMatcher DEFAULT_ALLOW_TRAILING = new PlusSignMatcher(true);
    private final boolean allowTrailing;

    public String toString() {
        return "<PlusSignMatcher>";
    }

    public static PlusSignMatcher getInstance(DecimalFormatSymbols decimalFormatSymbols, boolean z) {
        String plusSignString = decimalFormatSymbols.getPlusSignString();
        if (ParsingUtils.safeContains(DEFAULT.uniSet, plusSignString)) {
            return z ? DEFAULT_ALLOW_TRAILING : DEFAULT;
        }
        return new PlusSignMatcher(plusSignString, z);
    }

    private PlusSignMatcher(String str, boolean z) {
        super(str, DEFAULT.uniSet);
        this.allowTrailing = z;
    }

    private PlusSignMatcher(boolean z) {
        super(StaticUnicodeSets.Key.PLUS_SIGN);
        this.allowTrailing = z;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.number.parse.SymbolMatcher
    public boolean isDisabled(ParsedNumber parsedNumber) {
        return !this.allowTrailing && parsedNumber.seenNumber();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.number.parse.SymbolMatcher
    public void accept(StringSegment stringSegment, ParsedNumber parsedNumber) {
        parsedNumber.setCharsConsumed(stringSegment);
    }
}
