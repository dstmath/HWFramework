package ohos.global.icu.text;

/* access modifiers changed from: package-private */
/* compiled from: NFSubstitution */
public class IntegralPartSubstitution extends NFSubstitution {
    @Override // ohos.global.icu.text.NFSubstitution
    public double calcUpperBound(double d) {
        return Double.MAX_VALUE;
    }

    @Override // ohos.global.icu.text.NFSubstitution
    public double composeRuleValue(double d, double d2) {
        return d + d2;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.text.NFSubstitution
    public char tokenChar() {
        return '<';
    }

    @Override // ohos.global.icu.text.NFSubstitution
    public long transformNumber(long j) {
        return j;
    }

    IntegralPartSubstitution(int i, NFRuleSet nFRuleSet, String str) {
        super(i, nFRuleSet, str);
    }

    @Override // ohos.global.icu.text.NFSubstitution
    public double transformNumber(double d) {
        return Math.floor(d);
    }
}
