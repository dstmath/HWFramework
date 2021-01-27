package ohos.agp.window.wmc;

import android.view.Display;
import java.util.function.Function;
import ohos.agp.window.wmc.DisplayManagerWrapper;

/* renamed from: ohos.agp.window.wmc.-$$Lambda$VjlH3IT22fic4VkJUe9QWJLOryc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$VjlH3IT22fic4VkJUe9QWJLOryc implements Function {
    public static final /* synthetic */ $$Lambda$VjlH3IT22fic4VkJUe9QWJLOryc INSTANCE = new $$Lambda$VjlH3IT22fic4VkJUe9QWJLOryc();

    private /* synthetic */ $$Lambda$VjlH3IT22fic4VkJUe9QWJLOryc() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return new DisplayManagerWrapper.DisplayWrapper((Display) obj);
    }
}
