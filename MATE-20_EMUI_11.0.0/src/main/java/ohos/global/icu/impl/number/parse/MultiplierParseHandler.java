package ohos.global.icu.impl.number.parse;

import ohos.global.icu.number.Scale;

public class MultiplierParseHandler extends ValidationMatcher {
    private final Scale multiplier;

    public MultiplierParseHandler(Scale scale) {
        this.multiplier = scale;
    }

    @Override // ohos.global.icu.impl.number.parse.NumberParseMatcher
    public void postProcess(ParsedNumber parsedNumber) {
        if (parsedNumber.quantity != null) {
            this.multiplier.applyReciprocalTo(parsedNumber.quantity);
        }
    }

    public String toString() {
        return "<MultiplierHandler " + this.multiplier + ">";
    }
}
