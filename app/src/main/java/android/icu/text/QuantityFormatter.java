package android.icu.text;

import android.icu.impl.SimplePatternFormatter;
import android.icu.impl.StandardPlural;
import android.icu.text.PluralRules.FixedDecimal;
import java.text.FieldPosition;

class QuantityFormatter {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private final SimplePatternFormatter[] templates;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.QuantityFormatter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.QuantityFormatter.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.QuantityFormatter.<clinit>():void");
    }

    public QuantityFormatter() {
        this.templates = new SimplePatternFormatter[StandardPlural.COUNT];
    }

    public void addIfAbsent(CharSequence variant, String template) {
        int idx = StandardPlural.indexFromString(variant);
        if (this.templates[idx] == null) {
            this.templates[idx] = SimplePatternFormatter.compileMinMaxPlaceholders(template, 0, 1);
        }
    }

    public boolean isValid() {
        return this.templates[StandardPlural.OTHER_INDEX] != null;
    }

    public String format(double number, NumberFormat numberFormat, PluralRules pluralRules) {
        String formatStr = numberFormat.format(number);
        SimplePatternFormatter formatter = this.templates[selectPlural(number, numberFormat, pluralRules).ordinal()];
        if (formatter == null) {
            formatter = this.templates[StandardPlural.OTHER_INDEX];
            if (!-assertionsDisabled) {
                if ((formatter != null ? 1 : 0) == 0) {
                    throw new AssertionError();
                }
            }
        }
        return formatter.format(formatStr);
    }

    public SimplePatternFormatter getByVariant(CharSequence variant) {
        if (-assertionsDisabled || isValid()) {
            int idx = StandardPlural.indexOrOtherIndexFromString(variant);
            SimplePatternFormatter template = this.templates[idx];
            if (template != null || idx == StandardPlural.OTHER_INDEX) {
                return template;
            }
            return this.templates[StandardPlural.OTHER_INDEX];
        }
        throw new AssertionError();
    }

    public static StandardPlural selectPlural(double number, NumberFormat numberFormat, PluralRules rules) {
        String pluralKeyword;
        if (numberFormat instanceof DecimalFormat) {
            pluralKeyword = rules.select(((DecimalFormat) numberFormat).getFixedDecimal(number));
        } else {
            pluralKeyword = rules.select(number);
        }
        return StandardPlural.orOtherFromString(pluralKeyword);
    }

    public static StandardPlural selectPlural(Number number, NumberFormat fmt, PluralRules rules, StringBuffer formattedNumber, FieldPosition pos) {
        FieldPosition fpos = new UFieldPosition(pos.getFieldAttribute(), pos.getField());
        fmt.format((Object) number, formattedNumber, fpos);
        String pluralKeyword = rules.select(new FixedDecimal(number.doubleValue(), fpos.getCountVisibleFractionDigits(), fpos.getFractionDigits()));
        pos.setBeginIndex(fpos.getBeginIndex());
        pos.setEndIndex(fpos.getEndIndex());
        return StandardPlural.orOtherFromString(pluralKeyword);
    }

    public static StringBuilder format(String compiledPattern, CharSequence value, StringBuilder appendTo, FieldPosition pos) {
        int[] offsets = new int[1];
        SimplePatternFormatter.formatAndAppend(compiledPattern, appendTo, offsets, value);
        if (!(pos.getBeginIndex() == 0 && pos.getEndIndex() == 0)) {
            if (offsets[0] >= 0) {
                pos.setBeginIndex(pos.getBeginIndex() + offsets[0]);
                pos.setEndIndex(pos.getEndIndex() + offsets[0]);
            } else {
                pos.setBeginIndex(0);
                pos.setEndIndex(0);
            }
        }
        return appendTo;
    }
}
