package java.util;

import java.security.PrivilegedAction;

/* renamed from: java.util.-$$Lambda$Tripwire$03Zb3z-rd6SqpmwW72AFPa8slaw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Tripwire$03Zb3zrd6SqpmwW72AFPa8slaw implements PrivilegedAction {
    public static final /* synthetic */ $$Lambda$Tripwire$03Zb3zrd6SqpmwW72AFPa8slaw INSTANCE = new $$Lambda$Tripwire$03Zb3zrd6SqpmwW72AFPa8slaw();

    private /* synthetic */ $$Lambda$Tripwire$03Zb3zrd6SqpmwW72AFPa8slaw() {
    }

    public final Object run() {
        return Boolean.valueOf(Boolean.getBoolean(Tripwire.TRIPWIRE_PROPERTY));
    }
}
