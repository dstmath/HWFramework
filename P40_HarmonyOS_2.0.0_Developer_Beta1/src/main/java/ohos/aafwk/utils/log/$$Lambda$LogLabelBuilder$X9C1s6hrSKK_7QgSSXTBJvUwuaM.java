package ohos.aafwk.utils.log;

import java.util.function.Function;

/* renamed from: ohos.aafwk.utils.log.-$$Lambda$LogLabelBuilder$X9C1s6hrSKK_7QgSSXTBJvUwuaM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$LogLabelBuilder$X9C1s6hrSKK_7QgSSXTBJvUwuaM implements Function {
    public static final /* synthetic */ $$Lambda$LogLabelBuilder$X9C1s6hrSKK_7QgSSXTBJvUwuaM INSTANCE = new $$Lambda$LogLabelBuilder$X9C1s6hrSKK_7QgSSXTBJvUwuaM();

    private /* synthetic */ $$Lambda$LogLabelBuilder$X9C1s6hrSKK_7QgSSXTBJvUwuaM() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((StackTraceElement) obj).getClassName();
    }
}
