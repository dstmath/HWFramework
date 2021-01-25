package ohos.global.icu.text;

import ohos.global.icu.impl.SimpleFormatterImpl;

public final class SimpleFormatter {
    private final String compiledPattern;

    private SimpleFormatter(String str) {
        this.compiledPattern = str;
    }

    public static SimpleFormatter compile(CharSequence charSequence) {
        return compileMinMaxArguments(charSequence, 0, Integer.MAX_VALUE);
    }

    public static SimpleFormatter compileMinMaxArguments(CharSequence charSequence, int i, int i2) {
        return new SimpleFormatter(SimpleFormatterImpl.compileToStringMinMaxArguments(charSequence, new StringBuilder(), i, i2));
    }

    public int getArgumentLimit() {
        return SimpleFormatterImpl.getArgumentLimit(this.compiledPattern);
    }

    public String format(CharSequence... charSequenceArr) {
        return SimpleFormatterImpl.formatCompiledPattern(this.compiledPattern, charSequenceArr);
    }

    public StringBuilder formatAndAppend(StringBuilder sb, int[] iArr, CharSequence... charSequenceArr) {
        return SimpleFormatterImpl.formatAndAppend(this.compiledPattern, sb, iArr, charSequenceArr);
    }

    public StringBuilder formatAndReplace(StringBuilder sb, int[] iArr, CharSequence... charSequenceArr) {
        return SimpleFormatterImpl.formatAndReplace(this.compiledPattern, sb, iArr, charSequenceArr);
    }

    public String toString() {
        String[] strArr = new String[getArgumentLimit()];
        for (int i = 0; i < strArr.length; i++) {
            strArr[i] = "{" + i + '}';
        }
        return formatAndAppend(new StringBuilder(), null, strArr).toString();
    }

    public String getTextWithNoArguments() {
        return SimpleFormatterImpl.getTextWithNoArguments(this.compiledPattern);
    }
}
