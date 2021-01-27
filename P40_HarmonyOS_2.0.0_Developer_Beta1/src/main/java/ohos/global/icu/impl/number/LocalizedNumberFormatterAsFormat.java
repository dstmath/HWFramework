package ohos.global.icu.impl.number;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.text.AttributedCharacterIterator;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import ohos.global.icu.number.FormattedNumber;
import ohos.global.icu.number.LocalizedNumberFormatter;
import ohos.global.icu.number.NumberFormatter;
import ohos.global.icu.util.ULocale;

public class LocalizedNumberFormatterAsFormat extends Format {
    private static final long serialVersionUID = 1;
    private final transient LocalizedNumberFormatter formatter;
    private final transient ULocale locale;

    public LocalizedNumberFormatterAsFormat(LocalizedNumberFormatter localizedNumberFormatter, ULocale uLocale) {
        this.formatter = localizedNumberFormatter;
        this.locale = uLocale;
    }

    @Override // java.text.Format
    public StringBuffer format(Object obj, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        if (obj instanceof Number) {
            FormattedNumber format = this.formatter.format((Number) obj);
            fieldPosition.setBeginIndex(0);
            fieldPosition.setEndIndex(0);
            if (format.nextFieldPosition(fieldPosition) && stringBuffer.length() != 0) {
                fieldPosition.setBeginIndex(fieldPosition.getBeginIndex() + stringBuffer.length());
                fieldPosition.setEndIndex(fieldPosition.getEndIndex() + stringBuffer.length());
            }
            format.appendTo(stringBuffer);
            return stringBuffer;
        }
        throw new IllegalArgumentException();
    }

    @Override // java.text.Format
    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        if (obj instanceof Number) {
            return this.formatter.format((Number) obj).toCharacterIterator();
        }
        throw new IllegalArgumentException();
    }

    @Override // java.text.Format
    public Object parseObject(String str, ParsePosition parsePosition) {
        throw new UnsupportedOperationException();
    }

    public LocalizedNumberFormatter getNumberFormatter() {
        return this.formatter;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return this.formatter.hashCode();
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && (obj instanceof LocalizedNumberFormatterAsFormat)) {
            return this.formatter.equals(((LocalizedNumberFormatterAsFormat) obj).getNumberFormatter());
        }
        return false;
    }

    private Object writeReplace() throws ObjectStreamException {
        Proxy proxy = new Proxy();
        proxy.languageTag = this.locale.toLanguageTag();
        proxy.skeleton = this.formatter.toSkeleton();
        return proxy;
    }

    static class Proxy implements Externalizable {
        private static final long serialVersionUID = 1;
        String languageTag;
        String skeleton;

        @Override // java.io.Externalizable
        public void writeExternal(ObjectOutput objectOutput) throws IOException {
            objectOutput.writeByte(0);
            objectOutput.writeUTF(this.languageTag);
            objectOutput.writeUTF(this.skeleton);
        }

        @Override // java.io.Externalizable
        public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
            objectInput.readByte();
            this.languageTag = objectInput.readUTF();
            this.skeleton = objectInput.readUTF();
        }

        private Object readResolve() throws ObjectStreamException {
            return NumberFormatter.forSkeleton(this.skeleton).locale(ULocale.forLanguageTag(this.languageTag)).toFormat();
        }
    }
}
