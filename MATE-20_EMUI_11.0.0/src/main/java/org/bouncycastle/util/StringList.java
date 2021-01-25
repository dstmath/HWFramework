package org.bouncycastle.util;

public interface StringList extends Iterable<String> {
    boolean add(String str);

    @Override // java.util.List, java.util.AbstractList, org.bouncycastle.util.StringList
    String get(int i);

    int size();

    String[] toStringArray();

    String[] toStringArray(int i, int i2);
}
