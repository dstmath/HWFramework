package android.service.contentsuggestions;

import android.os.Bundle;
import com.android.internal.util.function.TriConsumer;

/* renamed from: android.service.contentsuggestions.-$$Lambda$XFxerYS8emT_xgiGwwUrQtqnPnc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$XFxerYS8emT_xgiGwwUrQtqnPnc implements TriConsumer {
    public static final /* synthetic */ $$Lambda$XFxerYS8emT_xgiGwwUrQtqnPnc INSTANCE = new $$Lambda$XFxerYS8emT_xgiGwwUrQtqnPnc();

    private /* synthetic */ $$Lambda$XFxerYS8emT_xgiGwwUrQtqnPnc() {
    }

    @Override // com.android.internal.util.function.TriConsumer
    public final void accept(Object obj, Object obj2, Object obj3) {
        ((ContentSuggestionsService) obj).onNotifyInteraction((String) obj2, (Bundle) obj3);
    }
}
