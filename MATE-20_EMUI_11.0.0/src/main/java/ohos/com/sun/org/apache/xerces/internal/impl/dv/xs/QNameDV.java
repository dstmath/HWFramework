package ohos.com.sun.org.apache.xerces.internal.impl.dv.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSQName;

public class QNameDV extends TypeValidator {
    private static final String EMPTY_STRING = "".intern();

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public short getAllowedFacets() {
        return 2079;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public Object getActualValue(String str, ValidationContext validationContext) throws InvalidDatatypeValueException {
        String str2;
        String str3;
        int indexOf = str.indexOf(":");
        if (indexOf > 0) {
            str3 = validationContext.getSymbol(str.substring(0, indexOf));
            str2 = str.substring(indexOf + 1);
        } else {
            str3 = EMPTY_STRING;
            str2 = str;
        }
        if (str3.length() > 0 && !XMLChar.isValidNCName(str3)) {
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{str, SchemaSymbols.ATTVAL_QNAME});
        } else if (XMLChar.isValidNCName(str2)) {
            String uri = validationContext.getURI(str3);
            if (str3.length() <= 0 || uri != null) {
                return new XQName(str3, validationContext.getSymbol(str2), validationContext.getSymbol(str), uri);
            }
            throw new InvalidDatatypeValueException("UndeclaredPrefix", new Object[]{str, str3});
        } else {
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{str, SchemaSymbols.ATTVAL_QNAME});
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public int getDataLength(Object obj) {
        return ((XQName) obj).rawname.length();
    }

    private static final class XQName extends QName implements XSQName {
        @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSQName
        public QName getXNIQName() {
            return this;
        }

        public XQName(String str, String str2, String str3, String str4) {
            setValues(str, str2, str3, str4);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.QName, java.lang.Object
        public boolean equals(Object obj) {
            if (!(obj instanceof QName)) {
                return false;
            }
            QName qName = (QName) obj;
            if (this.uri == qName.uri && this.localpart == qName.localpart) {
                return true;
            }
            return false;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.QName, java.lang.Object
        public synchronized String toString() {
            return this.rawname;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSQName
        public ohos.javax.xml.namespace.QName getJAXPQName() {
            return new ohos.javax.xml.namespace.QName(this.uri, this.localpart, this.prefix);
        }
    }
}
