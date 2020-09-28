package android.service.contentsuggestions;

import android.graphics.Bitmap;
import android.os.Bundle;
import com.android.internal.util.function.QuadConsumer;

/* renamed from: android.service.contentsuggestions.-$$Lambda$Mv-op4AGm9iWERwfXEFnqOVKWt0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Mvop4AGm9iWERwfXEFnqOVKWt0 implements QuadConsumer {
    public static final /* synthetic */ $$Lambda$Mvop4AGm9iWERwfXEFnqOVKWt0 INSTANCE = new $$Lambda$Mvop4AGm9iWERwfXEFnqOVKWt0();

    private /* synthetic */ $$Lambda$Mvop4AGm9iWERwfXEFnqOVKWt0() {
    }

    @Override // com.android.internal.util.function.QuadConsumer
    public final void accept(Object obj, Object obj2, Object obj3, Object obj4) {
        ((ContentSuggestionsService) obj).onProcessContextImage(((Integer) obj2).intValue(), (Bitmap) obj3, (Bundle) obj4);
    }
}
