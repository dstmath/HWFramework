package android.icu.text;

import java.text.ParsePosition;

/* compiled from: NFSubstitution */
class FractionalPartSubstitution extends NFSubstitution {
    private final boolean byDigits;
    private final boolean useSpaces;

    FractionalPartSubstitution(int pos, NFRuleSet ruleSet, String description) {
        super(pos, ruleSet, description);
        if (description.equals(">>") || description.equals(">>>") || ruleSet == this.ruleSet) {
            this.byDigits = true;
            this.useSpaces = description.equals(">>>") ^ 1;
            return;
        }
        this.byDigits = false;
        this.useSpaces = true;
        this.ruleSet.makeIntoFractionRuleSet();
    }

    public void doSubstitution(double number, StringBuilder toInsertInto, int position, int recursionCount) {
        if (this.byDigits) {
            DigitList dl = new DigitList();
            dl.set(number, 20, true);
            boolean pad = false;
            while (dl.count > Math.max(0, dl.decimalAt)) {
                if (pad && this.useSpaces) {
                    toInsertInto.insert(this.pos + position, ' ');
                } else {
                    pad = true;
                }
                NFRuleSet nFRuleSet = this.ruleSet;
                byte[] bArr = dl.digits;
                int i = dl.count - 1;
                dl.count = i;
                nFRuleSet.format((long) (bArr[i] - 48), toInsertInto, position + this.pos, recursionCount);
            }
            while (dl.decimalAt < 0) {
                if (pad && this.useSpaces) {
                    toInsertInto.insert(this.pos + position, ' ');
                } else {
                    pad = true;
                }
                this.ruleSet.format(0, toInsertInto, position + this.pos, recursionCount);
                dl.decimalAt++;
            }
            return;
        }
        super.doSubstitution(number, toInsertInto, position, recursionCount);
    }

    public long transformNumber(long number) {
        return 0;
    }

    public double transformNumber(double number) {
        return number - Math.floor(number);
    }

    public Number doParse(String text, ParsePosition parsePosition, double baseValue, double upperBound, boolean lenientParse) {
        if (!this.byDigits) {
            return super.doParse(text, parsePosition, baseValue, 0.0d, lenientParse);
        }
        String workText = text;
        ParsePosition parsePosition2 = new ParsePosition(1);
        DigitList dl = new DigitList();
        while (workText.length() > 0 && parsePosition2.getIndex() != 0) {
            parsePosition2.setIndex(0);
            int digit = this.ruleSet.parse(workText, parsePosition2, 10.0d).intValue();
            if (lenientParse && parsePosition2.getIndex() == 0) {
                Number n = this.ruleSet.owner.getDecimalFormat().parse(workText, parsePosition2);
                if (n != null) {
                    digit = n.intValue();
                }
            }
            if (parsePosition2.getIndex() != 0) {
                dl.append(digit + 48);
                parsePosition.setIndex(parsePosition.getIndex() + parsePosition2.getIndex());
                workText = workText.substring(parsePosition2.getIndex());
                while (workText.length() > 0 && workText.charAt(0) == ' ') {
                    workText = workText.substring(1);
                    parsePosition.setIndex(parsePosition.getIndex() + 1);
                }
            }
        }
        return new Double(composeRuleValue(dl.count == 0 ? 0.0d : dl.getDouble(), baseValue));
    }

    public double composeRuleValue(double newRuleValue, double oldRuleValue) {
        return newRuleValue + oldRuleValue;
    }

    public double calcUpperBound(double oldUpperBound) {
        return 0.0d;
    }

    char tokenChar() {
        return '>';
    }
}
