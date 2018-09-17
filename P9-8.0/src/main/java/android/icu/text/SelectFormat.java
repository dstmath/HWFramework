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
    static final /* synthetic */ boolean -assertionsDisabled = (SelectFormat.class.desiredAssertionStatus() ^ 1);
    private static final long serialVersionUID = 2993154333257524984L;
    private transient MessagePattern msgPattern;
    private String pattern = null;

    public SelectFormat(String pattern) {
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

    static int findSubMessage(MessagePattern pattern, int partIndex, String keyword) {
        int count = pattern.countParts();
        int msgStart = 0;
        while (true) {
            int partIndex2 = partIndex + 1;
            Part part = pattern.getPart(partIndex);
            Type type = part.getType();
            if (type == Type.ARG_LIMIT) {
                partIndex = partIndex2;
                break;
            } else if (-assertionsDisabled || type == Type.ARG_SELECTOR) {
                if (!pattern.partSubstringMatches(part, keyword)) {
                    if (msgStart == 0 && pattern.partSubstringMatches(part, "other")) {
                        msgStart = partIndex2;
                    }
                    partIndex = pattern.getLimitPartIndex(partIndex2) + 1;
                    if (partIndex >= count) {
                        break;
                    }
                } else {
                    return partIndex2;
                }
            } else {
                throw new AssertionError();
            }
        }
        return msgStart;
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
            return false;
        }
        SelectFormat sf = (SelectFormat) obj;
        if (this.msgPattern != null) {
            z = this.msgPattern.equals(sf.msgPattern);
        } else if (sf.msgPattern != null) {
            z = false;
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
