package org.apache.xml.utils;

import java.util.EmptyStackException;

public class ObjectStack extends ObjectVector {
    public ObjectStack(int blocksize) {
        super(blocksize);
    }

    public ObjectStack(ObjectStack v) {
        super((ObjectVector) v);
    }

    public Object push(Object i) {
        if (this.m_firstFree + 1 >= this.m_mapSize) {
            this.m_mapSize += this.m_blocksize;
            Object[] newMap = new Object[this.m_mapSize];
            System.arraycopy(this.m_map, 0, newMap, 0, this.m_firstFree + 1);
            this.m_map = newMap;
        }
        this.m_map[this.m_firstFree] = i;
        this.m_firstFree++;
        return i;
    }

    public Object pop() {
        Object[] objArr = this.m_map;
        int i = this.m_firstFree - 1;
        this.m_firstFree = i;
        Object val = objArr[i];
        this.m_map[this.m_firstFree] = null;
        return val;
    }

    public void quickPop(int n) {
        this.m_firstFree -= n;
    }

    public Object peek() {
        try {
            return this.m_map[this.m_firstFree - 1];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new EmptyStackException();
        }
    }

    public Object peek(int n) {
        try {
            return this.m_map[this.m_firstFree - (n + 1)];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new EmptyStackException();
        }
    }

    public void setTop(Object val) {
        try {
            this.m_map[this.m_firstFree - 1] = val;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new EmptyStackException();
        }
    }

    public boolean empty() {
        return this.m_firstFree == 0;
    }

    public int search(Object o) {
        int i = lastIndexOf(o);
        if (i >= 0) {
            return size() - i;
        }
        return -1;
    }

    public Object clone() throws CloneNotSupportedException {
        return (ObjectStack) super.clone();
    }
}
