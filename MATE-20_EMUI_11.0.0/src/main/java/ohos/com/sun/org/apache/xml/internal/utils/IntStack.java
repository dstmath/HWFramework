package ohos.com.sun.org.apache.xml.internal.utils;

import java.util.EmptyStackException;

public class IntStack extends IntVector {
    public IntStack() {
    }

    public IntStack(int i) {
        super(i);
    }

    public IntStack(IntStack intStack) {
        super(intStack);
    }

    public int push(int i) {
        if (this.m_firstFree + 1 >= this.m_mapSize) {
            this.m_mapSize += this.m_blocksize;
            int[] iArr = new int[this.m_mapSize];
            System.arraycopy(this.m_map, 0, iArr, 0, this.m_firstFree + 1);
            this.m_map = iArr;
        }
        this.m_map[this.m_firstFree] = i;
        this.m_firstFree++;
        return i;
    }

    public final int pop() {
        int[] iArr = this.m_map;
        int i = this.m_firstFree - 1;
        this.m_firstFree = i;
        return iArr[i];
    }

    public final void quickPop(int i) {
        this.m_firstFree -= i;
    }

    public final int peek() {
        try {
            return this.m_map[this.m_firstFree - 1];
        } catch (ArrayIndexOutOfBoundsException unused) {
            throw new EmptyStackException();
        }
    }

    public int peek(int i) {
        try {
            return this.m_map[this.m_firstFree - (i + 1)];
        } catch (ArrayIndexOutOfBoundsException unused) {
            throw new EmptyStackException();
        }
    }

    public void setTop(int i) {
        try {
            this.m_map[this.m_firstFree - 1] = i;
        } catch (ArrayIndexOutOfBoundsException unused) {
            throw new EmptyStackException();
        }
    }

    public boolean empty() {
        return this.m_firstFree == 0;
    }

    public int search(int i) {
        int lastIndexOf = lastIndexOf(i);
        if (lastIndexOf >= 0) {
            return size() - lastIndexOf;
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.IntVector, java.lang.Object
    public Object clone() throws CloneNotSupportedException {
        return (IntStack) super.clone();
    }
}
