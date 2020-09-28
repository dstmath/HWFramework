package huawei.hiview;

import java.util.Iterator;

interface Payload {
    void append(String str, byte b);

    void append(String str, float f);

    void append(String str, int i);

    void append(String str, long j);

    void append(String str, Payload payload);

    void append(String str, String str2);

    void append(String str, short s);

    void append(String str, boolean z);

    void clear();

    Object get(String str);

    Iterator<String> keys();

    void merge(Payload payload);

    void put(String str, byte b);

    void put(String str, float f);

    void put(String str, int i);

    void put(String str, long j);

    void put(String str, Payload payload);

    void put(String str, Object obj);

    void put(String str, String str2);

    void put(String str, short s);

    void put(String str, boolean z);

    int size();
}
