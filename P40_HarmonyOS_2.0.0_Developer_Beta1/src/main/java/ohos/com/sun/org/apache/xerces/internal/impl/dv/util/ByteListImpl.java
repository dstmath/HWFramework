package ohos.com.sun.org.apache.xerces.internal.impl.dv.util;

import java.util.AbstractList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSException;
import ohos.com.sun.org.apache.xerces.internal.xs.datatypes.ByteList;

public class ByteListImpl extends AbstractList implements ByteList {
    protected String canonical;
    protected final byte[] data;

    public ByteListImpl(byte[] bArr) {
        this.data = bArr;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.ByteList
    public int getLength() {
        return this.data.length;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.ByteList
    public boolean contains(byte b) {
        int i = 0;
        while (true) {
            byte[] bArr = this.data;
            if (i >= bArr.length) {
                return false;
            }
            if (bArr[i] == b) {
                return true;
            }
            i++;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.ByteList
    public byte item(int i) throws XSException {
        if (i >= 0) {
            byte[] bArr = this.data;
            if (i <= bArr.length - 1) {
                return bArr[i];
            }
        }
        throw new XSException(2, null);
    }

    @Override // java.util.AbstractList, java.util.List
    public Object get(int i) {
        if (i >= 0) {
            byte[] bArr = this.data;
            if (i < bArr.length) {
                return new Byte(bArr[i]);
            }
        }
        throw new IndexOutOfBoundsException("Index: " + i);
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public int size() {
        return getLength();
    }
}
