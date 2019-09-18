package java.util;

import java.io.Serializable;
import java.util.Map;

/* renamed from: java.util.-$$Lambda$TreeMap$EntrySpliterator$YqCulUmBGNzQr1PJ_ERFnbxUmTQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TreeMap$EntrySpliterator$YqCulUmBGNzQr1PJ_ERFnbxUmTQ implements Comparator, Serializable {
    public static final /* synthetic */ $$Lambda$TreeMap$EntrySpliterator$YqCulUmBGNzQr1PJ_ERFnbxUmTQ INSTANCE = new $$Lambda$TreeMap$EntrySpliterator$YqCulUmBGNzQr1PJ_ERFnbxUmTQ();

    private /* synthetic */ $$Lambda$TreeMap$EntrySpliterator$YqCulUmBGNzQr1PJ_ERFnbxUmTQ() {
    }

    public final int compare(Object obj, Object obj2) {
        return ((Comparable) ((Map.Entry) obj).getKey()).compareTo(((Map.Entry) obj2).getKey());
    }
}
