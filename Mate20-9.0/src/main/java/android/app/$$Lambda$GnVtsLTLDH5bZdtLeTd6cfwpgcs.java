package android.app;

import android.app.UiAutomation;
import android.view.accessibility.AccessibilityEvent;
import java.util.function.BiConsumer;

/* renamed from: android.app.-$$Lambda$GnVtsLTLDH5bZdtLeTd6cfwpgcs  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$GnVtsLTLDH5bZdtLeTd6cfwpgcs implements BiConsumer {
    public static final /* synthetic */ $$Lambda$GnVtsLTLDH5bZdtLeTd6cfwpgcs INSTANCE = new $$Lambda$GnVtsLTLDH5bZdtLeTd6cfwpgcs();

    private /* synthetic */ $$Lambda$GnVtsLTLDH5bZdtLeTd6cfwpgcs() {
    }

    public final void accept(Object obj, Object obj2) {
        ((UiAutomation.OnAccessibilityEventListener) obj).onAccessibilityEvent((AccessibilityEvent) obj2);
    }
}
