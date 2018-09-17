package android.icu.text;

import android.icu.text.UnicodeSet.SpanCondition;
import android.icu.util.OutputInt;
import org.xmlpull.v1.XmlPullParser;

public class UnicodeSetSpanner {
    private final UnicodeSet unicodeSet;

    public enum CountMethod {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.UnicodeSetSpanner.CountMethod.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.UnicodeSetSpanner.CountMethod.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.UnicodeSetSpanner.CountMethod.<clinit>():void");
        }
    }

    public enum TrimOption {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.UnicodeSetSpanner.TrimOption.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.UnicodeSetSpanner.TrimOption.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.UnicodeSetSpanner.TrimOption.<clinit>():void");
        }
    }

    public UnicodeSetSpanner(UnicodeSet source) {
        this.unicodeSet = source;
    }

    public UnicodeSet getUnicodeSet() {
        return this.unicodeSet;
    }

    public boolean equals(Object other) {
        return other instanceof UnicodeSetSpanner ? this.unicodeSet.equals(((UnicodeSetSpanner) other).unicodeSet) : false;
    }

    public int hashCode() {
        return this.unicodeSet.hashCode();
    }

    public int countIn(CharSequence sequence) {
        return countIn(sequence, CountMethod.MIN_ELEMENTS, SpanCondition.SIMPLE);
    }

    public int countIn(CharSequence sequence, CountMethod countMethod) {
        return countIn(sequence, countMethod, SpanCondition.SIMPLE);
    }

    public int countIn(CharSequence sequence, CountMethod countMethod, SpanCondition spanCondition) {
        SpanCondition skipSpan;
        int count = 0;
        int start = 0;
        if (spanCondition == SpanCondition.NOT_CONTAINED) {
            skipSpan = SpanCondition.SIMPLE;
        } else {
            skipSpan = SpanCondition.NOT_CONTAINED;
        }
        int length = sequence.length();
        OutputInt outputInt = null;
        while (start != length) {
            int endOfSpan = this.unicodeSet.span(sequence, start, skipSpan);
            if (endOfSpan == length) {
                break;
            } else if (countMethod == CountMethod.WHOLE_SPAN) {
                start = this.unicodeSet.span(sequence, endOfSpan, spanCondition);
                count++;
            } else {
                if (outputInt == null) {
                    outputInt = new OutputInt();
                }
                start = this.unicodeSet.spanAndCount(sequence, endOfSpan, spanCondition, outputInt);
                count += outputInt.value;
            }
        }
        return count;
    }

    public String deleteFrom(CharSequence sequence) {
        return replaceFrom(sequence, XmlPullParser.NO_NAMESPACE, CountMethod.WHOLE_SPAN, SpanCondition.SIMPLE);
    }

    public String deleteFrom(CharSequence sequence, SpanCondition spanCondition) {
        return replaceFrom(sequence, XmlPullParser.NO_NAMESPACE, CountMethod.WHOLE_SPAN, spanCondition);
    }

    public String replaceFrom(CharSequence sequence, CharSequence replacement) {
        return replaceFrom(sequence, replacement, CountMethod.MIN_ELEMENTS, SpanCondition.SIMPLE);
    }

    public String replaceFrom(CharSequence sequence, CharSequence replacement, CountMethod countMethod) {
        return replaceFrom(sequence, replacement, countMethod, SpanCondition.SIMPLE);
    }

    public String replaceFrom(CharSequence sequence, CharSequence replacement, CountMethod countMethod, SpanCondition spanCondition) {
        SpanCondition copySpan;
        if (spanCondition == SpanCondition.NOT_CONTAINED) {
            copySpan = SpanCondition.SIMPLE;
        } else {
            copySpan = SpanCondition.NOT_CONTAINED;
        }
        boolean remove = replacement.length() == 0;
        StringBuilder result = new StringBuilder();
        int length = sequence.length();
        OutputInt spanCount = null;
        int endCopy = 0;
        while (endCopy != length) {
            int endModify;
            if (countMethod == CountMethod.WHOLE_SPAN) {
                endModify = this.unicodeSet.span(sequence, endCopy, spanCondition);
            } else {
                if (spanCount == null) {
                    spanCount = new OutputInt();
                }
                endModify = this.unicodeSet.spanAndCount(sequence, endCopy, spanCondition, spanCount);
            }
            if (!(remove || endModify == 0)) {
                if (countMethod == CountMethod.WHOLE_SPAN) {
                    result.append(replacement);
                } else {
                    for (int i = spanCount.value; i > 0; i--) {
                        result.append(replacement);
                    }
                }
            }
            if (endModify == length) {
                break;
            }
            endCopy = this.unicodeSet.span(sequence, endModify, copySpan);
            result.append(sequence.subSequence(endModify, endCopy));
        }
        return result.toString();
    }

    public CharSequence trim(CharSequence sequence) {
        return trim(sequence, TrimOption.BOTH, SpanCondition.SIMPLE);
    }

    public CharSequence trim(CharSequence sequence, TrimOption trimOption) {
        return trim(sequence, trimOption, SpanCondition.SIMPLE);
    }

    public CharSequence trim(CharSequence sequence, TrimOption trimOption, SpanCondition spanCondition) {
        int endLeadContained;
        int startTrailContained;
        int length = sequence.length();
        if (trimOption != TrimOption.TRAILING) {
            endLeadContained = this.unicodeSet.span(sequence, spanCondition);
            if (endLeadContained == length) {
                return XmlPullParser.NO_NAMESPACE;
            }
        }
        endLeadContained = 0;
        if (trimOption != TrimOption.LEADING) {
            startTrailContained = this.unicodeSet.spanBack(sequence, spanCondition);
        } else {
            startTrailContained = length;
        }
        if (!(endLeadContained == 0 && startTrailContained == length)) {
            sequence = sequence.subSequence(endLeadContained, startTrailContained);
        }
        return sequence;
    }
}
