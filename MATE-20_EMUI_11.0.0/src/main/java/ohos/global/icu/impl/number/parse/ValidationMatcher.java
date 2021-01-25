package ohos.global.icu.impl.number.parse;

import ohos.global.icu.impl.StringSegment;

public abstract class ValidationMatcher implements NumberParseMatcher {
    @Override // ohos.global.icu.impl.number.parse.NumberParseMatcher
    public boolean match(StringSegment stringSegment, ParsedNumber parsedNumber) {
        return false;
    }

    @Override // ohos.global.icu.impl.number.parse.NumberParseMatcher
    public boolean smokeTest(StringSegment stringSegment) {
        return false;
    }
}
