package ohos.app.dispatcher.task;

import java.util.function.Consumer;

/* renamed from: ohos.app.dispatcher.task.-$$Lambda$Task$oPLtQ24oGQQ1u0DiqEPczEcYLZw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Task$oPLtQ24oGQQ1u0DiqEPczEcYLZw implements Consumer {
    public static final /* synthetic */ $$Lambda$Task$oPLtQ24oGQQ1u0DiqEPczEcYLZw INSTANCE = new $$Lambda$Task$oPLtQ24oGQQ1u0DiqEPczEcYLZw();

    private /* synthetic */ $$Lambda$Task$oPLtQ24oGQQ1u0DiqEPczEcYLZw() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((TaskListener) obj).onChanged(TaskStage.AFTER_EXECUTE);
    }
}
