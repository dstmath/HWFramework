package android.icu.text;

import android.icu.impl.number.DecimalQuantity_DualStorageBCD;
import java.text.ParsePosition;

/* compiled from: NFSubstitution */
class FractionalPartSubstitution extends NFSubstitution {
    private final boolean byDigits;
    private final boolean useSpaces;

    FractionalPartSubstitution(int pos, NFRuleSet ruleSet, String description) {
        super(pos, ruleSet, description);
        if (description.equals(">>") || description.equals(">>>") || ruleSet == this.ruleSet) {
            this.byDigits = true;
            this.useSpaces = !description.equals(">>>");
            return;
        }
        this.byDigits = false;
        this.useSpaces = true;
        this.ruleSet.makeIntoFractionRuleSet();
    }

    public void doSubstitution(double number, StringBuilder toInsertInto, int position, int recursionCount) {
        if (!this.byDigits) {
            super.doSubstitution(number, toInsertInto, position, recursionCount);
            return;
        }
        DecimalQuantity_DualStorageBCD fq = new DecimalQuantity_DualStorageBCD(number);
        fq.roundToInfinity();
        boolean pad = false;
        for (int mag = fq.getLowerDisplayMagnitude(); mag < 0; mag++) {
            if (!pad || !this.useSpaces) {
                pad = true;
            } else {
                toInsertInto.insert(this.pos + position, ' ');
            }
            this.ruleSet.format((long) fq.getDigit(mag), toInsertInto, position + this.pos, recursionCount);
        }
    }

    public long transformNumber(long number) {
        return 0;
    }

    public double transformNumber(double number) {
        return number - Math.floor(number);
    }

    public Number doParse(String text, ParsePosition parsePosition, double baseValue, double upperBound, boolean lenientParse) {
        ParsePosition parsePosition2 = parsePosition;
        if (!this.byDigits) {
            return super.doParse(text, parsePosition2, baseValue, 0.0d, lenientParse);
        }
        ParsePosition workPos = new ParsePosition(1);
        DecimalQuantity_DualStorageBCD fq = new DecimalQuantity_DualStorageBCD();
        String workText = text;
        int leadingZeros = 0;
        while (workText.length() > 0 && workPos.getIndex() != 0) {
            workPos.setIndex(0);
            int digit = this.ruleSet.parse(workText, workPos, 10.0d).intValue();
            if (lenientParse && workPos.getIndex() == 0) {
                Number n = this.ruleSet.owner.getDecimalFormat().parse(workText, workPos);
                if (n != null) {
                    digit = n.intValue();
                }
            }
            if (workPos.getIndex() != 0) {
                if (digit == 0) {
                    leadingZeros++;
                } else {
                    fq.appendDigit((byte) digit, leadingZeros, false);
                    leadingZeros = 0;
                }
                parsePosition2.setIndex(parsePosition.getIndex() + workPos.getIndex());
                workText = workText.substring(workPos.getIndex());
                while (workText.length() > 0 && workText.charAt(0) == ' ') {
                    workText = workText.substring(1);
                    parsePosition2.setIndex(parsePosition.getIndex() + 1);
                }
            }
        }
        return new Double(composeRuleValue(fq.toDouble(), baseValue));
    }

    public double composeRuleValue(double newRuleValue, double oldRuleValue) {
        return newRuleValue + oldRuleValue;
    }

    public double calcUpperBound(double oldUpperBound) {
        return 0.0d;
    }

    /* access modifiers changed from: package-private */
    public char tokenChar() {
        return '>';
    }
}
