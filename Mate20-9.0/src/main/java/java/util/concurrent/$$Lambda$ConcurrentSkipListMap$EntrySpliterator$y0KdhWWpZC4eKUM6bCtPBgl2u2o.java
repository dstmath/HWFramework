package java.util.concurrent;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

/* renamed from: java.util.concurrent.-$$Lambda$ConcurrentSkipListMap$EntrySpliterator$y0KdhWWpZC4eKUM6bCtPBgl2u2o  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ConcurrentSkipListMap$EntrySpliterator$y0KdhWWpZC4eKUM6bCtPBgl2u2o implements Comparator, Serializable {
    public static final /* synthetic */ $$Lambda$ConcurrentSkipListMap$EntrySpliterator$y0KdhWWpZC4eKUM6bCtPBgl2u2o INSTANCE = new $$Lambda$ConcurrentSkipListMap$EntrySpliterator$y0KdhWWpZC4eKUM6bCtPBgl2u2o();

    private /* synthetic */ $$Lambda$ConcurrentSkipListMap$EntrySpliterator$y0KdhWWpZC4eKUM6bCtPBgl2u2o() {
    }

    public final int compare(Object obj, Object obj2) {
        return ((Comparable) ((Map.Entry) obj).getKey()).compareTo(((Map.Entry) obj2).getKey());
    }
}
