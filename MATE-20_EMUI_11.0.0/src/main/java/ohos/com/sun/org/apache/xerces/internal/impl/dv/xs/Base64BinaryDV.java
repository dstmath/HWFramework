package ohos.com.sun.org.apache.xerces.internal.impl.dv.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.util.ByteListImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;

public class Base64BinaryDV extends TypeValidator {
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public short getAllowedFacets() {
        return 2079;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public Object getActualValue(String str, ValidationContext validationContext) throws InvalidDatatypeValueException {
        byte[] decode = Base64.decode(str);
        if (decode != null) {
            return new XBase64(decode);
        }
        throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{str, SchemaSymbols.ATTVAL_BASE64BINARY});
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public int getDataLength(Object obj) {
        return ((XBase64) obj).getLength();
    }

    private static final class XBase64 extends ByteListImpl {
        public XBase64(byte[] bArr) {
            super(bArr);
        }

        @Override // java.util.AbstractCollection, java.lang.Object
        public synchronized String toString() {
            if (this.canonical == null) {
                this.canonical = Base64.encode(this.data);
            }
            return this.canonical;
        }

        @Override // java.util.AbstractList, java.util.List, java.util.Collection, java.lang.Object
        public boolean equals(Object obj) {
            if (!(obj instanceof XBase64)) {
                return false;
            }
            byte[] bArr = ((XBase64) obj).data;
            int length = this.data.length;
            if (length != bArr.length) {
                return false;
            }
            for (int i = 0; i < length; i++) {
                if (this.data[i] != bArr[i]) {
                    return false;
                }
            }
            return true;
        }

        @Override // java.util.AbstractList, java.util.List, java.util.Collection, java.lang.Object
        public int hashCode() {
            int i = 0;
            for (int i2 = 0; i2 < this.data.length; i2++) {
                i = (i * 37) + (this.data[i2] & 255);
            }
            return i;
        }
    }
}
