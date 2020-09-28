package android.service.contentsuggestions;

import android.app.contentsuggestions.ClassificationsRequest;
import android.app.contentsuggestions.ContentSuggestionsManager;
import com.android.internal.util.function.TriConsumer;

/* renamed from: android.service.contentsuggestions.-$$Lambda$5oRtA6f92le979Nv8-bd2We4x10  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$5oRtA6f92le979Nv8bd2We4x10 implements TriConsumer {
    public static final /* synthetic */ $$Lambda$5oRtA6f92le979Nv8bd2We4x10 INSTANCE = new $$Lambda$5oRtA6f92le979Nv8bd2We4x10();

    private /* synthetic */ $$Lambda$5oRtA6f92le979Nv8bd2We4x10() {
    }

    @Override // com.android.internal.util.function.TriConsumer
    public final void accept(Object obj, Object obj2, Object obj3) {
        ((ContentSuggestionsService) obj).onClassifyContentSelections((ClassificationsRequest) obj2, (ContentSuggestionsManager.ClassificationsCallback) obj3);
    }
}
