package android.os;

import java.util.function.ToIntFunction;

/* renamed from: android.os.-$$Lambda$HidlSupport$GHxmwrIWiKN83tl6aMQt_nV5hiw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HidlSupport$GHxmwrIWiKN83tl6aMQt_nV5hiw implements ToIntFunction {
    public static final /* synthetic */ $$Lambda$HidlSupport$GHxmwrIWiKN83tl6aMQt_nV5hiw INSTANCE = new $$Lambda$HidlSupport$GHxmwrIWiKN83tl6aMQt_nV5hiw();

    private /* synthetic */ $$Lambda$HidlSupport$GHxmwrIWiKN83tl6aMQt_nV5hiw() {
    }

    public final int applyAsInt(Object obj) {
        return HidlSupport.deepHashCode(obj);
    }
}
