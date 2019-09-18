package android.content;

import java.util.function.BiConsumer;

/* renamed from: android.content.-$$Lambda$AbstractThreadedSyncAdapter$ISyncAdapterImpl$L6ZtOCe8gjKwJj0908ytPlrD8Rc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AbstractThreadedSyncAdapter$ISyncAdapterImpl$L6ZtOCe8gjKwJj0908ytPlrD8Rc implements BiConsumer {
    public static final /* synthetic */ $$Lambda$AbstractThreadedSyncAdapter$ISyncAdapterImpl$L6ZtOCe8gjKwJj0908ytPlrD8Rc INSTANCE = new $$Lambda$AbstractThreadedSyncAdapter$ISyncAdapterImpl$L6ZtOCe8gjKwJj0908ytPlrD8Rc();

    private /* synthetic */ $$Lambda$AbstractThreadedSyncAdapter$ISyncAdapterImpl$L6ZtOCe8gjKwJj0908ytPlrD8Rc() {
    }

    public final void accept(Object obj, Object obj2) {
        ((AbstractThreadedSyncAdapter) ((AbstractThreadedSyncAdapter) obj)).handleOnUnsyncableAccount((ISyncAdapterUnsyncableAccountCallback) obj2);
    }
}
