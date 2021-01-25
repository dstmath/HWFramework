package ohos.com.sun.org.apache.xerces.internal.impl.xs.util;

import java.lang.reflect.Array;
import java.util.AbstractList;
import ohos.com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList;

public final class ObjectListImpl extends AbstractList implements ObjectList {
    public static final ObjectListImpl EMPTY_LIST = new ObjectListImpl(new Object[0], 0);
    private final Object[] fArray;
    private final int fLength;

    public ObjectListImpl(Object[] objArr, int i) {
        this.fArray = objArr;
        this.fLength = i;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList
    public int getLength() {
        return this.fLength;
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection, ohos.com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList
    public boolean contains(Object obj) {
        if (obj == null) {
            for (int i = 0; i < this.fLength; i++) {
                if (this.fArray[i] == null) {
                    return true;
                }
            }
        } else {
            for (int i2 = 0; i2 < this.fLength; i2++) {
                if (obj.equals(this.fArray[i2])) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList
    public Object item(int i) {
        if (i < 0 || i >= this.fLength) {
            return null;
        }
        return this.fArray[i];
    }

    @Override // java.util.AbstractList, java.util.List
    public Object get(int i) {
        if (i >= 0 && i < this.fLength) {
            return this.fArray[i];
        }
        throw new IndexOutOfBoundsException("Index: " + i);
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public int size() {
        return getLength();
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public Object[] toArray() {
        Object[] objArr = new Object[this.fLength];
        toArray0(objArr);
        return objArr;
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public Object[] toArray(Object[] objArr) {
        if (objArr.length < this.fLength) {
            objArr = (Object[]) Array.newInstance(objArr.getClass().getComponentType(), this.fLength);
        }
        toArray0(objArr);
        int length = objArr.length;
        int i = this.fLength;
        if (length > i) {
            objArr[i] = null;
        }
        return objArr;
    }

    private void toArray0(Object[] objArr) {
        int i = this.fLength;
        if (i > 0) {
            System.arraycopy(this.fArray, 0, objArr, 0, i);
        }
    }
}
