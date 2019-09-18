package java.text;

import java.io.Serializable;
import java.text.AttributedCharacterIterator;

public abstract class Format implements Serializable, Cloneable {
    private static final long serialVersionUID = -299282585814624189L;

    public static class Field extends AttributedCharacterIterator.Attribute {
        private static final long serialVersionUID = 276966692217360283L;

        protected Field(String name) {
            super(name);
        }
    }

    interface FieldDelegate {
        void formatted(int i, Field field, Object obj, int i2, int i3, StringBuffer stringBuffer);

        void formatted(Field field, Object obj, int i, int i2, StringBuffer stringBuffer);
    }

    public abstract StringBuffer format(Object obj, StringBuffer stringBuffer, FieldPosition fieldPosition);

    public abstract Object parseObject(String str, ParsePosition parsePosition);

    protected Format() {
    }

    public final String format(Object obj) {
        return format(obj, new StringBuffer(), new FieldPosition(0)).toString();
    }

    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        return createAttributedCharacterIterator(format(obj));
    }

    public Object parseObject(String source) throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Object result = parseObject(source, pos);
        if (pos.index != 0) {
            return result;
        }
        throw new ParseException("Format.parseObject(String) failed", pos.errorIndex);
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError((Throwable) e);
        }
    }

    /* access modifiers changed from: package-private */
    public AttributedCharacterIterator createAttributedCharacterIterator(String s) {
        return new AttributedString(s).getIterator();
    }

    /* access modifiers changed from: package-private */
    public AttributedCharacterIterator createAttributedCharacterIterator(AttributedCharacterIterator[] iterators) {
        return new AttributedString(iterators).getIterator();
    }

    /* access modifiers changed from: package-private */
    public AttributedCharacterIterator createAttributedCharacterIterator(String string, AttributedCharacterIterator.Attribute key, Object value) {
        AttributedString as = new AttributedString(string);
        as.addAttribute(key, value);
        return as.getIterator();
    }

    /* access modifiers changed from: package-private */
    public AttributedCharacterIterator createAttributedCharacterIterator(AttributedCharacterIterator iterator, AttributedCharacterIterator.Attribute key, Object value) {
        AttributedString as = new AttributedString(iterator);
        as.addAttribute(key, value);
        return as.getIterator();
    }
}
