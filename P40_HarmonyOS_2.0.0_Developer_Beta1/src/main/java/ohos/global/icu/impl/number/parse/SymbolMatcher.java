package ohos.global.icu.impl.number.parse;

import ohos.global.icu.impl.StaticUnicodeSets;
import ohos.global.icu.impl.StringSegment;
import ohos.global.icu.text.UnicodeSet;

public abstract class SymbolMatcher implements NumberParseMatcher {
    protected final String string;
    protected final UnicodeSet uniSet;

    /* access modifiers changed from: protected */
    public abstract void accept(StringSegment stringSegment, ParsedNumber parsedNumber);

    /* access modifiers changed from: protected */
    public abstract boolean isDisabled(ParsedNumber parsedNumber);

    @Override // ohos.global.icu.impl.number.parse.NumberParseMatcher
    public void postProcess(ParsedNumber parsedNumber) {
    }

    protected SymbolMatcher(String str, UnicodeSet unicodeSet) {
        this.string = str;
        this.uniSet = unicodeSet;
    }

    protected SymbolMatcher(StaticUnicodeSets.Key key) {
        this.string = "";
        this.uniSet = StaticUnicodeSets.get(key);
    }

    public UnicodeSet getSet() {
        return this.uniSet;
    }

    @Override // ohos.global.icu.impl.number.parse.NumberParseMatcher
    public boolean match(StringSegment stringSegment, ParsedNumber parsedNumber) {
        int i;
        if (isDisabled(parsedNumber)) {
            return false;
        }
        if (!this.string.isEmpty()) {
            i = stringSegment.getCommonPrefixLength(this.string);
            if (i == this.string.length()) {
                stringSegment.adjustOffset(this.string.length());
                accept(stringSegment, parsedNumber);
                return false;
            }
        } else {
            i = 0;
        }
        if (stringSegment.startsWith(this.uniSet)) {
            stringSegment.adjustOffsetByCodePoint();
            accept(stringSegment, parsedNumber);
            return false;
        } else if (i == stringSegment.length()) {
            return true;
        } else {
            return false;
        }
    }

    @Override // ohos.global.icu.impl.number.parse.NumberParseMatcher
    public boolean smokeTest(StringSegment stringSegment) {
        return stringSegment.startsWith(this.uniSet) || stringSegment.startsWith(this.string);
    }
}
