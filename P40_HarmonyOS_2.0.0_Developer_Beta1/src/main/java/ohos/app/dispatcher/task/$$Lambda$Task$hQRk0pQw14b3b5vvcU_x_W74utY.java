package ohos.app.dispatcher.task;

import java.util.function.Consumer;

/* renamed from: ohos.app.dispatcher.task.-$$Lambda$Task$hQRk0pQw14b3b5vvcU_x_W74utY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Task$hQRk0pQw14b3b5vvcU_x_W74utY implements Consumer {
    public static final /* synthetic */ $$Lambda$Task$hQRk0pQw14b3b5vvcU_x_W74utY INSTANCE = new $$Lambda$Task$hQRk0pQw14b3b5vvcU_x_W74utY();

    private /* synthetic */ $$Lambda$Task$hQRk0pQw14b3b5vvcU_x_W74utY() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((TaskListener) obj).onChanged(TaskStage.REVOKED);
    }
}
