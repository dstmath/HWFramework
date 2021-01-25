package ohos.com.sun.org.apache.xerces.internal.impl.dv.xs;

import java.util.AbstractList;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList;

public class ListDV extends TypeValidator {
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public Object getActualValue(String str, ValidationContext validationContext) throws InvalidDatatypeValueException {
        return str;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public short getAllowedFacets() {
        return 2079;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public int getDataLength(Object obj) {
        return ((ListData) obj).getLength();
    }

    /* access modifiers changed from: package-private */
    public static final class ListData extends AbstractList implements ObjectList {
        private String canonical;
        final Object[] data;

        public ListData(Object[] objArr) {
            this.data = objArr;
        }

        @Override // java.util.AbstractCollection, java.lang.Object
        public synchronized String toString() {
            if (this.canonical == null) {
                int length = this.data.length;
                StringBuffer stringBuffer = new StringBuffer();
                if (length > 0) {
                    stringBuffer.append(this.data[0].toString());
                }
                for (int i = 1; i < length; i++) {
                    stringBuffer.append(' ');
                    stringBuffer.append(this.data[i].toString());
                }
                this.canonical = stringBuffer.toString();
            }
            return this.canonical;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList
        public int getLength() {
            return this.data.length;
        }

        @Override // java.util.AbstractList, java.util.List, java.util.Collection, java.lang.Object
        public boolean equals(Object obj) {
            if (!(obj instanceof ListData)) {
                return false;
            }
            Object[] objArr = ((ListData) obj).data;
            int length = this.data.length;
            if (length != objArr.length) {
                return false;
            }
            for (int i = 0; i < length; i++) {
                if (!this.data[i].equals(objArr[i])) {
                    return false;
                }
            }
            return true;
        }

        @Override // java.util.AbstractList, java.util.List, java.util.Collection, java.lang.Object
        public int hashCode() {
            int i = 0;
            int i2 = 0;
            while (true) {
                Object[] objArr = this.data;
                if (i >= objArr.length) {
                    return i2;
                }
                i2 ^= objArr[i].hashCode();
                i++;
            }
        }

        @Override // java.util.AbstractCollection, java.util.List, java.util.Collection, ohos.com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList
        public boolean contains(Object obj) {
            int i = 0;
            while (true) {
                Object[] objArr = this.data;
                if (i >= objArr.length) {
                    return false;
                }
                if (obj == objArr[i]) {
                    return true;
                }
                i++;
            }
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList
        public Object item(int i) {
            if (i < 0) {
                return null;
            }
            Object[] objArr = this.data;
            if (i >= objArr.length) {
                return null;
            }
            return objArr[i];
        }

        @Override // java.util.AbstractList, java.util.List
        public Object get(int i) {
            if (i >= 0) {
                Object[] objArr = this.data;
                if (i < objArr.length) {
                    return objArr[i];
                }
            }
            throw new IndexOutOfBoundsException("Index: " + i);
        }

        @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
        public int size() {
            return getLength();
        }
    }
}
