package android.icu.text;

import android.icu.impl.PatternProps;
import android.icu.text.MessagePattern.Part;
import android.icu.text.MessagePattern.Part.Type;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

public class SelectFormat extends Format {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final long serialVersionUID = 2993154333257524984L;
    private transient MessagePattern msgPattern;
    private String pattern;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.SelectFormat.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.SelectFormat.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.SelectFormat.<clinit>():void");
    }

    public SelectFormat(String pattern) {
        this.pattern = null;
        applyPattern(pattern);
    }

    private void reset() {
        this.pattern = null;
        if (this.msgPattern != null) {
            this.msgPattern.clear();
        }
    }

    public void applyPattern(String pattern) {
        this.pattern = pattern;
        if (this.msgPattern == null) {
            this.msgPattern = new MessagePattern();
        }
        try {
            this.msgPattern.parseSelectStyle(pattern);
        } catch (RuntimeException e) {
            reset();
            throw e;
        }
    }

    public String toPattern() {
        return this.pattern;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static int findSubMessage(MessagePattern pattern, int partIndex, String keyword) {
        int count = pattern.countParts();
        int msgStart = 0;
        while (true) {
            int partIndex2 = partIndex + 1;
            Part part = pattern.getPart(partIndex);
            Type type = part.getType();
            if (type != Type.ARG_LIMIT) {
                if (!-assertionsDisabled) {
                    if ((type == Type.ARG_SELECTOR ? 1 : null) == null) {
                        break;
                    }
                }
                if (!pattern.partSubstringMatches(part, keyword)) {
                    if (msgStart == 0 && pattern.partSubstringMatches(part, PluralRules.KEYWORD_OTHER)) {
                        msgStart = partIndex2;
                    }
                    partIndex = pattern.getLimitPartIndex(partIndex2) + 1;
                    if (partIndex >= count) {
                        break;
                    }
                } else {
                    return partIndex2;
                }
            }
            break;
        }
        throw new AssertionError();
    }

    public final String format(String keyword) {
        if (!PatternProps.isIdentifier(keyword)) {
            throw new IllegalArgumentException("Invalid formatting argument.");
        } else if (this.msgPattern == null || this.msgPattern.countParts() == 0) {
            throw new IllegalStateException("Invalid format error.");
        } else {
            int msgStart = findSubMessage(this.msgPattern, 0, keyword);
            if (this.msgPattern.jdkAposMode()) {
                int index;
                StringBuilder result = null;
                int prevIndex = this.msgPattern.getPart(msgStart).getLimit();
                int i = msgStart;
                while (true) {
                    i++;
                    Part part = this.msgPattern.getPart(i);
                    Type type = part.getType();
                    index = part.getIndex();
                    if (type == Type.MSG_LIMIT) {
                        break;
                    } else if (type == Type.SKIP_SYNTAX) {
                        if (result == null) {
                            result = new StringBuilder();
                        }
                        result.append(this.pattern, prevIndex, index);
                        prevIndex = part.getLimit();
                    } else if (type == Type.ARG_START) {
                        if (result == null) {
                            result = new StringBuilder();
                        }
                        result.append(this.pattern, prevIndex, index);
                        prevIndex = index;
                        i = this.msgPattern.getLimitPartIndex(i);
                        index = this.msgPattern.getPart(i).getLimit();
                        MessagePattern.appendReducedApostrophes(this.pattern, prevIndex, index, result);
                        prevIndex = index;
                    }
                }
                if (result == null) {
                    return this.pattern.substring(prevIndex, index);
                }
                return result.append(this.pattern, prevIndex, index).toString();
            }
            return this.msgPattern.getPatternString().substring(this.msgPattern.getPart(msgStart).getLimit(), this.msgPattern.getPatternIndex(this.msgPattern.getLimitPartIndex(msgStart)));
        }
    }

    public StringBuffer format(Object keyword, StringBuffer toAppendTo, FieldPosition pos) {
        if (keyword instanceof String) {
            toAppendTo.append(format((String) keyword));
            return toAppendTo;
        }
        throw new IllegalArgumentException("'" + keyword + "' is not a String");
    }

    public Object parseObject(String source, ParsePosition pos) {
        throw new UnsupportedOperationException();
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return -assertionsDisabled;
        }
        SelectFormat sf = (SelectFormat) obj;
        if (this.msgPattern != null) {
            z = this.msgPattern.equals(sf.msgPattern);
        } else if (sf.msgPattern != null) {
            z = -assertionsDisabled;
        }
        return z;
    }

    public int hashCode() {
        if (this.pattern != null) {
            return this.pattern.hashCode();
        }
        return 0;
    }

    public String toString() {
        return "pattern='" + this.pattern + "'";
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (this.pattern != null) {
            applyPattern(this.pattern);
        }
    }
}
