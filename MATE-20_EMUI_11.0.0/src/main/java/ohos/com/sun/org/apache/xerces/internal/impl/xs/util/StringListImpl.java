package ohos.com.sun.org.apache.xerces.internal.impl.xs.util;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.xs.StringList;

public final class StringListImpl extends AbstractList implements StringList {
    public static final StringListImpl EMPTY_LIST = new StringListImpl(new String[0], 0);
    private final String[] fArray;
    private final int fLength;
    private final Vector fVector;

    public StringListImpl(Vector vector) {
        int i;
        this.fVector = vector;
        if (vector == null) {
            i = 0;
        } else {
            i = vector.size();
        }
        this.fLength = i;
        this.fArray = null;
    }

    public StringListImpl(String[] strArr, int i) {
        this.fArray = strArr;
        this.fLength = i;
        this.fVector = null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.StringList
    public int getLength() {
        return this.fLength;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.StringList
    public boolean contains(String str) {
        Vector vector = this.fVector;
        if (vector != null) {
            return vector.contains(str);
        }
        if (str == null) {
            for (int i = 0; i < this.fLength; i++) {
                if (this.fArray[i] == null) {
                    return true;
                }
            }
        } else {
            for (int i2 = 0; i2 < this.fLength; i2++) {
                if (str.equals(this.fArray[i2])) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.StringList
    public String item(int i) {
        if (i < 0 || i >= this.fLength) {
            return null;
        }
        Vector vector = this.fVector;
        if (vector != null) {
            return (String) vector.elementAt(i);
        }
        return this.fArray[i];
    }

    @Override // java.util.AbstractList, java.util.List
    public Object get(int i) {
        if (i < 0 || i >= this.fLength) {
            throw new IndexOutOfBoundsException("Index: " + i);
        }
        Vector vector = this.fVector;
        if (vector != null) {
            return vector.elementAt(i);
        }
        return this.fArray[i];
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public int size() {
        return getLength();
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public Object[] toArray() {
        Vector vector = this.fVector;
        if (vector != null) {
            return vector.toArray();
        }
        Object[] objArr = new Object[this.fLength];
        toArray0(objArr);
        return objArr;
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public Object[] toArray(Object[] objArr) {
        Vector vector = this.fVector;
        if (vector != null) {
            return vector.toArray(objArr);
        }
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
