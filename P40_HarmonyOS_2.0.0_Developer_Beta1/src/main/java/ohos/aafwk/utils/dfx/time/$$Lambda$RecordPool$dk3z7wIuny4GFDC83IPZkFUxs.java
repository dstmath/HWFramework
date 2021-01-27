package ohos.aafwk.utils.dfx.time;

import java.util.function.Consumer;

/* renamed from: ohos.aafwk.utils.dfx.time.-$$Lambda$RecordPool$dk3-z7wIuny4G-FDC83IPZkFUxs  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RecordPool$dk3z7wIuny4GFDC83IPZkFUxs implements Consumer {
    public static final /* synthetic */ $$Lambda$RecordPool$dk3z7wIuny4GFDC83IPZkFUxs INSTANCE = new $$Lambda$RecordPool$dk3z7wIuny4GFDC83IPZkFUxs();

    private /* synthetic */ $$Lambda$RecordPool$dk3z7wIuny4GFDC83IPZkFUxs() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        RecordPool.lambda$clearDeadRecords$2((TimeRecord) obj);
    }
}
