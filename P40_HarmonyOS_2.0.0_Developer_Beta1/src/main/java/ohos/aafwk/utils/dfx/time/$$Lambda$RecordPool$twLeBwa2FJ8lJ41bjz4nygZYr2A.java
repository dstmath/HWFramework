package ohos.aafwk.utils.dfx.time;

import java.util.function.Consumer;

/* renamed from: ohos.aafwk.utils.dfx.time.-$$Lambda$RecordPool$twLeBwa2FJ8lJ41bjz4nygZYr2A  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RecordPool$twLeBwa2FJ8lJ41bjz4nygZYr2A implements Consumer {
    public static final /* synthetic */ $$Lambda$RecordPool$twLeBwa2FJ8lJ41bjz4nygZYr2A INSTANCE = new $$Lambda$RecordPool$twLeBwa2FJ8lJ41bjz4nygZYr2A();

    private /* synthetic */ $$Lambda$RecordPool$twLeBwa2FJ8lJ41bjz4nygZYr2A() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((TimeRecord) obj).forceDone();
    }
}
