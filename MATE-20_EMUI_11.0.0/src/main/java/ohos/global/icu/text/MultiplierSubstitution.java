package ohos.global.icu.text;

/* access modifiers changed from: package-private */
/* compiled from: NFSubstitution */
public class MultiplierSubstitution extends NFSubstitution {
    long divisor;

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.text.NFSubstitution
    public char tokenChar() {
        return '<';
    }

    MultiplierSubstitution(int i, NFRule nFRule, NFRuleSet nFRuleSet, String str) {
        super(i, nFRuleSet, str);
        this.divisor = nFRule.getDivisor();
        if (this.divisor == 0) {
            throw new IllegalStateException("Substitution with divisor 0 " + str.substring(0, i) + " | " + str.substring(i));
        }
    }

    @Override // ohos.global.icu.text.NFSubstitution
    public void setDivisor(int i, short s) {
        this.divisor = NFRule.power((long) i, s);
        if (this.divisor == 0) {
            throw new IllegalStateException("Substitution with divisor 0");
        }
    }

    @Override // ohos.global.icu.text.NFSubstitution
    public boolean equals(Object obj) {
        return super.equals(obj) && this.divisor == ((MultiplierSubstitution) obj).divisor;
    }

    @Override // ohos.global.icu.text.NFSubstitution
    public long transformNumber(long j) {
        return (long) Math.floor((double) (j / this.divisor));
    }

    @Override // ohos.global.icu.text.NFSubstitution
    public double transformNumber(double d) {
        if (this.ruleSet == null) {
            return d / ((double) this.divisor);
        }
        return Math.floor(d / ((double) this.divisor));
    }

    @Override // ohos.global.icu.text.NFSubstitution
    public double composeRuleValue(double d, double d2) {
        return d * ((double) this.divisor);
    }

    @Override // ohos.global.icu.text.NFSubstitution
    public double calcUpperBound(double d) {
        return (double) this.divisor;
    }
}
