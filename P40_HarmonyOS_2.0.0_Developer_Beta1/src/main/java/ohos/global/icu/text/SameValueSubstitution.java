package ohos.global.icu.text;

/* access modifiers changed from: package-private */
/* compiled from: NFSubstitution */
public class SameValueSubstitution extends NFSubstitution {
    @Override // ohos.global.icu.text.NFSubstitution
    public double calcUpperBound(double d) {
        return d;
    }

    @Override // ohos.global.icu.text.NFSubstitution
    public double composeRuleValue(double d, double d2) {
        return d;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.text.NFSubstitution
    public char tokenChar() {
        return '=';
    }

    @Override // ohos.global.icu.text.NFSubstitution
    public double transformNumber(double d) {
        return d;
    }

    @Override // ohos.global.icu.text.NFSubstitution
    public long transformNumber(long j) {
        return j;
    }

    SameValueSubstitution(int i, NFRuleSet nFRuleSet, String str) {
        super(i, nFRuleSet, str);
        if (str.equals("==")) {
            throw new IllegalArgumentException("== is not a legal token");
        }
    }
}
