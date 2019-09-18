package java.util;

import java.io.Serializable;
import java.util.Map;

/* renamed from: java.util.-$$Lambda$Map$Entry$acJOHw6hO1wh4v9r2vtUuCFe5vI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Map$Entry$acJOHw6hO1wh4v9r2vtUuCFe5vI implements Comparator, Serializable {
    public static final /* synthetic */ $$Lambda$Map$Entry$acJOHw6hO1wh4v9r2vtUuCFe5vI INSTANCE = new $$Lambda$Map$Entry$acJOHw6hO1wh4v9r2vtUuCFe5vI();

    private /* synthetic */ $$Lambda$Map$Entry$acJOHw6hO1wh4v9r2vtUuCFe5vI() {
    }

    public final int compare(Object obj, Object obj2) {
        return ((Comparable) ((Map.Entry) obj).getValue()).compareTo(((Map.Entry) obj2).getValue());
    }
}
