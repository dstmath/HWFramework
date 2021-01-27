package ohos.global.icu.text;

import java.text.ParsePosition;

/* access modifiers changed from: package-private */
/* compiled from: NFSubstitution */
public class NumeratorSubstitution extends NFSubstitution {
    private final double denominator;
    private final boolean withZeros;

    @Override // ohos.global.icu.text.NFSubstitution
    public double composeRuleValue(double d, double d2) {
        return d / d2;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.text.NFSubstitution
    public char tokenChar() {
        return '<';
    }

    NumeratorSubstitution(int i, double d, NFRuleSet nFRuleSet, String str) {
        super(i, nFRuleSet, fixdesc(str));
        this.denominator = d;
        this.withZeros = str.endsWith("<<");
    }

    static String fixdesc(String str) {
        return str.endsWith("<<") ? str.substring(0, str.length() - 1) : str;
    }

    @Override // ohos.global.icu.text.NFSubstitution
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        NumeratorSubstitution numeratorSubstitution = (NumeratorSubstitution) obj;
        if (this.denominator == numeratorSubstitution.denominator && this.withZeros == numeratorSubstitution.withZeros) {
            return true;
        }
        return false;
    }

    @Override // ohos.global.icu.text.NFSubstitution
    public void doSubstitution(double d, StringBuilder sb, int i, int i2) {
        int i3;
        double transformNumber = transformNumber(d);
        if (!this.withZeros || this.ruleSet == null) {
            i3 = i;
        } else {
            long j = (long) transformNumber;
            int length = sb.length();
            while (true) {
                long j2 = j * 10;
                if (((double) j2) >= this.denominator) {
                    break;
                }
                sb.insert(i + this.pos, ' ');
                this.ruleSet.format(0L, sb, i + this.pos, i2);
                j = j2;
            }
            i3 = i + (sb.length() - length);
        }
        if (transformNumber == Math.floor(transformNumber) && this.ruleSet != null) {
            this.ruleSet.format((long) transformNumber, sb, i3 + this.pos, i2);
        } else if (this.ruleSet != null) {
            this.ruleSet.format(transformNumber, sb, i3 + this.pos, i2);
        } else {
            sb.insert(i3 + this.pos, this.numberFormat.format(transformNumber));
        }
    }

    @Override // ohos.global.icu.text.NFSubstitution
    public long transformNumber(long j) {
        return Math.round(((double) j) * this.denominator);
    }

    @Override // ohos.global.icu.text.NFSubstitution
    public double transformNumber(double d) {
        return (double) Math.round(d * this.denominator);
    }

    @Override // ohos.global.icu.text.NFSubstitution
    public Number doParse(String str, ParsePosition parsePosition, double d, double d2, boolean z, int i) {
        int i2;
        String str2;
        if (this.withZeros) {
            ParsePosition parsePosition2 = new ParsePosition(1);
            String str3 = str;
            i2 = 0;
            while (str3.length() > 0 && parsePosition2.getIndex() != 0) {
                parsePosition2.setIndex(0);
                this.ruleSet.parse(str3, parsePosition2, 1.0d, i).intValue();
                if (parsePosition2.getIndex() == 0) {
                    break;
                }
                i2++;
                parsePosition.setIndex(parsePosition.getIndex() + parsePosition2.getIndex());
                str3 = str3.substring(parsePosition2.getIndex());
                while (str3.length() > 0 && str3.charAt(0) == ' ') {
                    str3 = str3.substring(1);
                    parsePosition.setIndex(parsePosition.getIndex() + 1);
                }
            }
            String substring = str.substring(parsePosition.getIndex());
            parsePosition.setIndex(0);
            str2 = substring;
        } else {
            i2 = 0;
            str2 = str;
        }
        Number doParse = super.doParse(str2, parsePosition, this.withZeros ? 1.0d : d, d2, false, i);
        if (!this.withZeros) {
            return doParse;
        }
        long longValue = doParse.longValue();
        long j = 1;
        while (j <= longValue) {
            j *= 10;
        }
        while (i2 > 0) {
            j *= 10;
            i2--;
        }
        return new Double(((double) longValue) / ((double) j));
    }

    @Override // ohos.global.icu.text.NFSubstitution
    public double calcUpperBound(double d) {
        return this.denominator;
    }
}
