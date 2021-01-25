package ohos.global.icu.text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import ohos.global.icu.impl.PatternProps;
import ohos.global.icu.text.MessagePattern;

public class SelectFormat extends Format {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final long serialVersionUID = 2993154333257524984L;
    private transient MessagePattern msgPattern;
    private String pattern = null;

    public SelectFormat(String str) {
        applyPattern(str);
    }

    private void reset() {
        this.pattern = null;
        MessagePattern messagePattern = this.msgPattern;
        if (messagePattern != null) {
            messagePattern.clear();
        }
    }

    public void applyPattern(String str) {
        this.pattern = str;
        if (this.msgPattern == null) {
            this.msgPattern = new MessagePattern();
        }
        try {
            this.msgPattern.parseSelectStyle(str);
        } catch (RuntimeException e) {
            reset();
            throw e;
        }
    }

    public String toPattern() {
        return this.pattern;
    }

    static int findSubMessage(MessagePattern messagePattern, int i, String str) {
        int countParts = messagePattern.countParts();
        int i2 = 0;
        do {
            int i3 = i + 1;
            MessagePattern.Part part = messagePattern.getPart(i);
            if (part.getType() == MessagePattern.Part.Type.ARG_LIMIT) {
                break;
            } else if (messagePattern.partSubstringMatches(part, str)) {
                return i3;
            } else {
                if (i2 == 0 && messagePattern.partSubstringMatches(part, "other")) {
                    i2 = i3;
                }
                i = messagePattern.getLimitPartIndex(i3) + 1;
            }
        } while (i < countParts);
        return i2;
    }

    public final String format(String str) {
        int index;
        if (PatternProps.isIdentifier(str)) {
            MessagePattern messagePattern = this.msgPattern;
            if (messagePattern == null || messagePattern.countParts() == 0) {
                throw new IllegalStateException("Invalid format error.");
            }
            int findSubMessage = findSubMessage(this.msgPattern, 0, str);
            if (!this.msgPattern.jdkAposMode()) {
                return this.msgPattern.getPatternString().substring(this.msgPattern.getPart(findSubMessage).getLimit(), this.msgPattern.getPatternIndex(this.msgPattern.getLimitPartIndex(findSubMessage)));
            }
            StringBuilder sb = null;
            int limit = this.msgPattern.getPart(findSubMessage).getLimit();
            while (true) {
                findSubMessage++;
                MessagePattern.Part part = this.msgPattern.getPart(findSubMessage);
                MessagePattern.Part.Type type = part.getType();
                index = part.getIndex();
                if (type == MessagePattern.Part.Type.MSG_LIMIT) {
                    break;
                } else if (type == MessagePattern.Part.Type.SKIP_SYNTAX) {
                    if (sb == null) {
                        sb = new StringBuilder();
                    }
                    sb.append((CharSequence) this.pattern, limit, index);
                    limit = part.getLimit();
                } else if (type == MessagePattern.Part.Type.ARG_START) {
                    if (sb == null) {
                        sb = new StringBuilder();
                    }
                    sb.append((CharSequence) this.pattern, limit, index);
                    findSubMessage = this.msgPattern.getLimitPartIndex(findSubMessage);
                    limit = this.msgPattern.getPart(findSubMessage).getLimit();
                    MessagePattern.appendReducedApostrophes(this.pattern, index, limit, sb);
                }
            }
            if (sb == null) {
                return this.pattern.substring(limit, index);
            }
            sb.append((CharSequence) this.pattern, limit, index);
            return sb.toString();
        }
        throw new IllegalArgumentException("Invalid formatting argument.");
    }

    @Override // java.text.Format
    public StringBuffer format(Object obj, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        if (obj instanceof String) {
            stringBuffer.append(format((String) obj));
            return stringBuffer;
        }
        throw new IllegalArgumentException("'" + obj + "' is not a String");
    }

    @Override // java.text.Format
    public Object parseObject(String str, ParsePosition parsePosition) {
        throw new UnsupportedOperationException();
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SelectFormat selectFormat = (SelectFormat) obj;
        MessagePattern messagePattern = this.msgPattern;
        if (messagePattern == null) {
            return selectFormat.msgPattern == null;
        }
        return messagePattern.equals(selectFormat.msgPattern);
    }

    @Override // java.lang.Object
    public int hashCode() {
        String str = this.pattern;
        if (str != null) {
            return str.hashCode();
        }
        return 0;
    }

    @Override // java.lang.Object
    public String toString() {
        return "pattern='" + this.pattern + "'";
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        String str = this.pattern;
        if (str != null) {
            applyPattern(str);
        }
    }
}
