package ohos.com.sun.org.apache.xerces.internal.impl.xs.util;

import java.util.AbstractList;
import ohos.com.sun.org.apache.xerces.internal.xs.ShortList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSException;

public final class ShortListImpl extends AbstractList implements ShortList {
    public static final ShortListImpl EMPTY_LIST = new ShortListImpl(new short[0], 0);
    private final short[] fArray;
    private final int fLength;

    public ShortListImpl(short[] sArr, int i) {
        this.fArray = sArr;
        this.fLength = i;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.ShortList
    public int getLength() {
        return this.fLength;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.ShortList
    public boolean contains(short s) {
        for (int i = 0; i < this.fLength; i++) {
            if (this.fArray[i] == s) {
                return true;
            }
        }
        return false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.ShortList
    public short item(int i) throws XSException {
        if (i >= 0 && i < this.fLength) {
            return this.fArray[i];
        }
        throw new XSException(2, null);
    }

    @Override // java.util.AbstractList, java.util.List, java.util.Collection, java.lang.Object
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ShortList)) {
            return false;
        }
        ShortList shortList = (ShortList) obj;
        if (this.fLength != shortList.getLength()) {
            return false;
        }
        for (int i = 0; i < this.fLength; i++) {
            if (this.fArray[i] != shortList.item(i)) {
                return false;
            }
        }
        return true;
    }

    @Override // java.util.AbstractList, java.util.List
    public Object get(int i) {
        if (i >= 0 && i < this.fLength) {
            return new Short(this.fArray[i]);
        }
        throw new IndexOutOfBoundsException("Index: " + i);
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public int size() {
        return getLength();
    }
}
