package ohos.app.dispatcher.task;

import java.util.function.Consumer;

/* renamed from: ohos.app.dispatcher.task.-$$Lambda$Task$h4JGBtaRJXOBvmSoGDHDe-Zn5nk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Task$h4JGBtaRJXOBvmSoGDHDeZn5nk implements Consumer {
    public static final /* synthetic */ $$Lambda$Task$h4JGBtaRJXOBvmSoGDHDeZn5nk INSTANCE = new $$Lambda$Task$h4JGBtaRJXOBvmSoGDHDeZn5nk();

    private /* synthetic */ $$Lambda$Task$h4JGBtaRJXOBvmSoGDHDeZn5nk() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((TaskListener) obj).onChanged(TaskStage.BEFORE_EXECUTE);
    }
}
