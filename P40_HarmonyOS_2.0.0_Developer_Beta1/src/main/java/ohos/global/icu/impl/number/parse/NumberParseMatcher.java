package ohos.global.icu.impl.number.parse;

import ohos.global.icu.impl.StringSegment;

public interface NumberParseMatcher {

    public interface Flexible {
    }

    boolean match(StringSegment stringSegment, ParsedNumber parsedNumber);

    void postProcess(ParsedNumber parsedNumber);

    boolean smokeTest(StringSegment stringSegment);
}
