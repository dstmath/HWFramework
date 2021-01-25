package ohos.global.icu.impl.number.parse;

public class RequireNumberValidator extends ValidationMatcher {
    public String toString() {
        return "<RequireNumber>";
    }

    @Override // ohos.global.icu.impl.number.parse.NumberParseMatcher
    public void postProcess(ParsedNumber parsedNumber) {
        if (!parsedNumber.seenNumber()) {
            parsedNumber.flags |= 256;
        }
    }
}
