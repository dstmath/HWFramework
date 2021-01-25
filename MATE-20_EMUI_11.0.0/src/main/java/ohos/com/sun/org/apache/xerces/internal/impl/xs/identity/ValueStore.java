package ohos.com.sun.org.apache.xerces.internal.impl.xs.identity;

import ohos.com.sun.org.apache.xerces.internal.xs.ShortList;

public interface ValueStore {
    void addValue(Field field, Object obj, short s, ShortList shortList);

    void reportError(String str, Object[] objArr);
}
