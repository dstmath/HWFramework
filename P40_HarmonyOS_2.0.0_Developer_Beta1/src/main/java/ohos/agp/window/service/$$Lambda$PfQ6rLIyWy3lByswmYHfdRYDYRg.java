package ohos.agp.window.service;

import java.util.function.Function;
import ohos.agp.window.wmc.DisplayManagerWrapper;

/* renamed from: ohos.agp.window.service.-$$Lambda$PfQ6rLIyWy3lByswmYHfdRYDYRg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PfQ6rLIyWy3lByswmYHfdRYDYRg implements Function {
    public static final /* synthetic */ $$Lambda$PfQ6rLIyWy3lByswmYHfdRYDYRg INSTANCE = new $$Lambda$PfQ6rLIyWy3lByswmYHfdRYDYRg();

    private /* synthetic */ $$Lambda$PfQ6rLIyWy3lByswmYHfdRYDYRg() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return new Display((DisplayManagerWrapper.DisplayWrapper) obj);
    }
}
