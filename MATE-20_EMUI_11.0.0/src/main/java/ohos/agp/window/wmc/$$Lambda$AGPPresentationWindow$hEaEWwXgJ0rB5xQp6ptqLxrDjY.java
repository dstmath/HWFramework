package ohos.agp.window.wmc;

import android.media.ImageReader;
import java.util.function.Consumer;

/* renamed from: ohos.agp.window.wmc.-$$Lambda$AGPPresentationWindow$hEaEW-wXgJ0rB5xQp6ptqLxrDjY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AGPPresentationWindow$hEaEWwXgJ0rB5xQp6ptqLxrDjY implements Consumer {
    public static final /* synthetic */ $$Lambda$AGPPresentationWindow$hEaEWwXgJ0rB5xQp6ptqLxrDjY INSTANCE = new $$Lambda$AGPPresentationWindow$hEaEWwXgJ0rB5xQp6ptqLxrDjY();

    private /* synthetic */ $$Lambda$AGPPresentationWindow$hEaEWwXgJ0rB5xQp6ptqLxrDjY() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((ImageReader) obj).close();
    }
}
