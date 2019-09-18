package java.util;

import java.io.Serializable;
import java.util.Map;

/* renamed from: java.util.-$$Lambda$Map$Entry$zJtjVuaqJl6rzQLvCcTd4dnXnnw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Map$Entry$zJtjVuaqJl6rzQLvCcTd4dnXnnw implements Comparator, Serializable {
    public static final /* synthetic */ $$Lambda$Map$Entry$zJtjVuaqJl6rzQLvCcTd4dnXnnw INSTANCE = new $$Lambda$Map$Entry$zJtjVuaqJl6rzQLvCcTd4dnXnnw();

    private /* synthetic */ $$Lambda$Map$Entry$zJtjVuaqJl6rzQLvCcTd4dnXnnw() {
    }

    public final int compare(Object obj, Object obj2) {
        return ((Comparable) ((Map.Entry) obj).getKey()).compareTo(((Map.Entry) obj2).getKey());
    }
}
