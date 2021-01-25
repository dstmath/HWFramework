package ohos.global.icu.impl.number.parse;

public class RequireDecimalSeparatorValidator extends ValidationMatcher {
    private static final RequireDecimalSeparatorValidator A = new RequireDecimalSeparatorValidator(true);
    private static final RequireDecimalSeparatorValidator B = new RequireDecimalSeparatorValidator(false);
    private final boolean patternHasDecimalSeparator;

    public String toString() {
        return "<RequireDecimalSeparator>";
    }

    public static RequireDecimalSeparatorValidator getInstance(boolean z) {
        return z ? A : B;
    }

    private RequireDecimalSeparatorValidator(boolean z) {
        this.patternHasDecimalSeparator = z;
    }

    @Override // ohos.global.icu.impl.number.parse.NumberParseMatcher
    public void postProcess(ParsedNumber parsedNumber) {
        if (((parsedNumber.flags & 32) != 0) != this.patternHasDecimalSeparator) {
            parsedNumber.flags |= 256;
        }
    }
}
