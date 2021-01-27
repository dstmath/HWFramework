package ohos.agp.render.render3d.impl;

import java.util.function.Function;
import ohos.agp.render.render3d.components.NodeComponent;

/* renamed from: ohos.agp.render.render3d.impl.-$$Lambda$FYmfdL3V8-kbUOVXOmz02MSn31I  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$FYmfdL3V8kbUOVXOmz02MSn31I implements Function {
    public static final /* synthetic */ $$Lambda$FYmfdL3V8kbUOVXOmz02MSn31I INSTANCE = new $$Lambda$FYmfdL3V8kbUOVXOmz02MSn31I();

    private /* synthetic */ $$Lambda$FYmfdL3V8kbUOVXOmz02MSn31I() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Boolean.valueOf(((NodeComponent) obj).isExported());
    }
}
