package android.os;

import java.util.Comparator;
import java.util.Map;

/* renamed from: android.os.-$$Lambda$BinderProxy$ProxyMap$huB_NMtOmTDIIYkL7mXm-Otlfnw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$BinderProxy$ProxyMap$huB_NMtOmTDIIYkL7mXmOtlfnw implements Comparator {
    public static final /* synthetic */ $$Lambda$BinderProxy$ProxyMap$huB_NMtOmTDIIYkL7mXmOtlfnw INSTANCE = new $$Lambda$BinderProxy$ProxyMap$huB_NMtOmTDIIYkL7mXmOtlfnw();

    private /* synthetic */ $$Lambda$BinderProxy$ProxyMap$huB_NMtOmTDIIYkL7mXmOtlfnw() {
    }

    public final int compare(Object obj, Object obj2) {
        return ((Integer) ((Map.Entry) obj2).getValue()).compareTo((Integer) ((Map.Entry) obj).getValue());
    }
}
