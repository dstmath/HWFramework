package android.service.contentsuggestions;

import android.app.contentsuggestions.ContentSuggestionsManager;
import android.app.contentsuggestions.SelectionsRequest;
import com.android.internal.util.function.TriConsumer;

/* renamed from: android.service.contentsuggestions.-$$Lambda$yZSFRdNS_6TrQJ8NQKXAv0kSKzk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$yZSFRdNS_6TrQJ8NQKXAv0kSKzk implements TriConsumer {
    public static final /* synthetic */ $$Lambda$yZSFRdNS_6TrQJ8NQKXAv0kSKzk INSTANCE = new $$Lambda$yZSFRdNS_6TrQJ8NQKXAv0kSKzk();

    private /* synthetic */ $$Lambda$yZSFRdNS_6TrQJ8NQKXAv0kSKzk() {
    }

    @Override // com.android.internal.util.function.TriConsumer
    public final void accept(Object obj, Object obj2, Object obj3) {
        ((ContentSuggestionsService) obj).onSuggestContentSelections((SelectionsRequest) obj2, (ContentSuggestionsManager.SelectionsCallback) obj3);
    }
}
