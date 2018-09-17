package android.icu.text;

import java.text.ParsePosition;

/* compiled from: NFSubstitution */
class NumeratorSubstitution extends NFSubstitution {
    private final double denominator;
    private final boolean withZeros;

    NumeratorSubstitution(int pos, double denominator, NFRuleSet ruleSet, String description) {
        super(pos, ruleSet, fixdesc(description));
        this.denominator = denominator;
        this.withZeros = description.endsWith("<<");
    }

    static String fixdesc(String description) {
        if (description.endsWith("<<")) {
            return description.substring(0, description.length() - 1);
        }
        return description;
    }

    public boolean equals(Object that) {
        boolean z = false;
        if (!super.equals(that)) {
            return false;
        }
        NumeratorSubstitution that2 = (NumeratorSubstitution) that;
        if (this.denominator == that2.denominator && this.withZeros == that2.withZeros) {
            z = true;
        }
        return z;
    }

    public void doSubstitution(double number, StringBuffer toInsertInto, int position, int recursionCount) {
        double numberToFormat = transformNumber(number);
        if (this.withZeros && this.ruleSet != null) {
            long nf = (long) numberToFormat;
            int len = toInsertInto.length();
            while (true) {
                nf *= 10;
                if (((double) nf) >= this.denominator) {
                    break;
                }
                toInsertInto.insert(this.pos + position, ' ');
                this.ruleSet.format(0, toInsertInto, position + this.pos, recursionCount);
            }
            position += toInsertInto.length() - len;
        }
        if (numberToFormat == Math.floor(numberToFormat) && this.ruleSet != null) {
            this.ruleSet.format((long) numberToFormat, toInsertInto, position + this.pos, recursionCount);
        } else if (this.ruleSet != null) {
            this.ruleSet.format(numberToFormat, toInsertInto, position + this.pos, recursionCount);
        } else {
            toInsertInto.insert(this.pos + position, this.numberFormat.format(numberToFormat));
        }
    }

    public long transformNumber(long number) {
        return Math.round(((double) number) * this.denominator);
    }

    public double transformNumber(double number) {
        return (double) Math.round(this.denominator * number);
    }

    public Number doParse(String text, ParsePosition parsePosition, double baseValue, double upperBound, boolean lenientParse) {
        double d;
        int zeroCount = 0;
        if (this.withZeros) {
            String workText = text;
            ParsePosition workPos = new ParsePosition(1);
            while (workText.length() > 0 && workPos.getIndex() != 0) {
                workPos.setIndex(0);
                this.ruleSet.parse(workText, workPos, 1.0d).intValue();
                if (workPos.getIndex() == 0) {
                    break;
                }
                zeroCount++;
                parsePosition.setIndex(parsePosition.getIndex() + workPos.getIndex());
                workText = workText.substring(workPos.getIndex());
                while (workText.length() > 0 && workText.charAt(0) == ' ') {
                    workText = workText.substring(1);
                    parsePosition.setIndex(parsePosition.getIndex() + 1);
                }
            }
            text = text.substring(parsePosition.getIndex());
            parsePosition.setIndex(0);
        }
        if (this.withZeros) {
            d = 1.0d;
        } else {
            d = baseValue;
        }
        Number result = super.doParse(text, parsePosition, d, upperBound, false);
        if (!this.withZeros) {
            return result;
        }
        long n = result.longValue();
        long d2 = 1;
        while (d2 <= n) {
            d2 *= 10;
        }
        for (zeroCount = 
        /* Method generation error in method: android.icu.text.NumeratorSubstitution.doParse(java.lang.String, java.text.ParsePosition, double, double, boolean):java.lang.Number
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r17_3 'zeroCount' int) = (r17_0 'zeroCount' int), (r17_1 'zeroCount' int) binds: {(r17_0 'zeroCount' int)=B:1:0x0006, (r17_1 'zeroCount' int)=B:9:0x0035} in method: android.icu.text.NumeratorSubstitution.doParse(java.lang.String, java.text.ParsePosition, double, double, boolean):java.lang.Number
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:225)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:184)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:177)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:324)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:116)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:81)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:19)
	at jadx.core.ProcessClass.process(ProcessClass.java:43)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.CodegenException: Unknown instruction: PHI in method: android.icu.text.NumeratorSubstitution.doParse(java.lang.String, java.text.ParsePosition, double, double, boolean):java.lang.Number
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:512)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:219)
	... 19 more
 */

        public double composeRuleValue(double newRuleValue, double oldRuleValue) {
            return newRuleValue / oldRuleValue;
        }

        public double calcUpperBound(double oldUpperBound) {
            return this.denominator;
        }

        char tokenChar() {
            return '<';
        }
    }
